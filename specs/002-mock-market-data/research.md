# Research: Mock Market Data & Market Watch

**Date**: 2025-11-13  
**Feature**: 002-mock-market-data  
**Purpose**: Document technical decisions, best practices, and alternatives evaluated for implementing simulated market data pipeline.

---

## 1. Spring WebSocket/STOMP Architecture

### Decision

Use **Spring WebSocket with STOMP over SockJS** for real-time quote and bar broadcasting.

### Rationale

- **Native JHipster Integration**: JHipster 8.x includes Spring WebSocket configuration out-of-the-box with JWT-based authentication
- **Topic-Based Broadcasting**: STOMP provides `/topic/quotes/{symbol}` and `/topic/bars/{symbol}` subscription patterns that map naturally to instrument-level data streams
- **Scalability**: Spring's `SimpMessagingTemplate` supports 10,000+ concurrent connections with proper thread pool tuning
- **Fallback Support**: SockJS provides WebSocket fallback to HTTP long-polling for restrictive network environments
- **Security**: JWT validation in handshake via `StompHeaderAccessor` ensures authenticated-only access

### Implementation Approach

```text
1. Extend WebSocketConfig with STOMP message broker:
   - Enable simple in-memory broker for /topic destinations
   - Configure application destination prefix /app
   - Register SockJS endpoint at /ws

2. Create MarketDataWebSocketHandler:
   - Use SimpMessagingTemplate to broadcast QuoteDTO/BarDTO
   - Topic naming: /topic/quotes/{symbol}, /topic/bars/{symbol}
   - Serialize DTOs to JSON automatically via Jackson

3. JWT Authentication:
   - Extract token from Sec-WebSocket-Protocol or query param
   - Validate via existing JHipster TokenProvider
   - Store Authentication in WebSocket session attributes
```

### Alternatives Considered

- **Server-Sent Events (SSE)**: Rejected because unidirectional (no client commands) and less robust reconnection handling than WebSocket
- **Raw WebSocket without STOMP**: Rejected because requires manual message framing, subscription management, and authentication protocol
- **Redis Pub/Sub**: Considered for horizontal scaling but deferred to post-MVP; in-memory broker sufficient for 1,000 concurrent traders

### Best Practices

- **Throttle Broadcasts**: Batch quote updates per 100-200ms window to avoid overwhelming clients with high-frequency ticks
- **Heartbeat Configuration**: Enable STOMP heartbeats (10s client → server, 10s server → client) to detect stale connections
- **Subscription Limits**: Enforce max 50 symbols per client to prevent abuse and memory exhaustion
- **Graceful Degradation**: Log but don't crash on individual send failures; continue broadcasting to other subscribers

---

## 2. Random Walk Price Generation

### Decision

Use **Geometric Brownian Motion (GBM)** with drift=0 and bounded constraints for simulated price ticks.

### Rationale

- **Realistic Movement**: GBM models actual equity price behavior better than arithmetic random walk (prevents negative prices naturally via multiplicative steps)
- **Configurable Volatility**: Allows per-exchange or per-asset-class volatility tuning (σ parameter)
- **Simplicity**: Drift=0 means no artificial upward/downward bias, keeping prices neutral over time
- **Bounds Enforcement**: Explicit min/max price checks prevent runaway values in extended simulations

### Formula

```text
P(t+Δt) = P(t) × exp(σ × √Δt × Z)

Where:
- P(t) = last price at time t
- σ = volatility factor (default 0.01 = 1% per tick)
- Δt = time step (normalized to 1 for per-tick updates)
- Z = random sample from standard normal distribution N(0,1)

Bounds:
- P(t+Δt) = max(minPrice, min(maxPrice, P(t+Δt)))
- minPrice = 0.01 × openPrice (prevent zero/negative)
- maxPrice = 10.0 × openPrice (prevent runaway inflation)
```

### Implementation Approach

```java
public class PriceGenerator {

  private final Random random = new Random();

  public BigDecimal nextPrice(BigDecimal lastPrice, BigDecimal openPrice, double volatility) {
    double z = random.nextGaussian(); // N(0,1)
    double factor = Math.exp(volatility * z);
    BigDecimal newPrice = lastPrice.multiply(BigDecimal.valueOf(factor));

    // Apply bounds
    BigDecimal minPrice = openPrice.multiply(BigDecimal.valueOf(0.01));
    BigDecimal maxPrice = openPrice.multiply(BigDecimal.valueOf(10.0));

    if (newPrice.compareTo(minPrice) < 0) return minPrice;
    if (newPrice.compareTo(maxPrice) > 0) return maxPrice;

    return newPrice.setScale(2, RoundingMode.HALF_UP); // Round to 2 decimals
  }
}

```

### Alternatives Considered

- **Arithmetic Random Walk** (P(t+1) = P(t) + ε): Rejected because allows negative prices without complex bounds logic
- **Mean-Reverting Ornstein-Uhlenbeck**: Rejected as overly complex for MVP; traders don't need statistical arbitrage signals yet
- **Historical Tick Replay**: Rejected because requires large tick datasets and doesn't generalize across new instruments

### Best Practices

- **Per-Exchange Volatility**: NSE equities σ=0.008, MCX commodities σ=0.015, BSE equities σ=0.008 (configurable in application properties)
- **Tick Size Compliance**: Round final price to instrument's tick size (e.g., ₹0.05 for most equities)
- **Volume Simulation**: Generate random volume per tick as `V = baseVolume × (1 + 0.5 × |Z|)` where Z is same Gaussian sample

---

## 3. OHLC Bar Aggregation

### Decision

Maintain **session-level OHLC** (not per-minute bars) and publish bar summaries every 60 seconds.

### Rationale

- **Spec Requirement**: FR-006 states "maintain OHLC across entire session" and "publish every minute"
- **Simplicity**: Single OHLC state per instrument eliminates need for complex windowing logic
- **Performance**: O(1) updates per tick (compare vs high/low, accumulate volume) scale linearly with instrument count

### State Model

```java
public class InstrumentState {

  private String symbol;
  private BigDecimal lastPrice;
  private BigDecimal sessionOpen; // Set at feed start from last close or default
  private BigDecimal sessionHigh; // Updated every tick: max(high, lastPrice)
  private BigDecimal sessionLow; // Updated every tick: min(low, lastPrice)
  private long cumulativeVolume; // Incremented every tick
  private Instant lastUpdated;
  private double volatility; // From config per exchange/asset class
}

```

### Implementation Approach

```text
1. On Feed Start:
   - Load all active instruments from DB
   - Initialize sessionOpen = lastClose ?? defaultPrice(instrument)
   - Reset sessionHigh/sessionLow = sessionOpen
   - Reset cumulativeVolume = 0

2. On Each Tick (500-1000ms):
   - Generate newPrice via PriceGenerator
   - Update lastPrice = newPrice
   - Update sessionHigh = max(sessionHigh, newPrice)
   - Update sessionLow = min(sessionLow, newPrice)
   - Increment cumulativeVolume += randomVolume()
   - Broadcast QuoteDTO to /topic/quotes/{symbol}

3. Every 60 Seconds (ScheduledExecutorService):
   - For each instrument, create BarDTO:
     - open = sessionOpen
     - high = sessionHigh
     - low = sessionLow
     - close = lastPrice
     - volume = cumulativeVolume
   - Broadcast BarDTO to /topic/bars/{symbol}
```

### Alternatives Considered

- **Per-Minute Sliding Windows**: Rejected because spec explicitly requires session-level OHLC, not true time-series bars
- **External Time-Series DB (InfluxDB)**: Deferred to post-MVP; in-memory state sufficient for mock data (no persistence required)

### Best Practices

- **Thread Safety**: Use `ConcurrentHashMap<String, InstrumentState>` for multi-threaded tick generation
- **Atomic Updates**: Synchronize OHLC updates within single tick to prevent race conditions
- **Bar Throttling**: Don't send bar updates for instruments with zero ticks in last minute (exchange closed/holiday)

---

## 4. Market Holiday & Trading Session Handling

### Decision

Query **MarketHoliday table** before each tick generation cycle and filter out instruments whose exchanges are on holiday or outside trading hours.

### Rationale

- **Spec Requirement**: FR-004 mandates respecting MarketHoliday records and trading session windows
- **Realism**: Prevents artificial ticks during non-trading periods, matches live market behavior
- **Data Integrity**: Existing M0 baseline seed includes MarketHoliday entities per exchange

### Implementation Approach

```java
// On each scheduled tick cycle (500-1000ms):
public void generateTicks() {
  LocalDate today = LocalDate.now();
  Set<String> closedExchanges = marketHolidayRepository
    .findByHolidayDate(today)
    .stream()
    .map(h -> h.getExchange().getCode())
    .collect(Collectors.toSet());

  instrumentStates
    .values()
    .stream()
    .filter(state -> !closedExchanges.contains(state.getExchange().getCode()))
    .filter(state -> isWithinTradingHours(state.getExchange()))
    .forEach(state -> {
      BigDecimal newPrice = priceGenerator.nextPrice(state.getLastPrice(), state.getSessionOpen(), state.getVolatility());
      updateStateAndBroadcast(state, newPrice);
    });
}

private boolean isWithinTradingHours(Exchange exchange) {
  LocalTime now = LocalTime.now();
  // Simplified: 09:15 - 15:30 for NSE/BSE, 09:00 - 23:30 for MCX
  // TODO: Read from exchange.tradingStart / exchange.tradingEnd properties
  return now.isAfter(exchange.getTradingStart()) && now.isBefore(exchange.getTradingEnd());
}

```

### Best Practices

- **Cache Holiday Lookup**: Refresh closed exchanges set once per minute (not per tick) to reduce DB queries
- **Timezone Handling**: Use exchange's local timezone (Asia/Kolkata for NSE/BSE/MCX) for all time checks
- **UI Feedback**: Market Watch displays "Closed/Holiday" badge for instruments from closed exchanges (FR-014)

---

## 5. React WebSocket Subscription Management

### Decision

Use **custom React hook** (`useMarketDataSubscription`) wrapping STOMP client with automatic subscribe/unsubscribe lifecycle.

### Rationale

- **Declarative API**: Component declares "I need quotes for these symbols" and hook handles connection/reconnection
- **Memory Efficiency**: Auto-unsubscribe on unmount prevents subscription leaks
- **Reconnection Logic**: Exponential backoff and stale connection detection built into hook

### Implementation Approach

```typescript
// hooks/useMarketDataSubscription.ts
export const useMarketDataSubscription = (symbols: string[], onQuote: (quote: IQuote) => void) => {
  const [status, setStatus] = useState<'connecting' | 'connected' | 'disconnected'>('connecting');
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      connectHeaders: {
        Authorization: `Bearer ${getJWT()}`,
      },
      reconnectDelay: 5000, // Exponential backoff handled by stompjs
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setStatus('connected');
        symbols.forEach(symbol => {
          client.subscribe(`/topic/quotes/${symbol}`, message => {
            const quote = JSON.parse(message.body);
            onQuote(quote);
          });
        });
      },
      onDisconnect: () => setStatus('disconnected'),
      onStompError: frame => {
        console.error('STOMP error', frame);
        setStatus('disconnected');
      },
    });

    client.activate();
    clientRef.current = client;

    return () => client.deactivate(); // Cleanup on unmount
  }, [symbols.join(',')]); // Re-subscribe if symbols change

  return { status };
};
```

### Best Practices

- **Throttle Redux Dispatches**: Use lodash `throttle(200ms)` when updating quote state to avoid 60fps React re-renders
- **Symbol Change Diffing**: Only unsubscribe/resubscribe changed symbols when watchlist updates (not full disconnect)
- **Stale Data Detection**: If no message received for 10s, show "Stale" indicator even if connection is "connected"

---

## 6. Performance Optimization Strategies

### Decision

Apply **multi-tier optimization** approach: (1) Batch broadcasting, (2) ThreadPool tuning, (3) Client-side throttling.

### Rationale

- **Requirement**: Must support 10,000 tick updates/sec and 1,000+ concurrent traders (from Technical Context)
- **Bottlenecks**: CPU (price generation), Network (WebSocket send), Memory (subscription tracking)

### Techniques

#### 6.1 Server-Side Batch Broadcasting

```java
// Instead of sending each tick immediately:
private final BlockingQueue<QuoteDTO> pendingQuotes = new LinkedBlockingQueue<>();

@Scheduled(fixedDelay = 100) // Send every 100ms
public void flushQuotes() {
  List<QuoteDTO> batch = new ArrayList<>();
  pendingQuotes.drainTo(batch, 1000); // Max 1000 per batch

  batch
    .stream()
    .collect(Collectors.groupingBy(QuoteDTO::getSymbol))
    .forEach((symbol, quotes) -> {
      // Send only latest quote per symbol in batch
      QuoteDTO latest = quotes.get(quotes.size() - 1);
      messagingTemplate.convertAndSend("/topic/quotes/" + symbol, latest);
    });
}

```

**Benefit**: Reduces WebSocket send syscalls from 10,000/sec to ~100/sec while keeping latency <100ms

#### 6.2 ThreadPool Configuration

```yaml
# application.yml
spring:
  task:
    scheduling:
      pool:
        size: 8 # Dedicated threads for tick generation
    execution:
      pool:
        core-size: 16
        max-size: 32
        queue-capacity: 1000
```

**Benefit**: Isolates tick generation (CPU-bound) from WebSocket broadcast (IO-bound)

#### 6.3 Client-Side Redux Throttling

```typescript
const throttledUpdateQuote = throttle((quote: IQuote) => {
  dispatch(updateQuote(quote));
}, 200); // Max 5 updates/sec per symbol to UI
```

**Benefit**: Prevents React re-render storms when subscribed to 50 symbols × 10 ticks/sec = 500 updates/sec

### Load Testing Plan

- **Gatling Scenario**: 1,000 virtual traders each subscribing to 10 symbols, measure p95 latency and message loss rate
- **Success Criteria**: <500ms p95 quote delivery latency, <0.1% message loss, <4GB heap usage

---

## 7. Operator Control Panel & Status Reporting

### Decision

Store feed state in **singleton service** with status exposed via REST endpoint, refreshed in UI every 2 seconds.

### Rationale

- **Stateful Service**: MockMarketDataService maintains single running/stopped state and per-exchange metrics
- **Idempotent API**: POST /api/marketdata/mock/start is idempotent (returns success if already running)
- **Real-Time Metrics**: Maintain Micrometer counters for ticks/sec per exchange, exposed in FeedStatusDTO

### Status DTO Schema

```java
public class FeedStatusDTO {

  private FeedState globalState; // RUNNING, STOPPED
  private Instant startedAt;
  private List<ExchangeStatusDTO> exchanges;

  public static class ExchangeStatusDTO {

    private String exchangeCode; // NSE, BSE, MCX
    private FeedState state; // RUNNING, STOPPED, HOLIDAY
    private Instant lastTickTime;
    private int ticksPerSecond; // Rolling 10-second average
    private int activeInstruments;
  }
}

```

### Implementation Approach

```java
@Service
public class MockMarketDataService {

  private volatile FeedState state = FeedState.STOPPED;
  private ScheduledFuture<?> tickJob;
  private final Map<String, MeterRegistry> exchangeMetrics;

  public void start() {
    if (state == FeedState.RUNNING) return; // Idempotent

    state = FeedState.RUNNING;
    tickJob = scheduler.scheduleAtFixedRate(
      this::generateTicks,
      0,
      750, // 750ms per tick cycle
      TimeUnit.MILLISECONDS
    );
    log.info("Mock feed started");
  }

  public void stop() {
    if (tickJob != null) tickJob.cancel(false);
    state = FeedState.STOPPED;
    log.info("Mock feed stopped");
  }

  public FeedStatusDTO getStatus() {
    return new FeedStatusDTO(state, startedAt, exchanges.stream().map(this::buildExchangeStatus).collect(Collectors.toList()));
  }
}

```

### Best Practices

- **Startup Behavior**: FR-013 requires auto-start on application launch; implement via `@PostConstruct` with holiday check
- **Graceful Shutdown**: Use `@PreDestroy` to stop feed cleanly before app termination
- **Audit Logging**: Log operator start/stop actions to TraderAuditLog entity with user ID and timestamp

---

## 8. Watchlist Management

### Decision

Extend existing **Watchlist entity** with new REST endpoints for add/remove items, return updated list in response for optimistic UI updates.

### Rationale

- **Existing Schema**: M0 baseline seed includes Watchlist and WatchlistItem entities linked to TraderProfile
- **REST Semantics**: POST /api/watchlists/{id}/items (add), DELETE /api/watchlists/{id}/items/{symbol} (remove)
- **Optimistic UI**: Return full updated watchlist in response to avoid extra GET request

### Endpoint Contracts

```yaml
# swagger/api.yml additions
/api/watchlists/{id}/items:
  post:
    summary: Add instrument to watchlist
    security:
      - jwt: [TRADER]
    parameters:
      - name: id
        in: path
        required: true
        schema: { type: integer }
    requestBody:
      content:
        application/json:
          schema:
            type: object
            properties:
              symbol: { type: string }
    responses:
      200:
        description: Updated watchlist
        content:
          application/json:
            schema: { $ref: '#/components/schemas/WatchlistDTO' }
      403:
        description: Watchlist does not belong to requesting trader

/api/watchlists/{id}/items/{symbol}:
  delete:
    summary: Remove instrument from watchlist
    security:
      - jwt: [TRADER]
    parameters:
      - name: id
        in: path
        required: true
      - name: symbol
        in: path
        required: true
    responses:
      200:
        description: Updated watchlist
      404:
        description: Symbol not in watchlist
```

### Security Enforcement

```java
@PreAuthorize("hasAuthority('TRADER')")
public ResponseEntity<WatchlistDTO> addItem(@PathVariable Long id, @RequestBody AddItemRequest request) {
  Watchlist watchlist = watchlistRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

  // Verify ownership
  if (!watchlist.getTrader().getUser().getLogin().equals(SecurityUtils.getCurrentUserLogin())) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }

  // Add item logic...
  return ResponseEntity.ok(watchlistMapper.toDto(watchlist));
}

```

---

## Summary of Key Decisions

| Area                    | Decision                                              | Primary Rationale                                                    |
| ----------------------- | ----------------------------------------------------- | -------------------------------------------------------------------- |
| **Real-Time Transport** | Spring WebSocket/STOMP over SockJS                    | Native JHipster integration, topic-based routing, JWT authentication |
| **Price Model**         | Geometric Brownian Motion (drift=0)                   | Realistic equity behavior, natural bounds, configurable volatility   |
| **OHLC Aggregation**    | Session-level OHLC, 60-second bar broadcasts          | Spec requirement, simplicity, O(1) per tick                          |
| **Holiday Handling**    | Query MarketHoliday table per tick cycle              | Respects existing seed data, realistic market closure                |
| **React Subscriptions** | Custom hook with auto-lifecycle                       | Declarative, prevents leaks, built-in reconnection                   |
| **Performance**         | Batch broadcasting + thread pools + client throttling | Achieves 10k ticks/sec target with <500ms latency                    |
| **Operator Control**    | Singleton service with REST status endpoint           | Stateful feed state, idempotent API, real-time metrics               |
| **Watchlist API**       | Extend existing entity with add/remove REST endpoints | Leverages M0 schema, optimistic UI updates, RBAC                     |

---

**Next Phase**: Phase 1 (Design & Contracts) — Generate data-model.md, OpenAPI contracts, and quickstart.md based on these research findings.
