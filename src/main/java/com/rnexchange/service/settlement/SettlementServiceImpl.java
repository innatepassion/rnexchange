package com.rnexchange.service.settlement;

import com.rnexchange.domain.*;
import com.rnexchange.domain.enumeration.LedgerEntryType;
import com.rnexchange.domain.enumeration.SettlementKind;
import com.rnexchange.domain.enumeration.SettlementStatus;
import com.rnexchange.repository.*;
import com.rnexchange.service.settlement.event.SettlementCompletedEvent;
import com.rnexchange.service.settlement.event.SettlementFailedEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of SettlementService for running end-of-day settlement batches.
 */
@Service
@Transactional
public class SettlementServiceImpl implements SettlementService {

    private static final Logger LOG = LoggerFactory.getLogger(SettlementServiceImpl.class);
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final SettlementBatchRepository settlementBatchRepository;
    private final PositionRepository positionRepository;
    private final DailySettlementPriceRepository dailySettlementPriceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final ReportLinkRepository reportLinkRepository;
    private final ExchangeRepository exchangeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SettlementServiceImpl(
        SettlementBatchRepository settlementBatchRepository,
        PositionRepository positionRepository,
        DailySettlementPriceRepository dailySettlementPriceRepository,
        LedgerEntryRepository ledgerEntryRepository,
        TradingAccountRepository tradingAccountRepository,
        ReportLinkRepository reportLinkRepository,
        ExchangeRepository exchangeRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.settlementBatchRepository = settlementBatchRepository;
        this.positionRepository = positionRepository;
        this.dailySettlementPriceRepository = dailySettlementPriceRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.tradingAccountRepository = tradingAccountRepository;
        this.reportLinkRepository = reportLinkRepository;
        this.exchangeRepository = exchangeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void runEod(LocalDate tradeDate) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);
        }
        LOG.info("[correlationId={}] Starting EOD settlement for trade date: {}", correlationId, tradeDate);

        // Get or create default exchange (assuming single exchange for MVP)
        Exchange exchange = exchangeRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No exchange found. At least one exchange must exist."));

        // Create or get existing batch
        SettlementBatch batch = findOrCreateBatch(tradeDate, exchange);
        batch.setStatus(SettlementStatus.CREATED);
        batch = settlementBatchRepository.save(batch);

        try {
            // Get all open positions
            List<Position> openPositions = positionRepository.findOpenPositions(ZERO);
            LOG.debug("[correlationId={}] Found {} open positions for EOD", correlationId, openPositions.size());

            if (openPositions.isEmpty()) {
                LOG.info("[correlationId={}] No open positions found for EOD settlement on {}", correlationId, tradeDate);
                batch.setStatus(SettlementStatus.PROCESSED);
                batch.setRemarks("{\"accountsProcessed\":0,\"positionsProcessed\":0,\"netPnl\":0}");
                settlementBatchRepository.save(batch);
                eventPublisher.publishEvent(new SettlementCompletedEvent(batch.getId(), tradeDate, 0, 0, ZERO));
                return;
            }

            // Group positions by trading account
            Map<TradingAccount, List<Position>> positionsByAccount = openPositions
                .stream()
                .collect(Collectors.groupingBy(Position::getTradingAccount));

            int accountsProcessed = 0;
            int positionsProcessed = 0;
            BigDecimal totalNetPnl = ZERO;

            // Process each account
            for (Map.Entry<TradingAccount, List<Position>> entry : positionsByAccount.entrySet()) {
                TradingAccount account = entry.getKey();
                List<Position> accountPositions = entry.getValue();

                try {
                    // Reverse any previous EOD MTM entries for this date
                    reversePreviousEodEntries(account, tradeDate);

                    // Calculate and update positions
                    BigDecimal accountNetPnl = ZERO;
                    for (Position position : accountPositions) {
                        BigDecimal settlePrice = resolveSettlementPrice(position.getInstrument(), tradeDate);
                        BigDecimal mtm = calculateMtm(position, settlePrice);
                        updatePosition(position, settlePrice, mtm);
                        accountNetPnl = accountNetPnl.add(mtm);
                        positionsProcessed++;
                    }

                    // Post ledger entry for account
                    if (accountNetPnl.compareTo(ZERO) != 0) {
                        postEodLedgerEntry(account, accountNetPnl, tradeDate, batch);
                    }

                    accountsProcessed++;
                    totalNetPnl = totalNetPnl.add(accountNetPnl);

                    LOG.debug(
                        "[correlationId={}] Processed account {}: {} positions, net P&L: {}",
                        correlationId,
                        account.getId(),
                        accountPositions.size(),
                        accountNetPnl
                    );
                } catch (Exception e) {
                    LOG.error("[correlationId={}] Error processing account {}: {}", correlationId, account.getId(), e.getMessage(), e);
                    throw new RuntimeException("Failed to process account " + account.getId() + ": " + e.getMessage(), e);
                }
            }

            // Update batch status
            batch.setStatus(SettlementStatus.PROCESSED);
            String remarks = String.format(
                "{\"accountsProcessed\":%d,\"positionsProcessed\":%d,\"netPnl\":%s}",
                accountsProcessed,
                positionsProcessed,
                totalNetPnl
            );
            batch.setRemarks(remarks);
            settlementBatchRepository.save(batch);

            // Create report links (placeholder for now - will be enhanced in Phase 4)
            createReportLinks(batch, tradeDate, positionsByAccount.keySet());

            LOG.info(
                "[correlationId={}] EOD settlement completed for {}: {} accounts, {} positions, net P&L: {}",
                correlationId,
                tradeDate,
                accountsProcessed,
                positionsProcessed,
                totalNetPnl
            );

            eventPublisher.publishEvent(
                new SettlementCompletedEvent(batch.getId(), tradeDate, accountsProcessed, positionsProcessed, totalNetPnl)
            );
        } catch (Exception e) {
            correlationId = MDC.get("correlationId");
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            LOG.error("[correlationId={}] EOD settlement failed for {}: {}", correlationId, tradeDate, e.getMessage(), e);
            batch.setStatus(SettlementStatus.FAILED);
            batch.setRemarks("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            settlementBatchRepository.save(batch);
            eventPublisher.publishEvent(new SettlementFailedEvent(batch.getId(), tradeDate, e.getMessage()));
            throw new RuntimeException("EOD settlement failed: " + e.getMessage(), e);
        }
    }

    private SettlementBatch findOrCreateBatch(LocalDate tradeDate, Exchange exchange) {
        // Find existing batch for this date and kind
        List<SettlementBatch> existing = settlementBatchRepository
            .findAll()
            .stream()
            .filter(b -> b.getRefDate().equals(tradeDate) && b.getKind() == SettlementKind.EOD)
            .toList();

        if (!existing.isEmpty()) {
            // Use the most recent one
            return existing.stream().max(Comparator.comparing(SettlementBatch::getId)).orElseThrow();
        }

        // Create new batch
        SettlementBatch batch = new SettlementBatch();
        batch.setRefDate(tradeDate);
        batch.setKind(SettlementKind.EOD);
        batch.setStatus(SettlementStatus.CREATED);
        batch.setExchange(exchange);
        return settlementBatchRepository.save(batch);
    }

    private BigDecimal resolveSettlementPrice(Instrument instrument, LocalDate tradeDate) {
        // First try DailySettlementPrice for the given trade date
        Optional<DailySettlementPrice> settlementPrice = dailySettlementPriceRepository.findByRefDateAndInstrument_Id(
            tradeDate,
            instrument.getId()
        );

        if (settlementPrice.isPresent()) {
            return settlementPrice.get().getSettlePrice();
        }

        // Fallback: use the latest available settlement price that is not after the trade date
        Optional<DailySettlementPrice> latestPrice = dailySettlementPriceRepository.findFirstByInstrument_IdOrderByRefDateDesc(
            instrument.getId()
        );

        if (latestPrice.isPresent()) {
            DailySettlementPrice latest = latestPrice.get();
            if (!latest.getRefDate().isAfter(tradeDate)) {
                return latest.getSettlePrice();
            }
        }

        // TODO: Fallback to 1-minute bar close price (not implemented yet)
        // For now, throw a clear exception if no usable price is found
        throw new IllegalStateException(
            "No settlement price found for instrument " + instrument.getSymbol() + " on or before date " + tradeDate
        );
    }

    private BigDecimal calculateMtm(Position position, BigDecimal settlePrice) {
        // MTM = (settlePrice - avgCost) * qty
        BigDecimal priceDiff = settlePrice.subtract(position.getAvgCost());
        return priceDiff.multiply(position.getQty()).setScale(2, RoundingMode.HALF_UP);
    }

    private void updatePosition(Position position, BigDecimal settlePrice, BigDecimal mtm) {
        position.setLastPx(settlePrice);
        // Update unrealized P&L (cumulative)
        BigDecimal currentUnrealized = position.getUnrealizedPnl() != null ? position.getUnrealizedPnl() : ZERO;
        // For EOD, we set unrealized P&L to the MTM based on settlement price
        position.setUnrealizedPnl(mtm);
        positionRepository.save(position);
    }

    private void reversePreviousEodEntries(TradingAccount account, LocalDate tradeDate) {
        // Find and mark previous EOD MTM entries as superseded (for audit trail)
        // In a production system, we might want to create reversal entries or mark them differently
        // For now, we'll update the reference field to indicate they were superseded
        List<LedgerEntry> previousEntries = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(
                le ->
                    le.getTradingAccount() != null &&
                    le.getTradingAccount().getId().equals(account.getId()) &&
                    (le.getType() == LedgerEntryType.EOD_MTM_CREDIT || le.getType() == LedgerEntryType.EOD_MTM_DEBIT) &&
                    le.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().equals(tradeDate) &&
                    (le.getReference() == null || !le.getReference().startsWith("SUPERSEDED-"))
            )
            .toList();

        for (LedgerEntry entry : previousEntries) {
            // Mark as superseded for audit trail
            String originalReference = entry.getReference() != null ? entry.getReference() : "EOD-" + tradeDate;
            entry.setReference("SUPERSEDED-" + originalReference);
            entry.setRemarks((entry.getRemarks() != null ? entry.getRemarks() + "; " : "") + "Superseded by re-run on " + Instant.now());
            ledgerEntryRepository.save(entry);

            // Adjust account balance by reversing the previous entry
            BigDecimal adjustment = entry.getType() == LedgerEntryType.EOD_MTM_CREDIT ? entry.getAmount().negate() : entry.getAmount();
            account.setBalance(account.getBalance().add(adjustment));
        }
        if (!previousEntries.isEmpty()) {
            tradingAccountRepository.save(account);
        }
    }

    private void postEodLedgerEntry(TradingAccount account, BigDecimal netPnl, LocalDate tradeDate, SettlementBatch batch) {
        LedgerEntry entry = new LedgerEntry();
        entry.setTradingAccount(account);
        entry.setType(netPnl.compareTo(ZERO) >= 0 ? LedgerEntryType.EOD_MTM_CREDIT : LedgerEntryType.EOD_MTM_DEBIT);
        entry.setAmount(netPnl.abs());
        entry.setCcy(account.getBaseCcy());
        entry.setDescription("EOD MTM P&L for " + tradeDate + " (Batch: " + batch.getId() + ")");
        entry.setCreatedAt(Instant.now());
        entry.setReference("EOD-" + tradeDate);

        // Calculate balance after
        BigDecimal currentBalance = account.getBalance();
        BigDecimal newBalance = currentBalance.add(netPnl);
        entry.setBalanceAfter(newBalance);
        account.setBalance(newBalance);

        ledgerEntryRepository.save(entry);
        tradingAccountRepository.save(account);
    }

    private void createReportLinks(SettlementBatch batch, LocalDate tradeDate, Set<TradingAccount> accounts) {
        // Supersede previous report links for this date
        List<ReportLink> previousLinks = reportLinkRepository.findByRefDateAndReportType(tradeDate, "TRADER_STATEMENT");
        for (ReportLink oldLink : previousLinks) {
            reportLinkRepository.delete(oldLink);
        }

        // Create report links for each account (placeholder - will be enhanced in Phase 4)
        for (TradingAccount account : accounts) {
            ReportLink link = new ReportLink();
            link.setRefDate(tradeDate);
            link.setReportType("TRADER_STATEMENT");
            link.setRelativeUrl("/api/statements?accountId=" + account.getId() + "&date=" + tradeDate);
            link.setSettlementBatch(batch);
            link.setTradingAccount(account);
            reportLinkRepository.save(link);
        }
    }
}
