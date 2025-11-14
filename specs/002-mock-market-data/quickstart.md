# Quickstart Guide: Mock Market Data & Market Watch

**Feature**: 002-mock-market-data  
**Date**: 2025-11-13  
**Purpose**: Step-by-step guide to build, run, and test the simulated market data pipeline.

---

## Prerequisites

Ensure the following are installed and configured:

| Tool                  | Version | Verification Command |
| --------------------- | ------- | -------------------- |
| **Java**              | 21+     | `java -version`      |
| **Node.js**           | 20+     | `node -v`            |
| **Maven**             | 3.9+    | `mvn -v`             |
| **PostgreSQL**        | 15+     | `psql --version`     |
| **Docker** (optional) | 24+     | `docker -v`          |

**JHipster Setup**: Project generated with JHipster 8.x (already done in M0)

**Baseline Data**: M0 seed data must be loaded (Instrument, Exchange, MarketHoliday, TraderProfile, Watchlist)

---

## Phase 1: Verify Baseline Environment

### 1.1 Check Database Connectivity

```bash
# Start PostgreSQL (if using Docker)
cd src/main/docker
docker-compose -f postgresql.yml up -d

# Verify connection
psql -h localhost -U rnexchange -d rnexchange -c "\dt"
# Should list tables: instrument, exchange, market_holiday, watchlist, etc.
```

### 1.2 Run M0 Baseline Seed

```bash
# From project root
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,seed

# Check logs for:
# "Baseline seed completed successfully"
# "Loaded 1500+ instruments for NSE, BSE, MCX"
```

### 1.3 Verify Frontend Build

```bash
# Install dependencies (if not already done)
npm install

# Build frontend
npm run webapp:build:dev

# Should complete without errors
```

---

## Phase 2: Implement Backend Components (TDD Approach)

### 2.1 Create DTOs and State Models

**Files to create** (in order):

```text
src/main/java/com/rnexchange/service/dto/
├── QuoteDTO.java
├── BarDTO.java
└── FeedStatusDTO.java

src/main/java/com/rnexchange/service/marketdata/
├── InstrumentState.java
├── PriceGenerator.java
├── BarAggregator.java
└── MockMarketDataService.java
```

**Recommended Flow**:

1. Copy DTO schemas from `data-model.md` → implement as Java records
2. Write unit tests for `PriceGenerator` (test random walk bounds, volatility)
3. Implement `PriceGenerator` to pass tests
4. Write unit tests for `InstrumentState` (test OHLC updates)
5. Implement `InstrumentState` with synchronized methods

**Example Unit Test** (`PriceGeneratorTest.java`):

```java
@Test
void shouldGeneratePriceWithinBounds() {
  PriceGenerator generator = new PriceGenerator();
  BigDecimal lastPrice = new BigDecimal("100.00");
  BigDecimal openPrice = new BigDecimal("100.00");
  double volatility = 0.01; // 1%

  // Generate 1000 ticks
  for (int i = 0; i < 1000; i++) {
    BigDecimal newPrice = generator.nextPrice(lastPrice, openPrice, volatility);

    // Price should be within [0.01 × open, 10.0 × open]
    assertThat(newPrice).isGreaterThanOrEqualTo(openPrice.multiply(BigDecimal.valueOf(0.01)));
    assertThat(newPrice).isLessThanOrEqualTo(openPrice.multiply(BigDecimal.valueOf(10.0)));

    lastPrice = newPrice;
  }
}

```

### 2.2 Implement MockMarketDataService

**Contract Test** (`MockMarketDataServiceIT.java`):

```java
@SpringBootTest
class MockMarketDataServiceIT {

  @Autowired
  private MockMarketDataService mockMarketDataService;

  @Test
  void shouldStartFeed() {
    mockMarketDataService.start();
    FeedStatusDTO status = mockMarketDataService.getStatus();

    assertThat(status.globalState()).isEqualTo(FeedState.RUNNING);
    assertThat(status.startedAt()).isNotNull();
  }

  @Test
  void shouldStopFeed() {
    mockMarketDataService.start();
    mockMarketDataService.stop();
    FeedStatusDTO status = mockMarketDataService.getStatus();

    assertThat(status.globalState()).isEqualTo(FeedState.STOPPED);
  }

  @Test
  void shouldBeIdempotent() {
    mockMarketDataService.start();
    mockMarketDataService.start(); // Second call should not throw

    FeedStatusDTO status = mockMarketDataService.getStatus();
    assertThat(status.globalState()).isEqualTo(FeedState.RUNNING);
  }
}

```

**Implementation Steps**:

1. Write tests above
2. Implement `MockMarketDataService.start()`:
   - Load active instruments from `InstrumentRepository`
   - Initialize `InstrumentState` map
   - Start `ScheduledExecutorService` with 750ms tick interval
3. Implement `MockMarketDataService.stop()`:
   - Cancel scheduled task
   - Set state to STOPPED
4. Implement `MockMarketDataService.getStatus()`:
   - Aggregate per-exchange metrics from `InstrumentState` map
5. Add `@PostConstruct` method to auto-start on application launch (FR-013)

### 2.3 Configure WebSocket/STOMP

**Extend existing WebSocketConfig**:

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic"); // Enable /topic destinations
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
  }
}

```

**Create MarketDataWebSocketHandler**:

```java
@Service
public class MarketDataWebSocketHandler {

  private final SimpMessagingTemplate messagingTemplate;

  public void broadcastQuote(QuoteDTO quote) {
    messagingTemplate.convertAndSend("/topic/quotes/" + quote.symbol(), quote);
  }

  public void broadcastBar(BarDTO bar) {
    messagingTemplate.convertAndSend("/topic/bars/" + bar.symbol(), bar);
  }
}

```

**Integration Test** (`MarketDataWebSocketIT.java`):

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MarketDataWebSocketIT {

  @LocalServerPort
  private int port;

  @Autowired
  private MockMarketDataService mockMarketDataService;

  @Test
  void shouldReceiveQuoteViaWebSocket() throws Exception {
    // Setup WebSocket client (see websocket-topics.md for full code)
    StompSession session = connectToWebSocket();
    BlockingQueue<QuoteDTO> quotes = new LinkedBlockingQueue<>();

    session.subscribe("/topic/quotes/RELIANCE", new QuoteStompFrameHandler(quotes));

    mockMarketDataService.start();

    // Wait for at least one quote within 2 seconds
    QuoteDTO quote = quotes.poll(2, TimeUnit.SECONDS);
    assertThat(quote).isNotNull();
    assertThat(quote.symbol()).isEqualTo("RELIANCE");
  }
}

```

### 2.4 Create REST Endpoints

**Update OpenAPI Spec**:

```bash
# Copy contracts/mock-market-data.openapi.yaml to src/main/resources/swagger/api.yml
# (Merge with existing api.yml if present)

# Generate API stubs
./mvnw generate-sources

# This generates:
# - MarketDataControlApiDelegate interface
# - WatchlistManagementApiDelegate interface
```

**Implement Delegates**:

```java
@Service
public class MarketDataControlApiDelegateImpl implements MarketDataControlApiDelegate {

  private final MockMarketDataService mockMarketDataService;

  @Override
  @PreAuthorize("hasAuthority('EXCHANGE_OPERATOR')")
  public ResponseEntity<FeedStatusDTO> startMockFeed() {
    mockMarketDataService.start();
    return ResponseEntity.ok(mockMarketDataService.getStatus());
  }

  @Override
  @PreAuthorize("hasAuthority('EXCHANGE_OPERATOR')")
  public ResponseEntity<FeedStatusDTO> stopMockFeed() {
    mockMarketDataService.stop();
    return ResponseEntity.ok(mockMarketDataService.getStatus());
  }

  @Override
  @PreAuthorize("hasAuthority('EXCHANGE_OPERATOR')")
  public ResponseEntity<FeedStatusDTO> getMockFeedStatus() {
    return ResponseEntity.ok(mockMarketDataService.getStatus());
  }
}

```

**Contract Test** (`MarketDataResourceIT.java`):

```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "exchange_op", authorities = { "EXCHANGE_OPERATOR" })
class MarketDataResourceIT {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldStartFeed() throws Exception {
    mockMvc
      .perform(post("/api/marketdata/mock/start").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.globalState").value("RUNNING"))
      .andExpect(jsonPath("$.startedAt").isNotEmpty());
  }

  @Test
  @WithMockUser(username = "trader1", authorities = { "TRADER" })
  void shouldRejectTraderStartRequest() throws Exception {
    mockMvc.perform(post("/api/marketdata/mock/start").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
  }
}

```

### 2.5 Extend Watchlist Endpoints

**Implement WatchlistManagementApiDelegateImpl**:

```java
@Service
public class WatchlistManagementApiDelegateImpl implements WatchlistManagementApiDelegate {

  private final WatchlistRepository watchlistRepository;
  private final InstrumentRepository instrumentRepository;
  private final WatchlistMapper watchlistMapper;

  @Override
  @PreAuthorize("hasAuthority('TRADER')")
  public ResponseEntity<WatchlistDTO> addWatchlistItem(Long id, AddWatchlistItemRequest request) {
    String currentUser = SecurityUtils.getCurrentUserLogin().orElseThrow();

    Watchlist watchlist = watchlistRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watchlist not found"));

    // Verify ownership
    if (!watchlist.getTrader().getUser().getLogin().equals(currentUser)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your watchlist");
    }

    // Verify instrument exists
    Instrument instrument = instrumentRepository
      .findBySymbol(request.symbol())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol not found"));

    // Check for duplicate
    boolean exists = watchlist.getItems().stream().anyMatch(item -> item.getSymbol().equals(request.symbol()));
    if (exists) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol already in watchlist");
    }

    // Add item
    WatchlistItem item = new WatchlistItem();
    item.setWatchlist(watchlist);
    item.setSymbol(request.symbol());
    item.setSortOrder(watchlist.getItems().size());
    watchlist.getItems().add(item);

    watchlistRepository.save(watchlist);

    return ResponseEntity.ok(watchlistMapper.toDto(watchlist));
  }
  // Implement removeWatchlistItem() similarly...
}

```

**Contract Test**:

```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "trader1", authorities = { "TRADER" })
class WatchlistResourceIT {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldAddItemToWatchlist() throws Exception {
    mockMvc
      .perform(post("/api/watchlists/1/items").contentType(MediaType.APPLICATION_JSON).content("{\"symbol\":\"RELIANCE\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.items[?(@.symbol == 'RELIANCE')]").exists());
  }
}

```

---

## Phase 3: Implement Frontend Components

### 3.1 Create Market Watch Module

**Directory Structure**:

```text
src/main/webapp/app/modules/market-watch/
├── market-watch.tsx
├── market-watch.reducer.ts
├── market-watch.scss
├── watchlist-selector.tsx
└── websocket-service.ts
```

### 3.2 Implement WebSocket Service

**File**: `src/main/webapp/app/modules/market-watch/websocket-service.ts`

```typescript
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Storage } from 'react-jhipster';

export interface IQuote {
  symbol: string;
  lastPrice: number;
  open: number;
  change: number;
  changePercent: number;
  volume: number;
  timestamp: string;
}

export class MarketDataWebSocketService {
  private client: Client | null = null;
  private subscriptions = new Map<string, any>();

  connect(onQuote: (quote: IQuote) => void, onStatusChange: (status: string) => void) {
    const token = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');

    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        onStatusChange('connected');
      },
      onDisconnect: () => {
        onStatusChange('disconnected');
        this.subscriptions.clear();
      },
      onStompError: frame => {
        console.error('STOMP error', frame);
        onStatusChange('error');
      },
    });

    this.client.activate();
  }

  subscribe(symbols: string[], onQuote: (quote: IQuote) => void) {
    if (!this.client) return;

    symbols.forEach(symbol => {
      const subscription = this.client!.subscribe(`/topic/quotes/${symbol}`, (message: IMessage) => {
        const quote: IQuote = JSON.parse(message.body);
        onQuote(quote);
      });
      this.subscriptions.set(symbol, subscription);
    });
  }

  unsubscribe(symbol: string) {
    const subscription = this.subscriptions.get(symbol);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(symbol);
    }
  }

  disconnect() {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.subscriptions.clear();
    if (this.client) {
      this.client.deactivate();
    }
  }
}
```

### 3.3 Create Redux Reducer

**File**: `src/main/webapp/app/modules/market-watch/market-watch.reducer.ts`

```typescript
import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { IQuote } from './websocket-service';

interface MarketWatchState {
  quotes: Record<string, IQuote>;
  connectionStatus: 'connecting' | 'connected' | 'disconnected' | 'error';
}

const initialState: MarketWatchState = {
  quotes: {},
  connectionStatus: 'disconnected',
};

const marketWatchSlice = createSlice({
  name: 'marketWatch',
  initialState,
  reducers: {
    updateQuote: (state, action: PayloadAction<IQuote>) => {
      state.quotes[action.payload.symbol] = action.payload;
    },
    setConnectionStatus: (state, action: PayloadAction<MarketWatchState['connectionStatus']>) => {
      state.connectionStatus = action.payload;
    },
    clearQuotes: state => {
      state.quotes = {};
    },
  },
});

export const { updateQuote, setConnectionStatus, clearQuotes } = marketWatchSlice.actions;
export default marketWatchSlice.reducer;
```

### 3.4 Create Market Watch Component

**File**: `src/main/webapp/app/modules/market-watch/market-watch.tsx`

```typescript
import React, { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { MarketDataWebSocketService } from './websocket-service';
import { updateQuote, setConnectionStatus } from './market-watch.reducer';
import './market-watch.scss';

const marketDataService = new MarketDataWebSocketService();

export const MarketWatch = () => {
  const dispatch = useAppDispatch();
  const quotes = useAppSelector(state => state.marketWatch.quotes);
  const connectionStatus = useAppSelector(state => state.marketWatch.connectionStatus);
  const [watchlistSymbols] = useState(['RELIANCE', 'TCS', 'INFY']); // TODO: Load from API

  useEffect(() => {
    marketDataService.connect(
      (quote) => dispatch(updateQuote(quote)),
      (status) => dispatch(setConnectionStatus(status as any))
    );

    marketDataService.subscribe(watchlistSymbols, (quote) => dispatch(updateQuote(quote)));

    return () => {
      marketDataService.disconnect();
    };
  }, [watchlistSymbols.join(',')]);

  const getRowClass = (change: number) => {
    if (change > 0) return 'positive';
    if (change < 0) return 'negative';
    return 'neutral';
  };

  return (
    <div className="market-watch">
      <div className="header">
        <h2>Market Watch</h2>
        <span className="simulated-badge">SIMULATED FEED</span>
        <span className={`status-badge ${connectionStatus}`}>{connectionStatus.toUpperCase()}</span>
      </div>

      <table className="market-watch-table">
        <thead>
          <tr>
            <th>Symbol</th>
            <th>LTP</th>
            <th>Change</th>
            <th>Change %</th>
            <th>Volume</th>
            <th>Last Updated</th>
          </tr>
        </thead>
        <tbody>
          {watchlistSymbols.map(symbol => {
            const quote = quotes[symbol];
            return (
              <tr key={symbol} className={quote ? getRowClass(quote.change) : ''}>
                <td>{symbol}</td>
                <td>{quote?.lastPrice.toFixed(2) ?? '-'}</td>
                <td>{quote?.change.toFixed(2) ?? '-'}</td>
                <td>{quote?.changePercent.toFixed(2) ?? '-'}%</td>
                <td>{quote?.volume.toLocaleString() ?? '-'}</td>
                <td>{quote ? new Date(quote.timestamp).toLocaleTimeString() : '-'}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default MarketWatch;
```

### 3.5 Register Reducer and Route

**File**: `src/main/webapp/app/config/store.ts` (add reducer)

```typescript
import marketWatchReducer from 'app/modules/market-watch/market-watch.reducer';

// ... existing code ...

const rootReducer = {
  // ... existing reducers ...
  marketWatch: marketWatchReducer,
};
```

**File**: `src/main/webapp/app/routes.tsx` (add route)

```typescript
import MarketWatch from 'app/modules/market-watch/market-watch';

// ... existing code ...

<PrivateRoute path="/market-watch" component={MarketWatch} hasAnyAuthorities={[AUTHORITIES.TRADER]} />
```

---

## Phase 4: Run & Test End-to-End

### 4.1 Start Application

```bash
# Terminal 1: Start backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Wait for: "Application 'rnexchange' is running!"

# Terminal 2: Start frontend dev server (if not using mvnw)
npm start
```

### 4.2 Verify Feed Auto-Start

```bash
# Check logs for:
# "Mock feed started automatically on application startup"
# "Loaded 1523 instruments across NSE, BSE, MCX"

# Or test via REST:
curl -X GET http://localhost:8080/api/marketdata/mock/status \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected response:
# {"globalState":"RUNNING","startedAt":"2025-11-13T09:15:00.000Z",...}
```

### 4.3 Test Exchange Operator Console

**Login as Exchange Operator**:

- Username: `exchange_op`
- Password: (from M0 seed data)

**Navigate to**: `/exchange-console/market-data`

**Verify**:

- [x] Feed status shows "RUNNING"
- [x] NSE, BSE, MCX panels show tick rates
- [x] "Stop Feed" button works (status changes to "STOPPED" within 2s)
- [x] "Start Feed" button works (status changes to "RUNNING" within 2s)

### 4.4 Test Trader Market Watch

**Login as Trader**:

- Username: `trader1`
- Password: (from M0 seed data)

**Navigate to**: `/market-watch`

**Verify**:

- [x] "SIMULATED FEED" badge visible
- [x] Connection status shows "CONNECTED"
- [x] Watchlist symbols (RELIANCE, TCS, INFY) display with LTP
- [x] Prices update automatically every ~1 second
- [x] Change % shows green/red color coding
- [x] Volume increments over time
- [x] Last updated time refreshes

**Test Watchlist Management**:

```bash
# Add instrument via REST (or implement UI button)
curl -X POST http://localhost:8080/api/watchlists/1/items \
  -H "Authorization: Bearer TRADER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"INFY"}'

# Verify: INFY appears in Market Watch table and starts receiving quotes
```

### 4.5 Test Holiday Handling

**Insert Holiday**:

```sql
-- Connect to DB
psql -h localhost -U rnexchange -d rnexchange

-- Add today as NSE holiday
INSERT INTO market_holiday (exchange_id, holiday_date, description)
VALUES (
  (SELECT id FROM exchange WHERE code = 'NSE'),
  CURRENT_DATE,
  'Test Holiday'
);
```

**Restart Feed** (to pick up holiday):

```bash
curl -X POST http://localhost:8080/api/marketdata/mock/stop \
  -H "Authorization: Bearer EXCHANGE_OP_JWT_TOKEN"

curl -X POST http://localhost:8080/api/marketdata/mock/start \
  -H "Authorization: Bearer EXCHANGE_OP_JWT_TOKEN"
```

**Verify**:

- [x] NSE instruments stop receiving ticks
- [x] BSE/MCX instruments continue ticking
- [x] Exchange console shows NSE state as "HOLIDAY"
- [x] Market Watch shows "Closed/Holiday" badge for NSE symbols

---

## Phase 5: Run Tests

### 5.1 Backend Unit Tests

```bash
./mvnw test -Dtest=PriceGeneratorTest
./mvnw test -Dtest=InstrumentStateTest
```

### 5.2 Backend Integration Tests

```bash
./mvnw verify -Dtest=MockMarketDataServiceIT
./mvnw verify -Dtest=MarketDataResourceIT
./mvnw verify -Dtest=WatchlistResourceIT
./mvnw verify -Dtest=MarketDataWebSocketIT
```

### 5.3 Frontend Tests

```bash
npm test -- market-watch.spec.tsx
```

### 5.4 E2E Tests (Cypress)

```bash
npm run e2e

# Run specific test:
npm run e2e:headless -- --spec "cypress/e2e/market-watch.cy.ts"
```

**Example Cypress Test** (`market-watch.cy.ts`):

```typescript
describe('Market Watch', () => {
  beforeEach(() => {
    cy.login('trader1', 'password');
  });

  it('should display live quotes', () => {
    cy.visit('/market-watch');
    cy.contains('SIMULATED FEED').should('be.visible');
    cy.contains('CONNECTED').should('be.visible');

    // Wait for at least one quote update
    cy.get('td')
      .contains(/\d+\.\d{2}/)
      .should('exist'); // LTP with 2 decimals

    // Verify color coding
    cy.get('tr.positive').should('exist'); // At least one positive change
  });

  it('should add instrument to watchlist', () => {
    cy.visit('/market-watch');
    cy.get('[data-testid=add-symbol-button]').click();
    cy.get('input[name=symbol]').type('INFY');
    cy.get('[data-testid=confirm-add]').click();

    cy.contains('INFY').should('be.visible');

    // Verify INFY starts receiving quotes
    cy.get('tr').contains('INFY').parent().find('td').eq(1).should('not.contain', '-');
  });
});
```

---

## Troubleshooting

### Issue: WebSocket Connection Fails

**Symptoms**: Connection status stuck on "CONNECTING" or "DISCONNECTED"

**Debugging**:

```bash
# Check browser console for STOMP errors
# Common issues:
# 1. JWT token expired → refresh token
# 2. CORS issue → verify WebSocketConfig.setAllowedOriginPatterns("*")
# 3. SockJS fallback failing → check /ws endpoint accessibility

# Test WebSocket endpoint directly:
curl -i -N -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Host: localhost:8080" \
  -H "Origin: http://localhost:9000" \
  http://localhost:8080/ws/websocket
```

### Issue: No Ticks Generated

**Symptoms**: Feed status shows "RUNNING" but no quotes arrive

**Debugging**:

```bash
# Check logs for:
tail -f target/jhipster.log | grep "MockMarketDataService"

# Look for:
# "Generating ticks for 1523 instruments"
# "Broadcasting quote for RELIANCE: 2485.30"

# Verify instruments loaded:
psql -h localhost -U rnexchange -d rnexchange \
  -c "SELECT COUNT(*) FROM instrument WHERE is_active = true;"

# Should return > 1000
```

### Issue: Quotes Not Updating in UI

**Symptoms**: WebSocket connected, backend logs show broadcasts, but UI frozen

**Debugging**:

```javascript
// In browser console:
// Check if Redux state is updating
window.__REDUX_DEVTOOLS_EXTENSION__?.monitor.getState().marketWatch.quotes;

// Verify WebSocket subscriptions:
// Open Network tab → Filter by WS → Check for MESSAGE frames

// Common fixes:
// 1. Throttle Redux dispatch (if CPU pegged)
// 2. Check React component re-render logic
// 3. Verify symbol case matches (RELIANCE vs reliance)
```

### Issue: Tests Fail with "Connection Refused"

**Symptoms**: Integration tests fail with WebSocket connection errors

**Fix**:

```java
// Ensure @SpringBootTest uses random port:
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// Use @LocalServerPort to get actual port:
@LocalServerPort
private int port;
// Connect to ws://localhost:{port}/ws, not hardcoded port 8080

```

---

## Performance Validation

### Load Test with Gatling

**Scenario A (Phase 3B core gate)**: Operator control + feed status SLA under load

```bash
./mvnw gatling:test -Dgatling.simulationClass=gatling.simulations.MockMarketDataFeedStatusGatlingTest
```

This simulation drives concurrent start/stop/status calls against the mock feed and asserts:

- p95 latency for control & status endpoints < 500 ms
- Successful requests ≥ 99.9% (proxy for <0.1% message loss at the API layer)

**Scenario B (Reconnect SLA proxy)**: Reconnect-style status polling

```bash
./mvnw gatling:test -Dgatling.simulationClass=gatling.simulations.MockMarketDataReconnectGatlingTest
```

This simulation repeatedly polls the mock feed status with jittered pauses and asserts:

- p99 latency < 30 s, mirroring the WebSocket reconnect SLA
- Successful requests ≥ 99%

Run these Gatling simulations in CI and treat failures as a hard gate before starting Phase 4 (Market Watch UI).

---

## Next Steps

After verifying all tests pass and acceptance criteria are met:

1. **Code Review**: Submit PR for `002-mock-market-data` branch
2. **Documentation**: Update main README with Market Watch feature
3. **Deployment**: Merge to `main` and deploy to staging environment
4. **User Validation**: Product owner validates against acceptance scenarios (spec.md)
5. **Proceed to M2**: Begin implementing Trading Core (order placement, matching, positions)

---

**Feature Complete Checklist**:

- [ ] All unit tests pass (90%+ backend coverage)
- [ ] All integration tests pass
- [ ] All E2E tests pass (Cypress)
- [ ] Manual testing scenarios validated (Exchange Operator + Trader)
- [ ] WebSocket connection resilience tested (disconnect/reconnect)
- [ ] Holiday handling verified (ticks stop for closed exchanges)
- [ ] Performance targets met (Gatling load test)
- [ ] Code review approved
- [ ] Documentation updated

**Congratulations!** M1 Mock Market Data & Market Watch is complete. 🎉
