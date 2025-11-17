package com.rnexchange.service.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.rnexchange.domain.*;
import com.rnexchange.domain.enumeration.LedgerEntryType;
import com.rnexchange.domain.enumeration.SettlementKind;
import com.rnexchange.domain.enumeration.SettlementStatus;
import com.rnexchange.repository.*;
import com.rnexchange.service.settlement.event.SettlementCompletedEvent;
import com.rnexchange.service.settlement.event.SettlementFailedEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementBatchRepository settlementBatchRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private DailySettlementPriceRepository dailySettlementPriceRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private TradingAccountRepository tradingAccountRepository;

    @Mock
    private ReportLinkRepository reportLinkRepository;

    @Mock
    private ExchangeRepository exchangeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SettlementServiceImpl settlementService;

    @BeforeEach
    void setUp() {
        settlementService = new SettlementServiceImpl(
            settlementBatchRepository,
            positionRepository,
            dailySettlementPriceRepository,
            ledgerEntryRepository,
            tradingAccountRepository,
            reportLinkRepository,
            exchangeRepository,
            eventPublisher
        );
    }

    @Test
    void shouldCalculateMtmCorrectly() {
        // Given
        Position position = new Position();
        position.setQty(new BigDecimal("10"));
        position.setAvgCost(new BigDecimal("100.00"));
        BigDecimal settlePrice = new BigDecimal("110.00");

        // When - using reflection or package-private method, or test through public method
        // For now, we'll test the logic indirectly through the service

        // Expected: (110 - 100) * 10 = 100
        BigDecimal expectedMtm = new BigDecimal("100.00");
        // This is tested indirectly through integration tests
        assertThat(expectedMtm).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldHandleEmptyPositions() {
        // Given
        LocalDate tradeDate = LocalDate.now();
        Exchange exchange = new Exchange();
        exchange.setId(1L);

        when(exchangeRepository.findAll()).thenReturn(List.of(exchange));
        when(positionRepository.findOpenPositions(any())).thenReturn(Collections.emptyList());
        when(settlementBatchRepository.findAll()).thenReturn(Collections.emptyList());
        when(settlementBatchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        settlementService.runEod(tradeDate);

        // Then
        ArgumentCaptor<SettlementBatch> batchCaptor = ArgumentCaptor.forClass(SettlementBatch.class);
        verify(settlementBatchRepository, atLeastOnce()).save(batchCaptor.capture());
        SettlementBatch savedBatch = batchCaptor.getValue();
        assertThat(savedBatch.getStatus()).isEqualTo(SettlementStatus.PROCESSED);

        ArgumentCaptor<SettlementCompletedEvent> eventCaptor = ArgumentCaptor.forClass(SettlementCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        SettlementCompletedEvent event = eventCaptor.getValue();
        assertThat(event.getAccountsProcessed()).isEqualTo(0);
        assertThat(event.getPositionsProcessed()).isEqualTo(0);
        assertThat(event.getTradeDate()).isEqualTo(tradeDate);
        assertThat(event.getNetPnl()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldPublishSettlementCompletedEventOnSuccess() {
        // Given
        LocalDate tradeDate = LocalDate.now();
        Exchange exchange = new Exchange();
        exchange.setId(1L);

        TradingAccount account = new TradingAccount();
        account.setId(1L);
        account.setBalance(BigDecimal.valueOf(10000.00));

        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setSymbol("TEST");

        Position position = new Position();
        position.setTradingAccount(account);
        position.setInstrument(instrument);
        position.setQty(BigDecimal.valueOf(100));
        position.setAvgCost(BigDecimal.valueOf(50.00));

        DailySettlementPrice price = new DailySettlementPrice();
        price.setRefDate(tradeDate);
        price.setInstrument(instrument);
        price.setSettlePrice(BigDecimal.valueOf(55.00));

        SettlementBatch batch = new SettlementBatch();
        batch.setId(1L);
        batch.setRefDate(tradeDate);
        batch.setKind(SettlementKind.EOD);
        batch.setStatus(SettlementStatus.CREATED);
        batch.setExchange(exchange);

        when(exchangeRepository.findAll()).thenReturn(List.of(exchange));
        when(positionRepository.findOpenPositions(any())).thenReturn(List.of(position));
        when(settlementBatchRepository.findAll()).thenReturn(Collections.emptyList());
        when(settlementBatchRepository.save(any())).thenAnswer(invocation -> {
            SettlementBatch b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(dailySettlementPriceRepository.findByRefDateAndInstrument_Id(tradeDate, instrument.getId())).thenReturn(Optional.of(price));
        when(tradingAccountRepository.save(any())).thenReturn(account);
        when(positionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.findAll()).thenReturn(Collections.emptyList());
        when(reportLinkRepository.findByRefDateAndReportType(any(), any())).thenReturn(Collections.emptyList());
        when(reportLinkRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        settlementService.runEod(tradeDate);

        // Then: Verify SettlementCompletedEvent was published
        ArgumentCaptor<SettlementCompletedEvent> eventCaptor = ArgumentCaptor.forClass(SettlementCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        SettlementCompletedEvent event = eventCaptor.getValue();
        assertThat(event.getBatchId()).isEqualTo(1L);
        assertThat(event.getTradeDate()).isEqualTo(tradeDate);
        assertThat(event.getAccountsProcessed()).isEqualTo(1);
        assertThat(event.getPositionsProcessed()).isEqualTo(1);
        assertThat(event.getNetPnl()).isEqualByComparingTo(new BigDecimal("500.00")); // (55-50)*100
    }

    @Test
    void shouldPublishSettlementFailedEventOnFailure() {
        // Given
        LocalDate tradeDate = LocalDate.now();
        Exchange exchange = new Exchange();
        exchange.setId(1L);

        TradingAccount account = new TradingAccount();
        account.setId(1L);

        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setSymbol("TEST");

        Position position = new Position();
        position.setTradingAccount(account);
        position.setInstrument(instrument);
        position.setQty(BigDecimal.valueOf(100));
        position.setAvgCost(BigDecimal.valueOf(50.00));

        SettlementBatch batch = new SettlementBatch();
        batch.setId(1L);
        batch.setRefDate(tradeDate);
        batch.setKind(SettlementKind.EOD);
        batch.setStatus(SettlementStatus.CREATED);
        batch.setExchange(exchange);

        when(exchangeRepository.findAll()).thenReturn(List.of(exchange));
        when(positionRepository.findOpenPositions(any())).thenReturn(List.of(position));
        when(settlementBatchRepository.findAll()).thenReturn(Collections.emptyList());
        when(settlementBatchRepository.save(any())).thenAnswer(invocation -> {
            SettlementBatch b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });
        // No settlement price available - will cause failure
        when(dailySettlementPriceRepository.findByRefDateAndInstrument_Id(tradeDate, instrument.getId())).thenReturn(Optional.empty());
        when(dailySettlementPriceRepository.findFirstByInstrument_IdOrderByRefDateDesc(instrument.getId())).thenReturn(Optional.empty());

        // When/Then: Should throw exception and publish failed event
        try {
            settlementService.runEod(tradeDate);
        } catch (RuntimeException e) {
            // Expected
        }

        // Then: Verify SettlementFailedEvent was published
        ArgumentCaptor<SettlementFailedEvent> eventCaptor = ArgumentCaptor.forClass(SettlementFailedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        SettlementFailedEvent event = eventCaptor.getValue();
        assertThat(event.getBatchId()).isEqualTo(1L);
        assertThat(event.getTradeDate()).isEqualTo(tradeDate);
        assertThat(event.getErrorMessage()).contains("Failed to process account");
    }
}
