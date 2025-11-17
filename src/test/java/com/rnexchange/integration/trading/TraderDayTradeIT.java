package com.rnexchange.integration.trading;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.*;
import com.rnexchange.domain.enumeration.*;
import com.rnexchange.repository.*;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.TradingService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for trader day trade flow (User Story 1 - T013).
 *
 * Verifies order → execution → position → ledger consistency for a demo trader:
 * - Market order placement
 * - Order execution and status update
 * - Position creation/update with correct quantity and average cost
 * - Ledger entry creation with correct debit amount and description
 * - Trading account balance update
 * - All entities are consistent and reconciled
 */
@IntegrationTest
@WithMockUser(authorities = AuthoritiesConstants.TRADER, username = "trader_demo")
@Transactional
class TraderDayTradeIT {

    @Autowired
    private TradingService tradingService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private TradingAccountRepository tradingAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private DailySettlementPriceRepository dailySettlementPriceRepository;

    @Autowired
    private EntityManager em;

    private TradingAccount tradingAccount;
    private Instrument instrument;
    private DailySettlementPrice settlementPrice;

    @BeforeEach
    void setUp() {
        // Create or find trading account for demo trader
        tradingAccount = tradingAccountRepository
            .findFirstByTrader_User_LoginOrderByIdAsc("trader_demo")
            .orElseGet(() -> {
                TradingAccount account = new TradingAccount();
                account.setType(AccountType.CASH);
                account.setBaseCcy(Currency.INR);
                account.setBalance(new BigDecimal("100000.00")); // Starting balance
                account.setStatus(AccountStatus.ACTIVE);
                return tradingAccountRepository.save(account);
            });

        // Ensure sufficient balance for test
        if (tradingAccount.getBalance().compareTo(new BigDecimal("10000.00")) < 0) {
            tradingAccount.setBalance(new BigDecimal("100000.00"));
            tradingAccount = tradingAccountRepository.save(tradingAccount);
        }

        // Create or find instrument (RELIANCE on NSE)
        instrument = instrumentRepository
            .findOneBySymbol("RELIANCE")
            .filter(inst -> inst.getExchange() != null && "NSE".equals(inst.getExchange().getCode()))
            .orElseGet(() -> {
                Exchange exchange = new Exchange();
                exchange.setCode("NSE");
                exchange.setName("National Stock Exchange");
                exchange.setStatus(ExchangeStatus.ACTIVE);
                em.persist(exchange);
                em.flush();

                Instrument inst = new Instrument();
                inst.setSymbol("RELIANCE");
                inst.setExchange(exchange);
                inst.setName("Reliance Industries Ltd");
                inst.setStatus("ACTIVE");
                inst.setLotSize(1L);
                inst.setTickSize(new BigDecimal("0.05"));
                return instrumentRepository.save(inst);
            });

        // Create or find settlement price for the instrument
        LocalDate today = LocalDate.now();
        settlementPrice = dailySettlementPriceRepository
            .findByRefDateAndInstrument_Id(today, instrument.getId())
            .orElseGet(() -> {
                DailySettlementPrice price = new DailySettlementPrice();
                price.setInstrument(instrument);
                price.setRefDate(today);
                price.setSettlePrice(new BigDecimal("2520.00")); // Settlement price for matching
                return dailySettlementPriceRepository.save(price);
            });

        // Update settlement price to current
        settlementPrice.setSettlePrice(new BigDecimal("2520.00"));
        settlementPrice = dailySettlementPriceRepository.save(settlementPrice);

        em.flush();
        em.clear();
    }

    @Test
    void testOrderExecutionCreatesPositionAndLedgerEntry() {
        // Given: Initial state
        BigDecimal initialBalance = tradingAccount.getBalance();
        BigDecimal orderQuantity = new BigDecimal("10");
        BigDecimal expectedPrice = new BigDecimal("2520.00");
        BigDecimal expectedTotalCost = orderQuantity
            .multiply(expectedPrice)
            .add(new BigDecimal("25.00")) // Fee
            .setScale(2, RoundingMode.HALF_UP);

        // Verify no position exists
        Optional<Position> existingPosition = positionRepository.findByTradingAccountAndInstrument(tradingAccount, instrument);
        assertThat(existingPosition).isEmpty();

        // Count initial ledger entries
        long initialLedgerCount = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(entry -> entry.getTradingAccount() != null && entry.getTradingAccount().getId().equals(tradingAccount.getId()))
            .count();

        // When: Place and execute a market BUY order
        Order order = new Order();
        order.setTradingAccount(tradingAccount);
        order.setInstrument(instrument);
        order.setSide(OrderSide.BUY);
        order.setType(OrderType.MARKET);
        order.setQty(orderQuantity);
        order.setStatus(OrderStatus.NEW);
        order.setTif(Tif.DAY);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order = orderRepository.save(order);

        // Process the order through TradingService
        Order processedOrder = tradingService.processBuyOrder(order, tradingAccount, instrument);

        // Then: Verify order status
        assertThat(processedOrder.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(processedOrder.getRejectionReason()).isNull();

        // Verify execution was created
        List<Execution> executions = executionRepository
            .findAll()
            .stream()
            .filter(exec -> exec.getOrder() != null && exec.getOrder().getId().equals(processedOrder.getId()))
            .toList();
        assertThat(executions).hasSize(1);
        Execution execution = executions.get(0);
        assertThat(execution.getSide()).isEqualTo(OrderSide.BUY);
        assertThat(execution.getQty()).isEqualByComparingTo(orderQuantity);
        assertThat(execution.getPx()).isEqualByComparingTo(expectedPrice);
        assertThat(execution.getFee()).isEqualByComparingTo(new BigDecimal("25.00"));

        // Verify position was created
        Optional<Position> positionOpt = positionRepository.findByTradingAccountAndInstrument(tradingAccount, instrument);
        assertThat(positionOpt).isPresent();
        Position position = positionOpt.orElseThrow();
        assertThat(position.getQty()).isEqualByComparingTo(orderQuantity);
        assertThat(position.getAvgCost()).isEqualByComparingTo(expectedPrice);

        // Verify ledger entry was created
        long newLedgerCount = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(entry -> entry.getTradingAccount() != null && entry.getTradingAccount().getId().equals(tradingAccount.getId()))
            .count();
        assertThat(newLedgerCount).isEqualTo(initialLedgerCount + 1);

        List<LedgerEntry> ledgerEntries = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(entry -> entry.getTradingAccount() != null && entry.getTradingAccount().getId().equals(tradingAccount.getId()))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList();
        LedgerEntry latestEntry = ledgerEntries.get(0);
        assertThat(latestEntry.getType()).isEqualTo(LedgerEntryType.DEBIT);
        assertThat(latestEntry.getAmount()).isEqualByComparingTo(expectedTotalCost);
        assertThat(latestEntry.getFee()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(latestEntry.getDescription()).contains("BUY");
        assertThat(latestEntry.getDescription()).contains(instrument.getSymbol());
        assertThat(latestEntry.getReference()).contains("ORD-" + order.getId());

        // Verify trading account balance was updated
        TradingAccount updatedAccount = tradingAccountRepository.findById(tradingAccount.getId()).orElseThrow();
        BigDecimal expectedBalance = initialBalance.subtract(expectedTotalCost);
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(expectedBalance);

        // Verify ledger entry balance matches account balance
        assertThat(latestEntry.getBalanceAfter()).isEqualByComparingTo(updatedAccount.getBalance());

        // Verify reconciliation: Position quantity × average cost should equal total cost basis
        BigDecimal positionCostBasis = position.getQty().multiply(position.getAvgCost());
        BigDecimal totalDebited = expectedTotalCost;
        // Note: Total debited includes fee, so we check position cost basis matches trade value (excluding fee)
        BigDecimal tradeValue = orderQuantity.multiply(expectedPrice);
        assertThat(positionCostBasis).isEqualByComparingTo(tradeValue);
    }

    @Test
    void testMultipleOrdersUpdatePositionWithCorrectAverageCost() {
        // Given: Initial state
        BigDecimal firstOrderQty = new BigDecimal("10");
        BigDecimal firstOrderPrice = new BigDecimal("2500.00");
        BigDecimal secondOrderQty = new BigDecimal("5");
        BigDecimal secondOrderPrice = new BigDecimal("2550.00");

        // Update settlement price for first order
        settlementPrice.setSettlePrice(firstOrderPrice);
        settlementPrice = dailySettlementPriceRepository.save(settlementPrice);

        // When: Place first order
        Order order1 = new Order();
        order1.setTradingAccount(tradingAccount);
        order1.setInstrument(instrument);
        order1.setSide(OrderSide.BUY);
        order1.setType(OrderType.MARKET);
        order1.setQty(firstOrderQty);
        order1.setStatus(OrderStatus.NEW);
        order1.setTif(Tif.DAY);
        order1.setCreatedAt(Instant.now());
        order1.setUpdatedAt(Instant.now());
        order1 = orderRepository.save(order1);
        Order processedOrder1 = tradingService.processBuyOrder(order1, tradingAccount, instrument);

        // Update settlement price for second order
        settlementPrice.setSettlePrice(secondOrderPrice);
        settlementPrice = dailySettlementPriceRepository.save(settlementPrice);

        // When: Place second order
        Order order2 = new Order();
        order2.setTradingAccount(tradingAccount);
        order2.setInstrument(instrument);
        order2.setSide(OrderSide.BUY);
        order2.setType(OrderType.MARKET);
        order2.setQty(secondOrderQty);
        order2.setStatus(OrderStatus.NEW);
        order2.setTif(Tif.DAY);
        order2.setCreatedAt(Instant.now());
        order2.setUpdatedAt(Instant.now());
        order2 = orderRepository.save(order2);

        // Reload trading account to get updated balance
        tradingAccount = tradingAccountRepository.findById(tradingAccount.getId()).orElseThrow();
        Order processedOrder2 = tradingService.processBuyOrder(order2, tradingAccount, instrument);

        // Then: Verify both orders are filled
        assertThat(processedOrder1.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(processedOrder2.getStatus()).isEqualTo(OrderStatus.FILLED);

        // Verify position has correct quantity and average cost
        Optional<Position> positionOpt = positionRepository.findByTradingAccountAndInstrument(tradingAccount, instrument);
        assertThat(positionOpt).isPresent();
        Position position = positionOpt.orElseThrow();

        BigDecimal expectedTotalQty = firstOrderQty.add(secondOrderQty);
        assertThat(position.getQty()).isEqualByComparingTo(expectedTotalQty);

        // Calculate expected average cost: (qty1 * price1 + qty2 * price2) / totalQty
        BigDecimal totalCostBasis = firstOrderQty.multiply(firstOrderPrice).add(secondOrderQty.multiply(secondOrderPrice));
        BigDecimal expectedAvgCost = totalCostBasis.divide(expectedTotalQty, 2, RoundingMode.HALF_UP);
        assertThat(position.getAvgCost()).isEqualByComparingTo(expectedAvgCost);

        // Verify two ledger entries were created
        List<LedgerEntry> ledgerEntries = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(entry -> entry.getTradingAccount() != null && entry.getTradingAccount().getId().equals(tradingAccount.getId()))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList();
        assertThat(ledgerEntries.size()).isGreaterThanOrEqualTo(2);

        // Verify ledger entries have correct descriptions
        assertThat(ledgerEntries.get(0).getDescription()).contains("BUY");
        assertThat(ledgerEntries.get(1).getDescription()).contains("BUY");
    }
}
