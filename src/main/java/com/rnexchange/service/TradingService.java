package com.rnexchange.service;

import com.rnexchange.domain.*;
import com.rnexchange.domain.enumeration.*;
import com.rnexchange.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for trading operations, including order placement, validation, matching, and settlement.
 *
 * This service implements the cash-only trading flow for M2 (Simple Trading & Portfolio):
 * - Order validation (quantity, funds, instrument)
 * - Order matching against latest prices
 * - Position tracking with average cost calculation
 * - Cash ledger and account balance updates
 * - WebSocket notifications
 *
 * Phase 3 Tasks: T009, T010, T012, T013, T014
 */
@Service
@Transactional
public class TradingService {

    private static final Logger LOG = LoggerFactory.getLogger(TradingService.class);

    private static final BigDecimal DEFAULT_FEE = new BigDecimal("25.00"); // Flat fee for each trade
    private static final int DECIMAL_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final OrderRepository orderRepository;
    private final ExecutionRepository executionRepository;
    private final PositionRepository positionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final InstrumentRepository instrumentRepository;
    private final MatchingService matchingService;
    private final TradingWebSocketService webSocketService;

    public TradingService(
        OrderRepository orderRepository,
        ExecutionRepository executionRepository,
        PositionRepository positionRepository,
        LedgerEntryRepository ledgerEntryRepository,
        TradingAccountRepository tradingAccountRepository,
        InstrumentRepository instrumentRepository,
        MatchingService matchingService,
        TradingWebSocketService webSocketService
    ) {
        this.orderRepository = orderRepository;
        this.executionRepository = executionRepository;
        this.positionRepository = positionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.tradingAccountRepository = tradingAccountRepository;
        this.instrumentRepository = instrumentRepository;
        this.matchingService = matchingService;
        this.webSocketService = webSocketService;
    }

    /**
     * T009: Process a BUY order with validation and orchestration.
     * Flow:
     * 1. Validate order (quantity, instrument, funds)
     * 2. Check instrument status
     * 3. Get matching price and determine if order can be filled
     * 4. If valid, create execution, update position, create ledger entry
     * 5. Publish WebSocket notifications
     *
     * @param order the order to process
     * @param tradingAccount the trading account placing the order
     * @param instrument the instrument being traded
     * @return the processed order with updated status
     */
    public Order processBuyOrder(Order order, TradingAccount tradingAccount, Instrument instrument) {
        LOG.debug("Processing BUY order: {}", order.getId());

        // Step 1: Validate order basics
        validateOrderBasics(order, instrument, tradingAccount);

        // Step 2: Get matching price from market data
        Optional<BigDecimal> matchingPriceOpt = matchingService.getLatestPrice(instrument);
        if (matchingPriceOpt.isEmpty()) {
            String reason = "No price available for instrument " + instrument.getSymbol();
            LOG.warn(reason);
            order.setStatus(OrderStatus.REJECTED);
            order.setRejectionReason(reason);
            order.setUpdatedAt(Instant.now());
            return orderRepository.save(order);
        }

        BigDecimal matchingPrice = matchingPriceOpt.orElseThrow();

        // Step 3: Check if order can be filled (limit order validation)
        if (!canOrderBeFilled(order, matchingPrice)) {
            String reason = String.format("Order cannot be filled: limit price %.2f, market price %.2f", order.getLimitPx(), matchingPrice);
            LOG.info(reason);
            order.setStatus(OrderStatus.REJECTED);
            order.setRejectionReason(reason);
            order.setUpdatedAt(Instant.now());
            return orderRepository.save(order);
        }

        // Step 4: Validate sufficient funds
        BigDecimal totalCost = calculateTotalCost(order.getQty(), matchingPrice);
        if (tradingAccount.getBalance().compareTo(totalCost) < 0) {
            String reason = String.format("Insufficient funds: required %.2f, available %.2f", totalCost, tradingAccount.getBalance());
            LOG.info(reason);
            order.setStatus(OrderStatus.REJECTED);
            order.setRejectionReason(reason);
            order.setUpdatedAt(Instant.now());
            return orderRepository.save(order);
        }

        // Step 5: Create execution and settle trade
        Execution execution = createExecution(order, instrument, tradingAccount, matchingPrice);
        executionRepository.save(execution);

        // Step 6: Update position with average cost
        updatePositionForExecution(tradingAccount, instrument, execution);

        // Step 7: Update ledger and account balance
        updateLedgerAndBalance(tradingAccount, instrument, execution);

        // Step 8: Update order status
        order.setStatus(OrderStatus.FILLED);
        order.setUpdatedAt(Instant.now());
        order = orderRepository.save(order);

        LOG.info("BUY order {} filled at {} for {} units", order.getId(), matchingPrice, order.getQty());

        // Step 9: Publish WebSocket notifications (T014)
        // Notify subscribers about order and execution
        webSocketService.publishTradeCompletedNotification(order, execution);

        return order;
    }

    /**
     * Validate order basics: quantity, instrument status, lot size alignment
     */
    private void validateOrderBasics(Order order, Instrument instrument, TradingAccount tradingAccount) {
        // Validate quantity is positive
        if (order.getQty() == null || order.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order quantity must be positive");
        }

        // Validate instrument is active
        if (instrument == null || !"ACTIVE".equals(instrument.getStatus())) {
            throw new IllegalArgumentException("Instrument is inactive or not found");
        }

        // Validate account type is CASH
        if (tradingAccount.getType() != AccountType.CASH) {
            throw new IllegalArgumentException("FR-014: Only CASH account type is supported");
        }

        // Validate quantity is multiple of lot size
        if (instrument.getLotSize() != null && instrument.getLotSize() > 0) {
            BigDecimal lotSize = BigDecimal.valueOf(instrument.getLotSize());
            BigDecimal[] divisionResult = order.getQty().divideAndRemainder(lotSize);
            if (divisionResult[1].compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException(
                    String.format("Order quantity %s must be a multiple of lot size %d", order.getQty(), instrument.getLotSize())
                );
            }
        }
    }

    /**
     * Check if order can be filled based on limit price condition
     */
    private boolean canOrderBeFilled(Order order, BigDecimal marketPrice) {
        if (order.getType() == OrderType.MARKET) {
            return true; // Market orders always fill
        }

        if (order.getType() == OrderType.LIMIT) {
            if (order.getLimitPx() == null) {
                throw new IllegalArgumentException("Limit price required for LIMIT orders");
            }

            // For BUY: order fills if market price <= limit price
            if (order.getSide() == OrderSide.BUY) {
                return marketPrice.compareTo(order.getLimitPx()) <= 0;
            }

            // For SELL: order fills if market price >= limit price
            if (order.getSide() == OrderSide.SELL) {
                return marketPrice.compareTo(order.getLimitPx()) >= 0;
            }
        }

        return false;
    }

    /**
     * Calculate total cost: quantity × price + fee
     */
    private BigDecimal calculateTotalCost(BigDecimal quantity, BigDecimal price) {
        BigDecimal tradeValue = quantity.multiply(price).setScale(DECIMAL_SCALE, ROUNDING_MODE);
        return tradeValue.add(DEFAULT_FEE);
    }

    /**
     * Create execution record for the filled order
     * T010: Obtain price and create execution
     */
    private Execution createExecution(Order order, Instrument instrument, TradingAccount tradingAccount, BigDecimal executionPrice) {
        Execution execution = new Execution();
        execution.setOrder(order);
        execution.setTradingAccount(tradingAccount);
        execution.setInstrument(instrument);
        execution.setSide(order.getSide());
        execution.setQty(order.getQty());
        execution.setPx(executionPrice.setScale(DECIMAL_SCALE, ROUNDING_MODE));
        execution.setExecTs(Instant.now());
        execution.setFee(DEFAULT_FEE);
        execution.setLiquidity("SELF_MATCH"); // Simulated fill
        return execution;
    }

    /**
     * T012: Update position with new quantity and average cost
     * For BUY:
     *   newQty = oldQty + execQty
     *   newAvgCost = (oldQty × oldAvgCost + execQty × execPrice) / newQty
     */
    private void updatePositionForExecution(TradingAccount tradingAccount, Instrument instrument, Execution execution) {
        Optional<Position> existingPosition = positionRepository.findByTradingAccountAndInstrument(tradingAccount, instrument);

        Position position;
        if (existingPosition.isPresent()) {
            position = existingPosition.orElseThrow();
            BigDecimal oldQty = position.getQty();
            BigDecimal oldAvgCost = position.getAvgCost();
            BigDecimal execQty = execution.getQty();
            BigDecimal execPrice = execution.getPx();

            // Calculate new average cost
            BigDecimal totalCostBasis = oldQty.multiply(oldAvgCost).add(execQty.multiply(execPrice));
            BigDecimal newQty = oldQty.add(execQty);
            BigDecimal newAvgCost = totalCostBasis.divide(newQty, DECIMAL_SCALE, ROUNDING_MODE);

            position.setQty(newQty);
            position.setAvgCost(newAvgCost);

            LOG.debug(
                "Updated position for {}: qty {} -> {}, avgCost {} -> {}",
                instrument.getSymbol(),
                oldQty,
                newQty,
                oldAvgCost,
                newAvgCost
            );
        } else {
            // Create new position
            position = new Position();
            position.setTradingAccount(tradingAccount);
            position.setInstrument(instrument);
            position.setQty(execution.getQty());
            position.setAvgCost(execution.getPx());
            position.setLastPx(execution.getPx());

            LOG.debug("Created new position for {}: qty {}, avgCost {}", instrument.getSymbol(), position.getQty(), position.getAvgCost());
        }

        // Update MTM if last price is available
        if (position.getLastPx() != null) {
            BigDecimal mtm = position.getLastPx().subtract(position.getAvgCost()).multiply(position.getQty());
            position.setUnrealizedPnl(mtm.setScale(DECIMAL_SCALE, ROUNDING_MODE));
        }

        positionRepository.save(position);
    }

    /**
     * T013: Update ledger and trading account balance
     * For BUY: create DEBIT entry, decrease balance
     */
    private void updateLedgerAndBalance(TradingAccount tradingAccount, Instrument instrument, Execution execution) {
        // Calculate debit amount: quantity × price + fee
        BigDecimal debitAmount = execution.getQty().multiply(execution.getPx()).add(execution.getFee());

        // Create ledger entry
        LedgerEntry entry = new LedgerEntry();
        entry.setTradingAccount(tradingAccount);
        entry.setType(LedgerEntryType.DEBIT);
        entry.setAmount(debitAmount.setScale(DECIMAL_SCALE, ROUNDING_MODE));
        entry.setFee(DEFAULT_FEE);
        entry.setCcy(Currency.INR);
        entry.setCreatedAt(Instant.now());
        entry.setDescription(String.format("BUY %s x%s @ %s", instrument.getSymbol(), execution.getQty().intValue(), execution.getPx()));
        entry.setReference("ORD-" + execution.getOrder().getId());

        // Update balance
        BigDecimal newBalance = tradingAccount.getBalance().subtract(debitAmount);
        tradingAccount.setBalance(newBalance.setScale(DECIMAL_SCALE, ROUNDING_MODE));
        entry.setBalanceAfter(newBalance);

        ledgerEntryRepository.save(entry);
        tradingAccountRepository.save(tradingAccount);

        LOG.debug("Updated ledger and balance for account {}: debit {}, new balance {}", tradingAccount.getId(), debitAmount, newBalance);
    }

    /**
     * T020, T021: Process a SELL order with validation and orchestration.
     * Flow:
     * 1. Validate order (quantity, instrument, existing position)
     * 2. Check instrument status
     * 3. Get matching price and determine if order can be filled
     * 4. If valid, create execution, reduce position, calculate realized P&L, create credit ledger entry
     * 5. Publish WebSocket notifications
     *
     * @param order the order to process
     * @param tradingAccount the trading account placing the order
     * @param instrument the instrument being traded
     * @return the processed order with updated status
     */
    public Order processSellOrder(Order order, TradingAccount tradingAccount, Instrument instrument) {
        LOG.debug("Processing SELL order: {}", order.getId());

        // Step 1: Validate order basics
        validateOrderBasics(order, instrument, tradingAccount);

        // Step 2: Validate SELL-specific constraints (T020)
        validateSellOrder(order, tradingAccount, instrument);

        // Step 3: Get matching price from market data
        Optional<BigDecimal> matchingPriceOpt = matchingService.getLatestPrice(instrument);
        if (matchingPriceOpt.isEmpty()) {
            String reason = "No price available for instrument " + instrument.getSymbol();
            LOG.warn(reason);
            order.setStatus(OrderStatus.REJECTED);
            order.setRejectionReason(reason);
            order.setUpdatedAt(Instant.now());
            return orderRepository.save(order);
        }

        BigDecimal matchingPrice = matchingPriceOpt.orElseThrow();

        // Step 4: Check if order can be filled (limit order validation)
        if (!canOrderBeFilled(order, matchingPrice)) {
            String reason = String.format("Order cannot be filled: limit price %.2f, market price %.2f", order.getLimitPx(), matchingPrice);
            LOG.info(reason);
            order.setStatus(OrderStatus.REJECTED);
            order.setRejectionReason(reason);
            order.setUpdatedAt(Instant.now());
            return orderRepository.save(order);
        }

        // Step 5: Create execution and settle trade
        Execution execution = createExecution(order, instrument, tradingAccount, matchingPrice);
        executionRepository.save(execution);

        // Step 6: Update position and calculate realized P&L (T021)
        BigDecimal realizedPnl = updatePositionForSellExecution(tradingAccount, instrument, execution);

        // Step 7: Update ledger and account balance with credit (T021)
        updateLedgerAndBalanceForSell(tradingAccount, instrument, execution, realizedPnl);

        // Step 8: Update order status
        order.setStatus(OrderStatus.FILLED);
        order.setUpdatedAt(Instant.now());
        order = orderRepository.save(order);

        LOG.info("SELL order {} filled at {} for {} units, realized P&L: {}", order.getId(), matchingPrice, order.getQty(), realizedPnl);

        // Step 9: Publish WebSocket notifications
        webSocketService.publishTradeCompletedNotification(order, execution);

        return order;
    }

    /**
     * T020: Validate SELL-specific requirements: position exists with sufficient quantity
     */
    private void validateSellOrder(Order order, TradingAccount tradingAccount, Instrument instrument) {
        // Find existing position
        Optional<Position> existingPosition = positionRepository.findByTradingAccountAndInstrument(tradingAccount, instrument);

        if (existingPosition.isEmpty()) {
            throw new IllegalArgumentException(String.format("Cannot SELL: no existing position in %s", instrument.getSymbol()));
        }

        Position position = existingPosition.orElseThrow();

        // Validate sufficient quantity
        if (position.getQty().compareTo(order.getQty()) < 0) {
            throw new IllegalArgumentException(
                String.format("Insufficient position: have %.0f units, trying to sell %.0f", position.getQty(), order.getQty())
            );
        }

        LOG.debug("SELL validation passed: position {} >= order {}", position.getQty(), order.getQty());
    }

    /**
     * T021: Update position for SELL execution (reduce quantity, keep average cost unchanged)
     * Returns the realized P&L for this transaction
     * Realized P&L = (executionPrice - averageCost) * executionQuantity
     */
    private BigDecimal updatePositionForSellExecution(TradingAccount tradingAccount, Instrument instrument, Execution execution) {
        Optional<Position> existingPosition = positionRepository.findByTradingAccountAndInstrument(tradingAccount, instrument);

        if (existingPosition.isEmpty()) {
            throw new IllegalStateException("Position should exist for SELL execution");
        }

        Position position = existingPosition.orElseThrow();
        BigDecimal oldQty = position.getQty();
        BigDecimal avgCost = position.getAvgCost();
        BigDecimal execQty = execution.getQty();
        BigDecimal execPrice = execution.getPx();

        // Calculate realized P&L (T020)
        BigDecimal realizedPnl = execPrice.subtract(avgCost).multiply(execQty).setScale(DECIMAL_SCALE, ROUNDING_MODE);

        // Reduce position quantity (average cost stays the same)
        BigDecimal newQty = oldQty.subtract(execQty).setScale(DECIMAL_SCALE, ROUNDING_MODE);
        position.setQty(newQty);

        // Update MTM (mark-to-market): now based on remaining quantity
        if (position.getLastPx() != null && newQty.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal mtm = position.getLastPx().subtract(avgCost).multiply(newQty);
            position.setUnrealizedPnl(mtm.setScale(DECIMAL_SCALE, ROUNDING_MODE));
        } else if (newQty.compareTo(BigDecimal.ZERO) == 0) {
            position.setUnrealizedPnl(BigDecimal.ZERO);
        }

        positionRepository.save(position);

        LOG.debug(
            "Updated position for SELL {}: qty {} -> {}, avgCost remains {}, realized P&L {}",
            instrument.getSymbol(),
            oldQty,
            newQty,
            avgCost,
            realizedPnl
        );

        return realizedPnl;
    }

    /**
     * T021: Update ledger and trading account balance for SELL execution
     * For SELL: create CREDIT entry with amount = quantity × price - fee
     * Increase balance (opposite of BUY debit)
     */
    private void updateLedgerAndBalanceForSell(
        TradingAccount tradingAccount,
        Instrument instrument,
        Execution execution,
        BigDecimal realizedPnl
    ) {
        // Calculate credit amount: quantity × price - fee
        BigDecimal creditAmount = execution.getQty().multiply(execution.getPx()).subtract(execution.getFee());

        // Create ledger entry for SELL credit
        LedgerEntry entry = new LedgerEntry();
        entry.setTradingAccount(tradingAccount);
        entry.setType(LedgerEntryType.CREDIT);
        entry.setAmount(creditAmount.setScale(DECIMAL_SCALE, ROUNDING_MODE));
        entry.setFee(DEFAULT_FEE);
        entry.setCcy(Currency.INR);
        entry.setCreatedAt(Instant.now());

        // Include realized P&L in description if not break-even
        String description;
        if (realizedPnl.compareTo(BigDecimal.ZERO) != 0) {
            description = String.format(
                "SELL %s x%s @ %s, P&L: %s",
                instrument.getSymbol(),
                execution.getQty().intValue(),
                execution.getPx(),
                realizedPnl
            );
        } else {
            description = String.format("SELL %s x%s @ %s", instrument.getSymbol(), execution.getQty().intValue(), execution.getPx());
        }
        entry.setDescription(description);
        entry.setReference("ORD-" + execution.getOrder().getId());

        // Update balance (add credit, opposite of BUY)
        BigDecimal newBalance = tradingAccount.getBalance().add(creditAmount);
        tradingAccount.setBalance(newBalance.setScale(DECIMAL_SCALE, ROUNDING_MODE));
        entry.setBalanceAfter(newBalance);

        ledgerEntryRepository.save(entry);
        tradingAccountRepository.save(tradingAccount);

        LOG.debug(
            "Updated ledger and balance for account {} after SELL: credit {}, new balance {}",
            tradingAccount.getId(),
            creditAmount,
            newBalance
        );
    }

    /**
     * Get latest price for an instrument (uses MatchingService)
     */
    public Optional<BigDecimal> getLatestPrice(Instrument instrument) {
        return matchingService.getLatestPrice(instrument);
    }
}
