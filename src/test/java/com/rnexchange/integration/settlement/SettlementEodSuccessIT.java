package com.rnexchange.integration.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.*;
import com.rnexchange.domain.enumeration.*;
import com.rnexchange.repository.*;
import com.rnexchange.service.settlement.SettlementService;
import com.rnexchange.service.settlement.event.SettlementCompletedEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for successful EOD settlement run (User Story 1).
 * Verifies that EOD run:
 * - Creates a settlement batch with status PROCESSED
 * - Updates positions (lastPx, unrealizedPnl)
 * - Posts ledger entries (EOD_MTM_CREDIT/DEBIT)
 * - Creates report links
 * - Publishes SettlementCompletedEvent
 */
@IntegrationTest
@RecordApplicationEvents
@Transactional
class SettlementEodSuccessIT {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private SettlementBatchRepository settlementBatchRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private TradingAccountRepository tradingAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private TraderProfileRepository traderProfileRepository;

    @Autowired
    private DailySettlementPriceRepository dailySettlementPriceRepository;

    @Autowired
    private ReportLinkRepository reportLinkRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    private Exchange exchange;
    private Broker broker;
    private TraderProfile trader;
    private TradingAccount account;
    private Instrument instrument;
    private Position position;
    private DailySettlementPrice settlementPrice;
    private LocalDate tradeDate;

    @BeforeEach
    void setUp() {
        tradeDate = LocalDate.now();

        // Create exchange
        exchange = exchangeRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                Exchange e = new Exchange();
                e.setCode("TEST");
                e.setName("Test Exchange");
                e.setTimezone("Asia/Kolkata");
                e.setStatus(ExchangeStatus.ACTIVE);
                return exchangeRepository.save(e);
            });

        // Create broker
        broker = brokerRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                Broker b = new Broker();
                b.setCode("TEST_BROKER");
                b.setName("Test Broker");
                b.setStatus("ACTIVE");
                return brokerRepository.save(b);
            });

        // Create trader
        trader = traderProfileRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                TraderProfile tp = new TraderProfile();
                tp.setDisplayName("Test Trader");
                tp.setEmail("test@example.com");
                tp.setKycStatus(KycStatus.APPROVED);
                tp.setStatus(AccountStatus.ACTIVE);
                return traderProfileRepository.save(tp);
            });

        // Create trading account
        account = tradingAccountRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                TradingAccount ta = new TradingAccount();
                ta.setType(AccountType.CASH);
                ta.setBaseCcy(Currency.INR);
                ta.setBalance(BigDecimal.valueOf(10000.00));
                ta.setStatus(AccountStatus.ACTIVE);
                ta.setBroker(broker);
                ta.setTrader(trader);
                return tradingAccountRepository.save(ta);
            });

        // Create instrument
        instrument = instrumentRepository
            .findAll()
            .stream()
            .filter(i -> "TEST".equals(i.getSymbol()))
            .findFirst()
            .orElseGet(() -> {
                Instrument inst = new Instrument();
                inst.setSymbol("TEST");
                inst.setName("Test Instrument");
                inst.setAssetClass(AssetClass.EQUITY);
                inst.setExchangeCode("TEST");
                inst.setTickSize(BigDecimal.valueOf(0.01));
                inst.setLotSize(10L);
                inst.setCurrency(Currency.INR);
                inst.setStatus("active");
                inst.setExchange(exchange);
                return instrumentRepository.save(inst);
            });

        // Create position
        position = positionRepository
            .findAll()
            .stream()
            .filter(p -> p.getTradingAccount().getId().equals(account.getId()) && p.getInstrument().getId().equals(instrument.getId()))
            .findFirst()
            .orElseGet(() -> {
                Position pos = new Position();
                pos.setTradingAccount(account);
                pos.setInstrument(instrument);
                pos.setQty(BigDecimal.valueOf(100));
                pos.setAvgCost(BigDecimal.valueOf(50.00));
                pos.setLastPx(BigDecimal.valueOf(50.00));
                pos.setUnrealizedPnl(BigDecimal.ZERO);
                pos.setRealizedPnl(BigDecimal.ZERO);
                return positionRepository.save(pos);
            });

        // Create settlement price
        settlementPrice = dailySettlementPriceRepository
            .findByRefDateAndInstrument_Id(tradeDate, instrument.getId())
            .orElseGet(() -> {
                DailySettlementPrice price = new DailySettlementPrice();
                price.setRefDate(tradeDate);
                price.setInstrumentSymbol(instrument.getSymbol());
                price.setSettlePrice(BigDecimal.valueOf(55.00));
                price.setInstrument(instrument);
                return dailySettlementPriceRepository.save(price);
            });
    }

    @Test
    void shouldSuccessfullyRunEodAndUpdatePositionsAndLedgers() {
        // Given: Initial state
        BigDecimal initialBalance = account.getBalance();
        BigDecimal initialLastPx = position.getLastPx();
        BigDecimal settlePrice = settlementPrice.getSettlePrice();

        // When: Run EOD
        settlementService.runEod(tradeDate);

        // Then: Verify batch was created with PROCESSED status
        List<SettlementBatch> batches = settlementBatchRepository
            .findAll()
            .stream()
            .filter(b -> b.getRefDate().equals(tradeDate) && b.getKind() == SettlementKind.EOD)
            .toList();
        assertThat(batches).isNotEmpty();
        SettlementBatch batch = batches.stream().max((a, b) -> Long.compare(a.getId(), b.getId())).orElseThrow();
        assertThat(batch.getStatus()).isEqualTo(SettlementStatus.PROCESSED);
        assertThat(batch.getRemarks()).contains("accountsProcessed");
        assertThat(batch.getRemarks()).contains("positionsProcessed");

        // Verify position was updated
        Position updatedPosition = positionRepository.findById(position.getId()).orElseThrow();
        assertThat(updatedPosition.getLastPx()).isEqualByComparingTo(settlePrice);
        // MTM = (55 - 50) * 100 = 500
        BigDecimal expectedMtm = new BigDecimal("500.00");
        assertThat(updatedPosition.getUnrealizedPnl()).isEqualByComparingTo(expectedMtm);

        // Verify ledger entry was created
        List<LedgerEntry> eodEntries = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(
                le ->
                    le.getTradingAccount() != null &&
                    le.getTradingAccount().getId().equals(account.getId()) &&
                    (le.getType() == LedgerEntryType.EOD_MTM_CREDIT || le.getType() == LedgerEntryType.EOD_MTM_DEBIT) &&
                    le.getCreatedAt().toLocalDate().equals(tradeDate) &&
                    (le.getReference() == null || !le.getReference().startsWith("SUPERSEDED-"))
            )
            .toList();
        assertThat(eodEntries).hasSize(1);
        LedgerEntry eodEntry = eodEntries.get(0);
        assertThat(eodEntry.getType()).isEqualTo(LedgerEntryType.EOD_MTM_CREDIT);
        assertThat(eodEntry.getAmount()).isEqualByComparingTo(expectedMtm);
        assertThat(eodEntry.getDescription()).contains(tradeDate.toString());

        // Verify account balance was updated
        TradingAccount updatedAccount = tradingAccountRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(initialBalance.add(expectedMtm));

        // Verify report links were created
        List<ReportLink> reportLinks = reportLinkRepository.findByRefDateAndReportType(tradeDate, "TRADER_STATEMENT");
        assertThat(reportLinks).isNotEmpty();

        // Verify SettlementCompletedEvent was published
        long eventCount = applicationEvents.stream(SettlementCompletedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        SettlementCompletedEvent event = applicationEvents.stream(SettlementCompletedEvent.class).findFirst().orElseThrow();
        assertThat(event.getBatchId()).isEqualTo(batch.getId());
        assertThat(event.getTradeDate()).isEqualTo(tradeDate);
        assertThat(event.getAccountsProcessed()).isEqualTo(1);
        assertThat(event.getPositionsProcessed()).isEqualTo(1);
        assertThat(event.getNetPnl()).isEqualByComparingTo(expectedMtm);
    }

    @Test
    void shouldHandleMultipleAccountsAndPositions() {
        // Create second account and position
        TradingAccount account2 = new TradingAccount();
        account2.setType(AccountType.CASH);
        account2.setBaseCcy(Currency.INR);
        account2.setBalance(BigDecimal.valueOf(5000.00));
        account2.setStatus(AccountStatus.ACTIVE);
        account2.setBroker(broker);
        account2.setTrader(trader);
        account2 = tradingAccountRepository.save(account2);

        Position position2 = new Position();
        position2.setTradingAccount(account2);
        position2.setInstrument(instrument);
        position2.setQty(BigDecimal.valueOf(50));
        position2.setAvgCost(BigDecimal.valueOf(60.00));
        position2.setLastPx(BigDecimal.valueOf(60.00));
        position2.setUnrealizedPnl(BigDecimal.ZERO);
        position2.setRealizedPnl(BigDecimal.ZERO);
        position2 = positionRepository.save(position2);

        // Run EOD
        settlementService.runEod(tradeDate);

        // Verify both accounts were processed
        List<SettlementBatch> batches = settlementBatchRepository
            .findAll()
            .stream()
            .filter(b -> b.getRefDate().equals(tradeDate) && b.getKind() == SettlementKind.EOD)
            .toList();
        SettlementBatch batch = batches.stream().max((a, b) -> Long.compare(a.getId(), b.getId())).orElseThrow();
        assertThat(batch.getStatus()).isEqualTo(SettlementStatus.PROCESSED);

        // Verify both positions were updated
        Position updatedPos1 = positionRepository.findById(position.getId()).orElseThrow();
        Position updatedPos2 = positionRepository.findById(position2.getId()).orElseThrow();
        assertThat(updatedPos1.getLastPx()).isEqualByComparingTo(settlementPrice.getSettlePrice());
        assertThat(updatedPos2.getLastPx()).isEqualByComparingTo(settlementPrice.getSettlePrice());

        // Verify ledger entries for both accounts
        List<LedgerEntry> allEodEntries = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(
                le ->
                    le.getTradingAccount() != null &&
                    (le.getTradingAccount().getId().equals(account.getId()) || le.getTradingAccount().getId().equals(account2.getId())) &&
                    (le.getType() == LedgerEntryType.EOD_MTM_CREDIT || le.getType() == LedgerEntryType.EOD_MTM_DEBIT) &&
                    le.getCreatedAt().toLocalDate().equals(tradeDate) &&
                    (le.getReference() == null || !le.getReference().startsWith("SUPERSEDED-"))
            )
            .toList();
        assertThat(allEodEntries).hasSize(2);
    }
}
