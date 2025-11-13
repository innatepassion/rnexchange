# Feature Specification: Mock Market Data & Market Watch

**Feature Branch**: `002-mock-market-data`  
**Created**: 2025-11-13  
**Status**: Draft  
**Input**: User description: "For M1 – Mock Market Data & Market Watch, the developer should take the clean M0 seed as a starting point and implement a full, end-to-end simulated market data pipeline that the Trader and Exchange Operator can both see and control: create a MockMarketDataService (or similar) in the backend that loads all active Instrument rows for NSE, BSE, and MCX, and for each instrument maintains an in-memory lastPrice, open, high, low, volume, and lastUpdated structure; on application startup (or when enabled by the Exchange Operator), this service should start a scheduled job (e.g. every 500–1000 ms) that applies a simple random-walk to each instrument’s last price (bounded so it doesn’t go negative and with a configurable volatility factor per exchangeCode / assetClass), updates OHLC and volume counters for the current 1-minute bar, and then publishes a quote DTO over a Spring WebSocket/STOMP topic such as /topic/quotes/{symbol} and a bar DTO over /topic/bars/{symbol}; expose a small REST endpoint for the Exchange Operator like POST /api/marketdata/mock/start and POST /api/marketdata/mock/stop plus GET /api/marketdata/mock/status so that from the Exchange Console they can start/stop the simulated feed and see whether it is running, and ensure the generator respects the MarketHoliday table and any basic trading session config (i.e. don’t generate ticks for instruments whose exchange is on holiday); on the frontend, implement the Trader Market Watch screen so that when a Trader logs in and navigates to “Market Watch” they can (a) select one of their watchlists, (b) see a table of symbols with LTP, % change, volume and last updated time, all driven by subscribing to the WebSocket topics for those instruments, and (c) add/remove instruments from the watchlist via REST calls like POST /api/watchlists/{id}/items and DELETE /api/watchlists/{id}/items/{symbol}; for performance and clarity, have the React client compute % change and color-coding (green/red/gray) from the last tick, show a clear “SIMULATED FEED” badge at the top of the Market Watch, and add a small WS status indicator (Connected / Reconnecting / Disconnected) so Traders can see if live updates are flowing; finally, add a simple Exchange Operator “Market Data” panel that lists the three exchanges (NSE, BSE, MCX) with their current feed status (Running/Stopped, last tick time, tick rate per second) derived from the mock service, and validate the phase by logging in as exchange_op to start the feed, logging in as trader1 to open Market Watch and confirm that NSE/BSE/MCX instruments from the seed data are ticking in real time with plausible price movement and no stale or cross-contaminated data across exchanges."

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
2. **Given** the WebSocket connection drops, **When** the client attempts reconnection, **Then** the status indicator shows “Reconnecting” or “Disconnected” until a successful reconnect updates the view with fresh data.

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
- What feedback is shown when the feed is disabled while a trader is viewing Market Watch?

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The platform MUST load all active instruments for NSE, BSE, and MCX at feed initialization and maintain an in-memory state per instrument containing last price, open, high, low, cumulative volume, and last update timestamp.
- **FR-002**: The mock generator MUST apply a bounded random-walk adjustment to each instrument’s last price at a configurable interval between 500 ms and 1,000 ms, preventing negative or zero prices.
- **FR-003**: The system MUST support configurable volatility factors per exchange or asset class that influence price step size.
- **FR-004**: The generator MUST respect MarketHoliday records and any defined trading session windows so that instruments paused for the day do not receive simulated ticks.
- **FR-005**: The platform MUST publish real-time quote updates for each instrument, containing price, percent change from the session open, rolling volume, and timestamp, over a subscription channel addressable by symbol.
- **FR-006**: The platform MUST emit one-minute bar summaries per instrument reflecting open, high, low, close, and aggregated volume for the active minute over a distinct subscription channel.
- **FR-007**: The Exchange Operator interface MUST expose start, stop, and status REST endpoints that return whether the feed is running, per-exchange last tick timestamps, and active tick rate.
- **FR-008**: The operator console MUST display a panel listing NSE, BSE, and MCX with feed status (Running/Stopped), last tick time, and ticks per second as reported by the mock service.
- **FR-009**: The trader Market Watch MUST allow selection among the trader’s existing watchlists and display LTP, percent change, volume, and last update time for each listed instrument.
- **FR-010**: The trader interface MUST compute percent change and row color-coding locally using successive ticks, with a persistent “Simulated Feed” banner and WebSocket connection status indicator.
- **FR-011**: The system MUST provide endpoints for traders to add instruments to a watchlist and remove them, providing immediate UI confirmation when the server acknowledges the change.
- **FR-012**: The client MUST subscribe only to the symbols present in the active watchlist and unsubscribe when instruments are removed to avoid cross-contamination between exchanges.

### Key Entities _(include if feature involves data)_

- **Instrument Feed State**: Represents the current simulated values for an instrument, including last price, open/high/low, cumulative volume, last update time, and assigned volatility factor.
- **Quote Update**: Represents an incremental tick sent to subscribers, capturing instrument identifier, price, percent change from open, incremental volume, and timestamp.
- **Minute Bar Summary**: Represents aggregated data for a one-minute interval per instrument, including open, high, low, close, and total volume.
- **Watchlist**: Represents a trader-managed collection of instrument symbols linked to a user account and used to drive subscription lists.
- **Exchange Feed Status**: Represents per-exchange metadata surfaced to operators, including current run state, last tick time, and recent tick rate.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: Exchange operators can start or stop the simulated feed and see the console status update within 2 seconds of their action in 95% of attempts during testing.
- **SC-002**: When the feed is running, traders viewing Market Watch receive refreshed price data for all subscribed instruments at least once every second with no cross-exchange leakage in 99% of observed ticks.
- **SC-003**: Traders can add or remove an instrument from their watchlist and observe the table update along with live data subscription changes within 3 seconds in 95% of test cases.
- **SC-004**: When an exchange is on holiday or outside its trading session, no simulated ticks are generated for its instruments, as validated across a full-day simulation with zero violations.
