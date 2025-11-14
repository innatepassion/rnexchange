package com.rnexchange.service.marketdata;

import com.rnexchange.config.MockMarketDataProperties;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.MarketHoliday;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.repository.MarketHolidayRepository;
import com.rnexchange.service.dto.BarDTO;
import com.rnexchange.service.dto.ExchangeStatusDTO;
import com.rnexchange.service.dto.FeedState;
import com.rnexchange.service.dto.FeedStatusDTO;
import com.rnexchange.service.dto.QuoteDTO;
import com.rnexchange.service.marketdata.RollingMinuteVolatilityGuard.GuardSnapshot;
import com.rnexchange.service.marketdata.events.FeedStartedEvent;
import com.rnexchange.service.marketdata.events.FeedStoppedEvent;
import com.rnexchange.web.websocket.MarketDataWebSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MockMarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MockMarketDataService.class);

    private static final int DEFAULT_INTERVAL_MILLIS = 750;
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("100.00");

    private final InstrumentRepository instrumentRepository;
    private final MarketHolidayRepository marketHolidayRepository;
    private final MarketDataWebSocketHandler webSocketHandler;
    private final RollingMinuteVolatilityGuard volatilityGuard;
    private final BarAggregator barAggregator;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    private final Map<String, InstrumentState> instrumentStates = new ConcurrentHashMap<>();
    private final Map<String, ExchangeMetrics> exchangeMetrics = new ConcurrentHashMap<>();
    private final Map<String, PriceGenerator> priceGenerators = new ConcurrentHashMap<>();
    /**
     * Pending quotes to be broadcast in batches. This enables high tick rates while
     * keeping WebSocket send frequency bounded, as described in research.md §6.1.
     */
    private final Queue<QuoteDTO> pendingQuotes = new ConcurrentLinkedQueue<>();
    private final AtomicReference<FeedState> feedState = new AtomicReference<>(FeedState.STOPPED);
    private volatile Instant startedAt;
    private volatile ScheduledFuture<?> generatorTask;
    private volatile ScheduledFuture<?> flushTask;
    private volatile ScheduledFuture<?> barTask;
    private final ScheduledExecutorService scheduler;
    private final int barIntervalSeconds;
    private volatile Set<String> lastClosedExchanges = Collections.emptySet();

    @org.springframework.beans.factory.annotation.Autowired
    public MockMarketDataService(
        InstrumentRepository instrumentRepository,
        MarketHolidayRepository marketHolidayRepository,
        MarketDataWebSocketHandler webSocketHandler,
        RollingMinuteVolatilityGuard volatilityGuard,
        ApplicationEventPublisher eventPublisher,
        BarAggregator barAggregator,
        MockMarketDataProperties properties
    ) {
        this(
            instrumentRepository,
            marketHolidayRepository,
            webSocketHandler,
            volatilityGuard,
            eventPublisher,
            barAggregator,
            properties,
            Clock.systemUTC()
        );
    }

    MockMarketDataService(
        InstrumentRepository instrumentRepository,
        MarketHolidayRepository marketHolidayRepository,
        MarketDataWebSocketHandler webSocketHandler,
        RollingMinuteVolatilityGuard volatilityGuard,
        ApplicationEventPublisher eventPublisher,
        BarAggregator barAggregator,
        MockMarketDataProperties properties,
        Clock clock
    ) {
        this.instrumentRepository = Objects.requireNonNull(instrumentRepository, "instrumentRepository must not be null");
        this.marketHolidayRepository = Objects.requireNonNull(marketHolidayRepository, "marketHolidayRepository must not be null");
        this.webSocketHandler = Objects.requireNonNull(webSocketHandler, "webSocketHandler must not be null");
        this.volatilityGuard = Objects.requireNonNull(volatilityGuard, "volatilityGuard must not be null");
        this.barAggregator = Objects.requireNonNull(barAggregator, "barAggregator must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("mock-marketdata"));
        int configuredBarInterval = properties != null ? properties.getBarIntervalSeconds() : 60;
        this.barIntervalSeconds = Math.max(1, configuredBarInterval);
    }

    @PostConstruct
    void onStartup() {
        log.info("MockMarketDataService initialised; feed state={}", feedState.get());
    }

    @PreDestroy
    void onShutdown() {
        stop();
        scheduler.shutdownNow();
    }

    @Transactional(readOnly = true)
    public FeedStatusDTO getStatus() {
        FeedState currentState = feedState.get();
        Set<String> exchanges = instrumentStates.values().stream().map(InstrumentState::getExchangeCode).collect(Collectors.toSet());

        List<ExchangeStatusDTO> exchangeStatus = exchanges.stream().map(code -> buildExchangeStatus(code, currentState)).toList();

        return new FeedStatusDTO(currentState, startedAt, exchangeStatus);
    }

    public synchronized void start() {
        if (feedState.get() == FeedState.RUNNING) {
            log.debug("Mock market data feed already running; ignoring start request");
            return;
        }

        Map<String, InstrumentState> loadedStates = loadInstrumentStates();
        if (loadedStates.isEmpty()) {
            log.warn("No eligible instruments available for mock feed; staying in STOPPED state");
            feedState.set(FeedState.STOPPED);
            return;
        }

        Set<String> exchanges = loadedStates.values().stream().map(InstrumentState::getExchangeCode).collect(Collectors.toSet());
        Set<String> closedExchanges = findClosedExchanges(exchanges);
        if (closedExchanges.size() == exchanges.size()) {
            log.info("All exchanges are closed today; mock feed will remain stopped");
            feedState.set(FeedState.STOPPED);
            lastClosedExchanges = closedExchanges;
            return;
        }

        instrumentStates.clear();
        instrumentStates.putAll(loadedStates);
        exchangeMetrics.clear();
        exchanges.forEach(code -> exchangeMetrics.put(code, new ExchangeMetrics()));
        feedState.set(FeedState.RUNNING);
        startedAt = clock.instant();
        lastClosedExchanges = closedExchanges;

        scheduleGenerator();
        eventPublisher.publishEvent(new FeedStartedEvent(new ArrayList<>(exchanges), "manual", startedAt));
        log.info("Mock market data feed started with {} instruments across {} exchanges", instrumentStates.size(), exchanges.size());
    }

    public synchronized void stop() {
        if (feedState.get() == FeedState.STOPPED) {
            log.debug("Mock market data feed already stopped; ignoring stop request");
            return;
        }
        if (generatorTask != null) {
            generatorTask.cancel(false);
            generatorTask = null;
        }
        if (flushTask != null) {
            flushTask.cancel(false);
            flushTask = null;
        }
        if (barTask != null) {
            barTask.cancel(false);
            barTask = null;
        }
        feedState.set(FeedState.STOPPED);
        eventPublisher.publishEvent(new FeedStoppedEvent(List.copyOf(exchangeMetrics.keySet()), "manual", clock.instant(), "STOP_INVOKED"));
        log.info("Mock market data feed stopped");
    }

    public Map<String, GuardSnapshot> getVolatilitySnapshots() {
        return instrumentStates
            .keySet()
            .stream()
            .map(symbol -> Map.entry(symbol, volatilityGuard.snapshot(symbol)))
            .filter(entry -> entry.getValue().isPresent())
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    private Map<String, InstrumentState> loadInstrumentStates() {
        List<Instrument> instruments = instrumentRepository.findAll();
        return instruments
            .stream()
            .filter(instrument -> "ACTIVE".equalsIgnoreCase(instrument.getStatus()))
            .collect(Collectors.toMap(Instrument::getSymbol, this::createInstrumentState));
    }

    private InstrumentState createInstrumentState(Instrument instrument) {
        BigDecimal openPrice = DEFAULT_PRICE;
        InstrumentState state = new InstrumentState(instrument.getSymbol(), instrument.getExchangeCode(), openPrice, 0.01);
        BigDecimal minPrice = openPrice.multiply(new BigDecimal("0.50"));
        BigDecimal maxPrice = openPrice.multiply(new BigDecimal("1.50"));
        priceGenerators.put(instrument.getSymbol(), new PriceGenerator(minPrice, maxPrice, 1));
        return state;
    }

    private void scheduleGenerator() {
        if (generatorTask != null && !generatorTask.isCancelled()) {
            generatorTask.cancel(false);
        }
        generatorTask = scheduler.scheduleAtFixedRate(this::generateTicksSafe, 0, DEFAULT_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
        if (flushTask != null && !flushTask.isCancelled()) {
            flushTask.cancel(false);
        }
        // Flush quote batches every 100ms, deduplicating by symbol to keep latency low
        // while dramatically reducing WebSocket send volume.
        flushTask = scheduler.scheduleAtFixedRate(this::flushQuotesSafe, 100, 100, TimeUnit.MILLISECONDS);
        if (barTask != null && !barTask.isCancelled()) {
            barTask.cancel(false);
        }
        barTask = scheduler.scheduleAtFixedRate(this::broadcastBarsSafe, barIntervalSeconds, barIntervalSeconds, TimeUnit.SECONDS);
    }

    private void generateTicksSafe() {
        try {
            generateTicks();
        } catch (Exception ex) {
            log.error("Failed to generate mock market data tick", ex);
        }
    }

    private void generateTicks() {
        if (feedState.get() != FeedState.RUNNING) {
            return;
        }

        Set<String> exchanges = instrumentStates.values().stream().map(InstrumentState::getExchangeCode).collect(Collectors.toSet());
        Set<String> closedExchanges = findClosedExchanges(exchanges);
        lastClosedExchanges = closedExchanges;

        instrumentStates
            .values()
            .forEach(state -> {
                if (closedExchanges.contains(state.getExchangeCode())) {
                    return;
                }
                PriceGenerator generator = priceGenerators.getOrDefault(state.getSymbol(), new PriceGenerator());
                BigDecimal nextPrice = generator.nextPrice(state.getLastPrice(), state.getSessionOpen(), state.getVolatility());
                long volume = ThreadLocalRandom.current().nextLong(1, 500);
                state.updateWithTick(nextPrice, volume);
                volatilityGuard.register(state.getSymbol(), state.getSessionOpen(), nextPrice);
                QuoteDTO quote = new QuoteDTO(
                    state.getSymbol(),
                    state.getLastPrice(),
                    state.getSessionOpen(),
                    state.getChange(),
                    state.getChangePercent(),
                    state.getCumulativeVolume(),
                    state.getLastUpdated()
                );
                pendingQuotes.add(quote);
                exchangeMetrics.computeIfAbsent(state.getExchangeCode(), key -> new ExchangeMetrics()).recordTick(state.getLastUpdated());
            });
    }

    private void flushQuotesSafe() {
        try {
            flushQuotes();
        } catch (Exception ex) {
            log.error("Failed to flush mock market data quotes", ex);
        }
    }

    private void broadcastBarsSafe() {
        try {
            broadcastBars();
        } catch (Exception ex) {
            log.error("Failed to broadcast mock market data bars", ex);
        }
    }

    /**
     * Drain pending quotes, deduplicate by symbol, and broadcast only the latest
     * quote per symbol. This implements the batch broadcasting strategy from
     * research.md Section 6.1 to support high tick throughput without
     * overwhelming WebSocket consumers.
     */
    private void flushQuotes() {
        if (pendingQuotes.isEmpty()) {
            return;
        }
        Map<String, QuoteDTO> latestBySymbol = new HashMap<>();
        QuoteDTO quote;
        while ((quote = pendingQuotes.poll()) != null) {
            latestBySymbol.put(quote.symbol(), quote);
        }
        latestBySymbol.values().forEach(webSocketHandler::broadcastQuote);
    }

    private void broadcastBars() {
        if (feedState.get() != FeedState.RUNNING) {
            return;
        }
        instrumentStates
            .values()
            .forEach(state -> {
                if (lastClosedExchanges.contains(state.getExchangeCode())) {
                    return;
                }
                BarDTO bar = barAggregator.createBar(state);
                webSocketHandler.broadcastBar(bar);
            });
    }

    private ExchangeStatusDTO buildExchangeStatus(String exchangeCode, FeedState currentState) {
        ExchangeMetrics metrics = exchangeMetrics.getOrDefault(exchangeCode, new ExchangeMetrics());
        FeedState exchangeState = lastClosedExchanges.contains(exchangeCode) ? FeedState.HOLIDAY : currentState;
        return new ExchangeStatusDTO(
            exchangeCode,
            exchangeState,
            metrics.getLastTickTime(),
            metrics.getTicksPerSecond(),
            (int) instrumentStates.values().stream().filter(state -> state.getExchangeCode().equals(exchangeCode)).count()
        );
    }

    private Set<String> findClosedExchanges(Set<String> exchanges) {
        LocalDate today = LocalDate.now(clock);
        return exchanges.stream().filter(code -> isExchangeClosed(code, today)).collect(Collectors.toUnmodifiableSet());
    }

    private boolean isExchangeClosed(String exchangeCode, LocalDate date) {
        List<MarketHoliday> holidays = marketHolidayRepository.findAllByExchange_CodeAndTradeDateAndIsHolidayTrue(exchangeCode, date);
        return !holidays.isEmpty();
    }

    private static class ExchangeMetrics {

        private final Deque<Instant> tickTimes = new ArrayDeque<>();
        private Instant lastTickTime;

        synchronized void recordTick(Instant instant) {
            tickTimes.addLast(instant);
            lastTickTime = instant;
            Instant threshold = instant.minusSeconds(5);
            while (!tickTimes.isEmpty() && tickTimes.peekFirst().isBefore(threshold)) {
                tickTimes.removeFirst();
            }
        }

        synchronized int getTicksPerSecond() {
            if (tickTimes.isEmpty()) {
                return 0;
            }
            Instant first = tickTimes.peekFirst();
            Instant last = tickTimes.peekLast();
            long durationSeconds = Math.max(1, Duration.between(first, last).getSeconds());
            return (int) Math.round((double) tickTimes.size() / durationSeconds);
        }

        synchronized Instant getLastTickTime() {
            return lastTickTime;
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final String prefix;

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(prefix + "-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        }
    }
}
