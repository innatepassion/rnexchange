# Data Model – M6 QA Hardening, Branding & Demo-Ready Polish

This document focuses on the entities and relationships most relevant to the M6 QA Hardening & Demo-Ready Polish feature.  
It builds on the existing RNExchange domain model; new or emphasised aspects for M6 are called out explicitly.

---

## Core Actors & Accounts

### Terminology Alignment with Constitution (DDD)

To align with the RNExchange constitution’s DDD entity definitions:

- “Trader” in this feature maps to the `TraderProfile` and `TradingAccount` aggregates.
- “Broker Admin / Broker” maps to the `Broker` entity plus associated trader accounts.
- “Ledger Entry / Funds Journal Entry” maps to the `LedgerEntry` entity.
- “Daily Statement” maps to the `SettlementBatch` aggregate plus rendered statement views.

Future specs, plans, and tasks for this area SHOULD prefer the constitution’s ubiquitous language (`TraderProfile`, `TradingAccount`, `Broker`, `SettlementBatch`, `LedgerEntry`) where appropriate.

### Trader

- **Description**: End user placing simulated trades.
- **Backed by**: `User` + `TraderProfile` + `TradingAccount` domain entities.
- **Key fields (effective for M6)**:
  - `id` (UUID or Long) – identity key.
  - `login` – unique username (e.g., `trader_demo`).
  - `authorities` – includes `TRADER`.
  - `tradingAccountId` – primary trading account.
- **M6-specific behaviour**:
  - Must see a persistent “SIMULATED / NOT REAL MONEY” banner on primary trading screens.
  - Lands on Market Watch after login.
  - Has access to statements via `/trader/statements` backed by `/api/statements`.

### Broker Admin / Broker

- **Description**: Administrative user who manages traders and cash movements.
- **Backed by**: `User` + `Broker` + `TradingAccount` / `LedgerEntry`.
- **Key fields**:
  - `id`, `login`, `authorities` including `BROKER_ADMIN`.
  - Association to one `Broker` entity and a set of trader accounts.
- **M6-specific behaviour**:
  - Can create funds journal entries (credits/debits) for trader accounts.
  - Must see negative or at-risk balances clearly flagged (e.g., after a debit pushing balance below zero).
  - Lands on Broker Dashboard after login; navigation hides generic “Entities” and unrelated JHipster items.

### Exchange Operator

- **Description**: Operational user responsible for running EOD and overseeing system health.
- **Backed by**: `User` + `ExchangeOperator`.
- **Key fields**:
  - `id`, `login`, `authorities` including `EXCHANGE_OPERATOR`.
- **M6-specific behaviour**:
  - Can trigger EOD via `/api/eod?date=YYYY-MM-DD`.
  - Views EOD settlement batches and high-level metrics in an Exchange Overview / console.

---

## Trading & Positions

### Order

- **Description**: Trader’s instruction to buy or sell a given instrument.
- **Key fields**:
  - `id`, `tradingAccountId`, `instrumentId`.
  - `side` (buy/sell), `type` (market, etc.), `quantity`, `price` (if applicable).
  - `status` (new, filled, partially filled, cancelled).
  - `createdAt`, `updatedAt`, `executionTime`.
- **Relationships**:
  - Many Orders belong to one `TradingAccount`.
  - Orders generate `Execution` and `LedgerEntry` records as they fill.
- **M6 focus**:
  - Ensure that successful order placement reliably updates `Position` and `LedgerEntry` and that Cypress tests cover this flow end-to-end.

### Position

- **Description**: Aggregated holdings for a trader in a given instrument.
- **Key fields**:
  - `id`, `tradingAccountId`, `instrumentId`.
  - `quantity`, `avgPrice`, `mtmPrice`, `unrealisedPnl`.
- **Relationships**:
  - One Position per (tradingAccount, instrument) pair.
  - Updated by trading engine and EOD settlement.
- **M6 focus**:
  - EOD must consistently revalue positions and feed MTM into statements; tests should verify reconciliation between positions, ledger entries, and statement P&L.

---

## Cash, Ledger, and Journal Entries

### LedgerEntry / Funds Journal Entry

- **Description**: Records cash movements, including trades, fees, and manual broker adjustments.
- **Key fields** (existing, emphasised for M6):
  - `id`, `tradingAccountId`, `refDate`.
  - `amount` (positive or negative), `currency`.
  - `type` (trade debit/credit, fee, deposit, withdrawal, `EOD_MTM_CREDIT`, `EOD_MTM_DEBIT`, etc.).
  - `description`, `reference`.
  - `runningBalance` (optional, derived or stored).
- **Relationships**:
  - Many LedgerEntries per `TradingAccount`.
  - Certain entries are created by EOD settlement batches.
- **M6-specific requirements**:
  - Broker-created journal entries MUST be allowed even if they push `runningBalance` negative or violate internal limits.
  - Negative or at-risk states MUST be surfaced:
    - In UI: flags/badges on trader account and ledger views.
    - In statements: clear indicators that balance is negative or below margin/limit thresholds.

---

## EOD, Settlement, and Statements

### SettlementBatch

- **Description**: Represents an EOD settlement run for a given trade date.
- **Key fields**:
  - `id`, `refDate` (trade date), `kind` (e.g., `EOD`).
  - `remarks` (JSON with metrics such as accounts processed, positions processed, net P&L).
- **Relationships**:
  - Associated with many `LedgerEntry` adjustments (EOD MTM credits/debits).
  - Source for broker/trader statements for the day.
- **M6 focus**:
  - EOD runs must be idempotent per `(refDate)`: rerunning EOD for the same date recomputes and replaces previous MTM adjustments and statements rather than duplicating them.

### StatementSummary

- **Description**: DTO representing a summarised daily statement as exposed via `/api/statements`.
- **Key fields** (from existing implementation, emphasised for M6):
  - `id`.
  - `refDate`.
  - `tradingAccountId` (or account identifier).
  - `openingBalance`.
  - `eodMtmPnl` (EOD mark-to-market P&L).
  - `closingBalance`.
  - `htmlUrl` – link to the full HTML statement document.
- **Relationships**:
  - One StatementSummary per (account, date) for which EOD has been successfully run.
- **M6-specific behaviour**:
  - For days with no activity, still emit a minimal StatementSummary showing unchanged balances and clear “no activity” messaging in HTML.
  - For negative/at-risk accounts, ensure the HTML and any summary fields surface this state clearly (e.g., warning row, badge, or note).

---

## Watchlists & Market Data

### Watchlist

- **Description**: A list of instruments the trader monitors on the Market Watch screen.
- **Key fields**:
  - `id`, `traderId` or `tradingAccountId`.
  - Collection of instrument identifiers and optional ordering preferences.
- **Relationships**:
  - One primary watchlist per trader, with many instruments.
- **M6 focus**:
  - Ensure watchlist changes are low-friction and reliably persisted; Cypress tests should cover add/remove and E2E tying watchlist → order → position/ledger updates.

### Real-Time Market Data (WebSockets)

- **Description**: Streaming mock market data and order/portfolio updates via WebSockets.
- **Key aspects**:
  - Topics for orders (e.g., `/topic/orders/{userId}`) and price ticks.
  - JWT-authenticated WebSocket handshake and reconnection with backoff.
- **M6 focus**:
  - QA must verify that heavy simulated load does not break WebSocket-driven order status and portfolio updates; Gatling + Cypress will be used to observe behaviour under load.

---

## Demo Users & Configuration

### Demo User Configuration

- **Description**: Conceptual configuration for fixed demo users with known starting balances and positions.
- **Representation**:
  - Likely implemented via Liquibase seed data or baseline profiles, not necessarily a dedicated runtime entity.
- **Key attributes**:
  - `username` (e.g., `trader_demo`, `broker_demo`, `exchange_demo`).
  - Initial balances and open positions.
  - Flags controlling whether their state is resettable between demos.
- **M6 focus**:
  - Provide deterministic seeds for demo users and document their use in `quickstart.md`.
  - Ensure automated tests use these demo accounts where appropriate to mirror real demo flows.

---

## State Transitions (High-Level)

1. **Trader trade flow**

   - Trader logs in → Market Watch loads with watchlist → Trader submits market order → Order is matched/executed → Position is updated → LedgerEntries are created for trade and fees → Trader’s dashboard and ledger views update in real-time → At EOD, SettlementBatch revalues positions and posts MTM entries → StatementSummary and HTML statements become available via `/api/statements`.

2. **Broker funds journal flow**

   - Broker logs in → Broker Dashboard → Broker selects a trader and posts a journal entry (credit/debit) → LedgerEntry created and running balance updated (even if negative) → Any negative or at-risk state is flagged in UI → At EOD, statements incorporate these journal entries and clearly show the resulting balances and risk flags.

3. **Exchange EOD and statement flow**
   - Exchange operator logs in → Exchange Overview → Operator runs EOD via `/api/eod?date=YYYY-MM-DD` → SettlementBatch created/updated, generating EOD MTM LedgerEntries and StatementSummary records → Traders and brokers can open statements via UI; rerunning EOD for the same date recomputes and replaces prior statements.
