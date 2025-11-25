package com.rnexchange.integration.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.*;
import com.rnexchange.domain.enumeration.*;
import com.rnexchange.repository.*;
import com.rnexchange.service.seed.BaselineTruncateService;
import com.rnexchange.service.settlement.SettlementService;
import com.rnexchange.service.settlement.event.SettlementFailedEvent;
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
 * Integration test for EOD settlement failure path (User Story 1).
 * Verifies that when settlement prices are missing:
 * - Batch is marked as FAILED
 * - No partial updates are made (positions, ledgers remain unchanged)
 * - No report links are created
 * - Publishes SettlementFailedEvent
 */
@IntegrationTest
@RecordApplicationEvents
@Transactional
class SettlementEodFailureIT {

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

    @Autowired
    private BaselineTruncateService baselineTruncateService;

    private Exchange exchange;
    private Broker broker;
    private TraderProfile trader;
    private TradingAccount account;
    private Instrument instrument;
    private Position position;
    private LocalDate tradeDate;

    @BeforeEach
    void setUp() {
        baselineTruncateService.cleanup();
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
            .filter(i -> "MISSING_PRICE".equals(i.getSymbol()))
            .findFirst()
            .orElseGet(() -> {
                Instrument inst = new Instrument();
                inst.setSymbol("MISSING_PRICE");
                inst.setName("Missing Price Instrument");
                inst.setAssetClass(AssetClass.EQUITY);
                inst.setExchangeCode("TEST");
                inst.setTickSize(BigDecimal.valueOf(0.01));
                inst.setLotSize(10L);
                inst.setCurrency(Currency.INR);
                inst.setStatus("active");
                inst.setExchange(exchange);
                return instrumentRepository.save(inst);
            });

        // Create position (but NO settlement price for this instrument)
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

        // Explicitly ensure NO settlement price exists for this instrument and date
        dailySettlementPriceRepository
            .findByRefDateAndInstrument_Id(tradeDate, instrument.getId())
            .ifPresent(dailySettlementPriceRepository::delete);
    }

    @Test
    void shouldFailEodWhenSettlementPriceIsMissing() {
        // Given: Initial state
        BigDecimal initialBalance = account.getBalance();
        BigDecimal initialLastPx = position.getLastPx();
        BigDecimal initialUnrealizedPnl = position.getUnrealizedPnl();
        int initialLedgerEntryCount = ledgerEntryRepository.findAll().size();

        // When: Run EOD (should fail due to missing price)
        assertThatThrownBy(() -> settlementService.runEod(tradeDate))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("No settlement price found");

        // Then: Verify batch was created with FAILED status
        List<SettlementBatch> batches = settlementBatchRepository
            .findAll()
            .stream()
            .filter(b -> b.getRefDate().equals(tradeDate) && b.getKind() == SettlementKind.EOD)
            .toList();
        assertThat(batches).isNotEmpty();
        SettlementBatch batch = batches.stream().max((a, b) -> Long.compare(a.getId(), b.getId())).orElseThrow();
        assertThat(batch.getStatus()).isEqualTo(SettlementStatus.FAILED);
        assertThat(batch.getRemarks()).contains("error");

        // Verify position was NOT updated
        Position unchangedPosition = positionRepository.findById(position.getId()).orElseThrow();
        assertThat(unchangedPosition.getLastPx()).isEqualByComparingTo(initialLastPx);
        assertThat(unchangedPosition.getUnrealizedPnl()).isEqualByComparingTo(initialUnrealizedPnl);

        // Verify NO EOD ledger entries were created
        List<LedgerEntry> eodEntries = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(
                le ->
                    le.getTradingAccount() != null &&
                    le.getTradingAccount().getId().equals(account.getId()) &&
                    (le.getType() == LedgerEntryType.EOD_MTM_CREDIT || le.getType() == LedgerEntryType.EOD_MTM_DEBIT) &&
                    le.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().equals(tradeDate)
            )
            .toList();
        assertThat(eodEntries).isEmpty();

        // Verify account balance was NOT changed
        TradingAccount unchangedAccount = tradingAccountRepository.findById(account.getId()).orElseThrow();
        assertThat(unchangedAccount.getBalance()).isEqualByComparingTo(initialBalance);

        // Verify NO report links were created
        List<ReportLink> reportLinks = reportLinkRepository.findByRefDateAndReportType(tradeDate, "TRADER_STATEMENT");
        assertThat(reportLinks).isEmpty();

        // Verify SettlementFailedEvent was published
        long eventCount = applicationEvents.stream(SettlementFailedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        SettlementFailedEvent event = applicationEvents.stream(SettlementFailedEvent.class).findFirst().orElseThrow();
        assertThat(event.getBatchId()).isEqualTo(batch.getId());
        assertThat(event.getTradeDate()).isEqualTo(tradeDate);
        assertThat(event.getErrorMessage()).contains("No settlement price found");
    }

    @Test
    void shouldFailEodWhenMultipleInstrumentsHaveMissingPrices() {
        // Create second instrument without price
        Instrument instrument2 = new Instrument();
        instrument2.setSymbol("MISSING_PRICE_2");
        instrument2.setName("Missing Price Instrument 2");
        instrument2.setAssetClass(AssetClass.EQUITY);
        instrument2.setExchangeCode("TEST");
        instrument2.setTickSize(BigDecimal.valueOf(0.01));
        instrument2.setLotSize(10L);
        instrument2.setCurrency(Currency.INR);
        instrument2.setStatus("active");
        instrument2.setExchange(exchange);
        instrument2 = instrumentRepository.save(instrument2);

        Position position2 = new Position();
        position2.setTradingAccount(account);
        position2.setInstrument(instrument2);
        position2.setQty(BigDecimal.valueOf(50));
        position2.setAvgCost(BigDecimal.valueOf(60.00));
        position2.setLastPx(BigDecimal.valueOf(60.00));
        position2.setUnrealizedPnl(BigDecimal.ZERO);
        position2.setRealizedPnl(BigDecimal.ZERO);
        position2 = positionRepository.save(position2);

        // Ensure no price for instrument2
        dailySettlementPriceRepository
            .findByRefDateAndInstrument_Id(tradeDate, instrument2.getId())
            .ifPresent(dailySettlementPriceRepository::delete);

        // Run EOD (should fail on first missing price)
        assertThatThrownBy(() -> settlementService.runEod(tradeDate))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("No settlement price found");

        // Verify batch is FAILED
        List<SettlementBatch> batches = settlementBatchRepository
            .findAll()
            .stream()
            .filter(b -> b.getRefDate().equals(tradeDate) && b.getKind() == SettlementKind.EOD)
            .toList();
        SettlementBatch batch = batches.stream().max((a, b) -> Long.compare(a.getId(), b.getId())).orElseThrow();
        assertThat(batch.getStatus()).isEqualTo(SettlementStatus.FAILED);

        // Verify no positions were updated
        Position unchangedPos1 = positionRepository.findById(position.getId()).orElseThrow();
        Position unchangedPos2 = positionRepository.findById(position2.getId()).orElseThrow();
        assertThat(unchangedPos1.getLastPx()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(unchangedPos2.getLastPx()).isEqualByComparingTo(new BigDecimal("60.00"));
    }
}
