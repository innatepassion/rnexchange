# Feature Specification: Mock Market Data & Market Watch

**Feature Branch**: `002-mock-market-data`  
**Created**: 2025-11-13  
**Status**: Draft  
**Input**: User description: "For M1 – Mock Market Data & Market Watch, the developer should take the clean M0 seed as a starting point and implement a full, end-to-end simulated market data pipeline that the Trader and Exchange Operator can both see and control: create a MockMarketDataService (or similar) in the backend that loads all active Instrument rows for NSE, BSE, and MCX, and for each instrument maintains an in-memory lastPrice, open, high, low, volume, and lastUpdated structure; on application startup, whenever at least one exchange is open, this service should immediately begin a scheduled job (e.g. every 100–1000 ms, with the ability to emit batched ticks per cycle) that applies a calibrated random-walk to each instrument’s last price (bounded so it doesn’t go negative and with a configurable volatility factor per exchangeCode / assetClass), updates OHLC and volume counters for the current 1-minute bar, and then publishes a quote DTO over a Spring WebSocket/STOMP topic such as /topic/quotes/{symbol} and a bar DTO over /topic/bars/{symbol}; exchange operators retain the ability to pause or resume the feed via REST endpoints like POST /api/marketdata/mock/start, POST /api/marketdata/mock/stop, and GET /api/marketdata/mock/status so that from the Exchange Console they can control the simulated feed, and the generator must respect the MarketHoliday table and any basic trading session config (i.e. don’t generate ticks for instruments whose exchange is on holiday); on the frontend, implement the Trader Market Watch screen so that when a Trader logs in and navigates to “Market Watch” they can (a) select one of their watchlists, (b) see a table of symbols with LTP, % change, volume and last updated time, all driven by subscribing to the WebSocket topics for those instruments, and (c) add/remove instruments from the watchlist via REST calls like POST /api/watchlists/{id}/items and DELETE /api/watchlists/{id}/items/{symbol}; for performance and clarity, have the React client display the server-provided percent change while applying tick-to-tick color-coding (green/red/gray), show a clear “SIMULATED FEED” badge at the top of the Market Watch, and add a small WS status indicator (Connected / Reconnecting / Disconnected) so Traders can see if live updates are flowing; finally, add a simple Exchange Operator “Market Data” panel that lists the three exchanges (NSE, BSE, MCX) with their current feed status (Running/Stopped), last tick time, and ticks-per-second derived from the mock service, and validate the phase by logging in as exchange_op to confirm automatic startup when eligible, logging in as trader1 to open Market Watch and confirm that NSE/BSE/MCX instruments from the seed data are ticking in real time with volatility staying within the configured ±5% per rolling minute band, with no stale or cross-contaminated data across exchanges."

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Exchange Operator Activates Simulated Feed (Priority: P1)

The exchange operator signs into the console, reviews the current mock market data status, and starts or stops the simulated feed for all active exchanges when required.

**Why this priority**: Without the operator’s ability to control the feed, no simulated prices reach traders, making downstream experiences impossible.

**Independent Test**: Can be fully tested by logging in as an exchange operator, toggling feed state, and observing status transitions without needing trader interaction.

**Acceptance Scenarios**:

1. **Given** the mock feed is stopped, **When** the operator issues a start command, **Then** the system reports the feed as running within 2 seconds and records the activation timestamp.
2. **Given** the mock feed is running, **When** the operator issues a stop command, **Then** the system halts price generation, updates status to stopped, and preserves the last tick time for each exchange.

---

### User Story 2 - Trader Monitors Live Watchlist Prices (Priority: P2)

The trader selects one of their watchlists, views live last-traded prices, percent change, volume, and last update time per instrument, and relies on the simulated feed indicator and connection status badge to confirm data freshness.

**Why this priority**: Traders need confidence that the simulated market behaves like a live feed so they can validate workflows and training scenarios.

**Independent Test**: Can be fully tested by logging in as a trader, opening Market Watch, and verifying that watchlist instruments update in real time with accurate calculations and status cues.

**Acceptance Scenarios**:

1. **Given** the mock feed is running, **When** a trader opens Market Watch and selects a populated watchlist, **Then** each instrument displays LTP, percent change, volume, and last update time that refresh automatically without manual reload.
2. **Given** the WebSocket connection drops, **When** the client attempts reconnection, **Then** the bottom-right traffic-light status indicator shows “Reconnecting” with an amber dot or “Disconnected” with a red dot until a successful reconnect updates the view with a green dot and fresh data.

---

### User Story 3 - Trader Manages Watchlist Membership (Priority: P3)

The trader adds or removes instruments from a chosen watchlist and immediately sees the table adjust, with new instruments beginning to stream live updates once added.

**Why this priority**: Traders need flexibility to tailor their watchlists quickly while observing the simulated feed response.

**Independent Test**: Can be fully tested by performing REST-based add/remove actions for a watchlist while monitoring the table to confirm instruments appear or disappear and receive updates promptly.

**Acceptance Scenarios**:

1. **Given** a trader selects a watchlist, **When** they add an eligible instrument, **Then** the instrument appears in the table within 2 seconds and begins displaying simulated quotes.
2. **Given** a trader is viewing a watchlist entry, **When** they remove that instrument, **Then** it disappears from the table and no longer receives live updates.

---

### Edge Cases

- What happens when an exchange is marked as a holiday during an active session?
- How does the system handle instruments lacking recent tick data at feed start?
- What occurs when a trader selects an empty watchlist or a watchlist with instruments from a stopped exchange?
- How are price movements constrained if the random walk attempts to push values below zero or beyond configured bounds?
- How is the ±5% per rolling minute volatility band enforced when batch emission is enabled?
- What feedback is shown when the feed is disabled while a trader is viewing Market Watch?
- How does the interface signal that quotes are frozen because an exchange is observing a holiday?

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The platform MUST load all active instruments for NSE, BSE, and MCX at feed initialization and maintain an in-memory state per instrument containing last price, open, high, low, cumulative volume, and last update timestamp. At session open, each instrument's open price MUST be initialized to its last closing price (or the application-level default price `marketdata.mock.defaultPrice`, default 100.00, if no prior close exists); every fallback application MUST trigger an audit log entry that includes the instrument symbol, exchange, and applied default.
- **FR-002**: The mock generator MUST apply a bounded random-walk adjustment to each instrument’s last price at a configurable interval between 100 ms and 1,000 ms, supporting emission of up to 10 batched ticks per cycle so the system can meet the 10,000 updates-per-second throughput target. Each tick delta MUST satisfy `abs(delta) ≤ volatilityMultiplier * lastPrice` with a default volatility multiplier of 0.004 (±0.4%) and a hard clamp that prevents prices from falling below `marketdata.mock.minPrice` (default 1.00) or exceeding `marketdata.mock.maxPrice` (default 10,000.00); the interval and batch size MUST be managed via application properties (`marketdata.mock.intervalMs`, `marketdata.mock.batchSize`) and validated at startup, and any out-of-bounds configuration MUST fail application startup with a configuration exception instead of auto-correcting values.
- **FR-002a**: The mock generator MUST log and expose via metrics whenever price clamping occurs (floor or ceiling) so operators can review when the bounded random-walk rejected a delta and confirm volatility configuration.
- **FR-002b**: The mock generator MUST enforce a rolling 60-second volatility band per exchange and instrument such that cumulative change across the window stays within ±5% of the anchor price (default threshold configurable via `marketdata.mock.volatilityBandPercent`). When the band is exceeded, the generator MUST suppress further drift in the offending direction until the window re-enters bounds, emit a structured log/metric describing the constraint, and surface the active band state via the feed status payload.
- **FR-003**: Volatility multipliers MUST be configurable per exchange and asset class via application properties and overridden by the persisted `exchange_volatility_override` table when available; the service MUST log which source supplied the active volatility factor.
- **FR-004**: Exchange operators MUST be able to start, stop, and retrieve the status of the mock feed via REST endpoints (`POST /api/marketdata/mock/start`, `POST /api/marketdata/mock/stop`, `GET /api/marketdata/mock/status`), each guarded by the `EXCHANGE_OPERATOR` role and responding within 2 seconds with the updated feed state.
- **FR-005**: The status endpoint MUST return a `FeedStatus` payload that includes global feed state, activation timestamp, per-exchange last tick time, active instrument count, and ticks-per-second moving average.
- **FR-006**: The system MUST broadcast per-symbol quote updates to `/topic/quotes/{symbol}` on every tick and publish 60-second OHLC bar updates to `/topic/bars/{symbol}`; both payloads MUST conform to the contracts documented in `contracts/websocket-topics.md`.
- **FR-007**: The WebSocket handshake MUST validate JWT tokens and reject unauthorized connections; `SUBSCRIBE` frames MUST enforce RBAC so that only traders and exchange operators receive authorized topics.
- **FR-008**: The mock feed MUST respect market holidays and configured trading sessions by skipping tick generation for exchanges that are closed, logging the reason, and leaving existing prices unchanged until trading resumes.
- **FR-009**: The trader Market Watch view MUST render a table showing Symbol, Last Traded Price (LTP), absolute change, percent change, cumulative volume, and last updated timestamp sourced directly from the live quote payload.
- **FR-010**: The trader Market Watch view MUST display tick-to-tick row color coding (green for positive, red for negative, gray for zero change), a persistent “SIMULATED FEED” banner, and a WebSocket connection status indicator. The indicator MUST appear in the bottom-right corner of the Market Watch screen as a 16px traffic-light dot with accompanying label: green for Connected, amber for Reconnecting, red for Disconnected, each providing an accessible tooltip describing the connection state.
- **FR-011**: The Market Watch UI MUST surface feed pause or holiday states by freezing affected rows, displaying a “Closed/Holiday” badge, and indicating the last update timestamp while the feed is halted.
- **FR-012**: Traders MUST be able to select among their watchlists via the existing watchlist APIs, with an explicit empty-state message when a watchlist contains no instruments.
- **FR-013**: The mock feed MUST auto-start on application launch when at least one exchange is open and remain stopped (with reason logged) when all exchanges are closed or on holiday.
- **FR-014**: Traders MUST be able to add instruments to a watchlist via `POST /api/watchlists/{id}/items` and receive live quotes within 2 seconds; instruments removed via `DELETE /api/watchlists/{id}/items/{symbol}` MUST disappear and stop streaming within 2 seconds.
- **FR-015**: Market Watch and the Exchange Operator console MUST provide educational tooltips explaining simulated feed behavior, metric definitions, and status indicators in alignment with the Educational Transparency principle.
- **FR-016**: Feed status reporting MUST calculate ticks-per-second using a 5-second moving average per exchange and expose the metric through both the REST status endpoint and the operator console UI.
- **FR-017**: WebSocket subscriptions for `/topic/quotes/{symbol}` and `/topic/bars/{symbol}` MUST restrict traders to instruments present in their own watchlists, returning an authorization error when access is attempted for other symbols.
- **FR-018**: Significant feed state transitions (e.g., auto-start, manual start/stop, guard activation/release) MUST publish domain events (`FeedStartedEvent`, `FeedStoppedEvent`, `VolatilityGuardTriggeredEvent`, etc.) that include exchange and timestamp context so downstream subscribers (audit logging, monitoring) can react; corresponding contract tests MUST validate event emission before implementation.

### Non-Functional Requirements

- **NFR-001 (Operator Responsiveness)**: Feed control endpoints MUST meet a p95 latency of ≤2 seconds under load (1,000 concurrent operator requests), failing the pipeline otherwise.
- **NFR-002 (Client Reconnection)**: The WebSocket client MUST automatically reconnect with exponential backoff and recover within 30 seconds for 99% of disconnections.
- **NFR-003 (Operational Transparency)**: All fallback behaviors (default pricing, volatility source selection, holiday gating) MUST emit structured INFO logs that include exchange/symbol context for operator review.
- **NFR-004 (Test Coverage)**: Backend services introduced by this feature MUST maintain ≥90% unit/integration test coverage; frontend modules MUST maintain ≥80% component/test coverage.
- **NFR-005 (Throughput)**: The system MUST sustain at least 10,000 tick updates per second across all exchanges with <500 ms p95 WebSocket delivery latency and ≤0.1% message loss by dynamically tuning interval and batch size parameters; throughput verification MUST block promotion to trader-facing phases.
