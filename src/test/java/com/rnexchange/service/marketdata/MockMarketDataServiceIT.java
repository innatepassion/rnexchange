package com.rnexchange.service.marketdata;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.domain.enumeration.ExchangeStatus;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.repository.MarketHolidayRepository;
import com.rnexchange.service.dto.ExchangeStatusDTO;
import com.rnexchange.service.dto.FeedState;
import com.rnexchange.service.dto.FeedStatusDTO;
import com.rnexchange.service.marketdata.events.FeedStartedEvent;
import com.rnexchange.service.marketdata.events.FeedStoppedEvent;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@IntegrationTest
@RecordApplicationEvents
@Timeout(value = 30, unit = java.util.concurrent.TimeUnit.SECONDS)
class MockMarketDataServiceIT extends com.rnexchange.service.seed.AbstractBaselineSeedIT {

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private MarketHolidayRepository marketHolidayRepository;

    @Autowired(required = false)
    private MockMarketDataService mockMarketDataService;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void assertServicePresent() {
        assertThat(mockMarketDataService).as("MockMarketDataService bean must be provided before running integration tests").isNotNull();
        mockMarketDataService.stop();
        instrumentRepository.deleteAll();
        exchangeRepository.deleteAll();
        marketHolidayRepository.deleteAll();
        applicationEvents.clear();
    }

    @Test
    void startInitialisesFeedStateAndPublishesEvent() {
        seedInstrument("RELIANCE", "NSE");

        mockMarketDataService.stop();
        FeedStatusDTO statusBefore = mockMarketDataService.getStatus();
        assertThat(statusBefore.globalState()).isEqualTo(FeedState.STOPPED);

        mockMarketDataService.start();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> mockMarketDataService.getStatus().globalState() == FeedState.RUNNING);

        FeedStatusDTO statusAfter = mockMarketDataService.getStatus();
        assertThat(statusAfter.startedAt()).isNotNull();
        assertThat(applicationEvents.stream(FeedStartedEvent.class).count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void stopTransitionsStateAndPublishesEvent() {
        seedInstrument("INFY", "NSE");

        mockMarketDataService.start();
        mockMarketDataService.stop();

        FeedStatusDTO status = mockMarketDataService.getStatus();
        assertThat(status.globalState()).isEqualTo(FeedState.STOPPED);
        assertThat(applicationEvents.stream(FeedStoppedEvent.class).count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void startIsIdempotent() {
        seedInstrument("SBIN", "NSE");

        mockMarketDataService.start();
        Instant firstStarted = mockMarketDataService.getStatus().startedAt();

        mockMarketDataService.start();
        Instant secondStarted = mockMarketDataService.getStatus().startedAt();

        assertThat(secondStarted).isEqualTo(firstStarted);
    }

    @Test
    void refusesToStartWhenAllExchangesClosed() {
        seedInstrument("HDFCBANK", "BSE");
        insertHoliday("BSE", LocalDate.now());

        mockMarketDataService.start();

        FeedStatusDTO status = mockMarketDataService.getStatus();
        assertThat(status.globalState()).isEqualTo(FeedState.STOPPED);
    }

    @Test
    void ticksPerSecondMetricsReflectLoad() {
        seedInstrument("TCS", "NSE");

        mockMarketDataService.start();
        mockMarketDataService.triggerTickGeneration();

        generateTicksRepeated(5);
        FeedStatusDTO status = mockMarketDataService.getStatus();
        assertThat(status.exchanges().stream().anyMatch(e -> e.ticksPerSecond() > 0)).isTrue();
    }

    @Test
    void volatilityGuardSurfacesInStatus() {
        seedInstrument("ITC", "NSE");

        mockMarketDataService.start();
        mockMarketDataService.triggerTickGeneration();

        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .until(() ->
                mockMarketDataService.getStatus().exchanges().stream().anyMatch(exchangeStatus -> exchangeStatus.lastTickTime() != null)
            );

        assertThat(mockMarketDataService.getVolatilitySnapshots()).isNotEmpty();
    }

    @Test
    void holidayExchangeRemainsInactiveWhileOthersTick() {
        // Insert instruments and holiday in a separate committed transaction so MockMarketDataService can see them
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            seedInstrument("NSESTAR", "NSE");
            seedInstrument("BSESTAR", "BSE");
            insertHoliday("NSE", LocalDate.now(Clock.systemUTC()));
            entityManager.flush();
        });
        entityManager.clear();

        transactionTemplate.executeWithoutResult(status ->
            assertThat(marketHolidayRepository.findAllByExchange_CodeAndTradeDateAndIsHolidayTrue("NSE", LocalDate.now())).hasSize(1)
        );

        transactionTemplate.executeWithoutResult(status -> {
            mockMarketDataService.start();
            mockMarketDataService.triggerTickGeneration();
        });

        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .until(() ->
                mockMarketDataService
                    .getStatus()
                    .exchanges()
                    .stream()
                    .anyMatch(status -> "BSE".equals(status.exchangeCode()) && status.ticksPerSecond() > 0)
            );

        FeedStatusDTO status = mockMarketDataService.getStatus();
        ExchangeStatusDTO nseStatus = status
            .exchanges()
            .stream()
            .filter(exchangeStatus -> "NSE".equals(exchangeStatus.exchangeCode()))
            .findFirst()
            .orElseThrow();
        ExchangeStatusDTO bseStatus = status
            .exchanges()
            .stream()
            .filter(exchangeStatus -> "BSE".equals(exchangeStatus.exchangeCode()))
            .findFirst()
            .orElseThrow();

        assertThat(nseStatus.state()).isEqualTo(FeedState.HOLIDAY);
        assertThat(nseStatus.ticksPerSecond()).isEqualTo(0);
        assertThat(bseStatus.state()).isEqualTo(FeedState.RUNNING);
        assertThat(bseStatus.ticksPerSecond()).isGreaterThan(0);
    }

    @Test
    void restartAfterNewHolidayPreventsImpactedExchangeFromTicking() {
        // Commit instruments in a separate transaction so MockMarketDataService can see them
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            seedInstrument("NSE_RESTART", "NSE");
            seedInstrument("BSE_RESTART", "BSE");
            entityManager.flush();
        });
        entityManager.clear();

        transactionTemplate.executeWithoutResult(status -> {
            mockMarketDataService.start();
            mockMarketDataService.triggerTickGeneration();
        });
        generateTicksRepeated(3);
        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .until(() ->
                mockMarketDataService
                    .getStatus()
                    .exchanges()
                    .stream()
                    .anyMatch(status -> "NSE".equals(status.exchangeCode()) && status.ticksPerSecond() > 0)
            );
        mockMarketDataService.stop();

        // Insert holiday in a separate committed transaction so MockMarketDataService can see it
        transactionTemplate.executeWithoutResult(status -> {
            insertHoliday("NSE", LocalDate.now(Clock.systemUTC()));
            entityManager.flush();
        });
        entityManager.clear();
        transactionTemplate.executeWithoutResult(status ->
            assertThat(
                marketHolidayRepository.findAllByExchange_CodeAndTradeDateAndIsHolidayTrue("NSE", LocalDate.now(Clock.systemUTC()))
            ).hasSize(1)
        );

        transactionTemplate.executeWithoutResult(status -> {
            mockMarketDataService.start();
            mockMarketDataService.triggerTickGeneration();
        });
        generateTicksRepeated(5);

        FeedStatusDTO status = mockMarketDataService.getStatus();
        ExchangeStatusDTO nseStatus = status
            .exchanges()
            .stream()
            .filter(exchangeStatus -> "NSE".equals(exchangeStatus.exchangeCode()))
            .findFirst()
            .orElseThrow();
        assertThat(nseStatus.state()).isEqualTo(FeedState.HOLIDAY);
        assertThat(nseStatus.ticksPerSecond()).isEqualTo(0);
        ExchangeStatusDTO bseStatus = status
            .exchanges()
            .stream()
            .filter(exchangeStatus -> "BSE".equals(exchangeStatus.exchangeCode()))
            .findFirst()
            .orElseThrow();
        assertThat(bseStatus.state()).isEqualTo(FeedState.RUNNING);
    }

    private void seedInstrument(String symbol, String exchangeCode) {
        com.rnexchange.domain.Exchange exchange = exchangeRepository
            .findOneByCode(exchangeCode)
            .orElseGet(() ->
                exchangeRepository.save(
                    new com.rnexchange.domain.Exchange()
                        .code(exchangeCode)
                        .name(exchangeCode + " Exchange")
                        .timezone("Asia/Kolkata")
                        .status(ExchangeStatus.ACTIVE)
                )
            );

        com.rnexchange.domain.Instrument instrument = new com.rnexchange.domain.Instrument()
            .symbol(symbol)
            .name(symbol + " Ltd")
            .assetClass(AssetClass.EQUITY)
            .exchangeCode(exchangeCode)
            .tickSize(java.math.BigDecimal.valueOf(0.05))
            .lotSize(1L)
            .currency(Currency.INR)
            .status("ACTIVE")
            .exchange(exchange);

        instrumentRepository.save(instrument);
    }

    private void generateTicksRepeated(int times) {
        for (int i = 0; i < times; i++) {
            mockMarketDataService.triggerTickGeneration();
        }
    }

    private void insertHoliday(String exchangeCode, LocalDate date) {
        com.rnexchange.domain.Exchange exchange = exchangeRepository
            .findOneByCode(exchangeCode)
            .orElseGet(() ->
                exchangeRepository.save(
                    new com.rnexchange.domain.Exchange()
                        .code(exchangeCode)
                        .name(exchangeCode + " Exchange")
                        .timezone("Asia/Kolkata")
                        .status(ExchangeStatus.ACTIVE)
                )
            );

        marketHolidayRepository.save(
            new com.rnexchange.domain.MarketHoliday().exchange(exchange).tradeDate(date).reason("Test Holiday").isHoliday(true)
        );
    }
}
