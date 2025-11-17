# Feature Specification: M6 QA Hardening & Demo-Ready Polish

**Feature Branch**: `006-qa-hardening-demo`  
**Created**: 2025-11-17  
**Status**: Draft  
**Input**: User description: "M6 – QA, Hardening & Demo-Ready Polish (stabilization, automated critical flows, demo-ready)."

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Trader day trade flow is reliable (Priority: P1)

A trader can log in, curate a market watchlist, place a market order in a commonly traded symbol (e.g., RELIANCE on NSE), see it fill, and immediately see both positions and cash ledger updated in a predictable way.

**Why this priority**: This is the core value of the simulator; if traders cannot place and verify trades end-to-end, the product cannot be demoed or evaluated.

**Independent Test**: Can be tested end-to-end with a single trader account by running through login → watchlist update → order placement → fill → position and ledger verification, without requiring broker or exchange operator actions.

**Acceptance Scenarios**:

1. **Given** a valid trader account with sufficient buying power, **When** the trader logs in, adds RELIANCE on NSE to their watchlist, and submits a market buy order, **Then** the order is accepted, filled, and the new position appears in the trader’s portfolio view.
2. **Given** a filled order in RELIANCE on NSE, **When** the trader navigates to their cash/ledger view, **Then** the cash movement (debit for buy, fees if applicable) and position quantity are reflected consistently and match the trade details.

---

### User Story 2 - Broker adjusts trader funds safely (Priority: P2)

A broker admin can log in, credit funds to a trader via a funds journal entry, and immediately see the trader’s increased buying power and updated ledger.

**Why this priority**: Broker controls over client cash are required to set up demo accounts and realistic trading sessions without manipulating raw data.

**Independent Test**: Can be tested with a broker admin account and a single trader account by performing a funds credit and validating buying power and ledger entries, without needing order placement or exchange operator actions.

**Acceptance Scenarios**:

1. **Given** a broker admin and an existing trader, **When** the broker admin logs in and posts a funds credit journal entry to that trader for a specified amount, **Then** the trader’s available cash and buying power increase by that amount.
2. **Given** a completed funds credit to a trader, **When** the broker or trader views the trader’s ledger, **Then** a clearly labeled journal entry appears with the correct amount, date, and description, and balances before/after reconcile.

---

### User Story 3 - Exchange operator runs EOD and statements (Priority: P3)

An exchange operator can log in, run end-of-day processing for the current trading day, and traders and brokers can open generated daily statements that show consistent balances, P&L, and cash movements.

**Why this priority**: End-of-day statements are critical for demonstrating that trades, cash adjustments, and valuations roll up into a coherent daily picture.

**Independent Test**: Can be tested by running a full trading day with at least one trader and broker, then running EOD and verifying that generated statements match ledger and position data without requiring new orders or fund movements.

**Acceptance Scenarios**:

1. **Given** completed trades and funds journal entries for the day, **When** the exchange operator runs the EOD process for that date, **Then** a daily statement is generated for each trader and broker containing starting balance, ending balance, P&L, and key cash movements.
2. **Given** a generated daily statement for a trader and broker, **When** they open the statement from their respective UIs, **Then** all balances and P&L figures reconcile with on-screen positions and ledger history for that day.

---

### User Story 4 - Demo-ready UX and role-based navigation (Priority: P2)

A team member using fixed demo users can quickly navigate and demonstrate a full “day in the life” across Trader, Broker, and Exchange Operator roles without confusion, raw data edits, or manual configuration.

**Why this priority**: The milestone is explicitly about being demo-ready; anyone on the team should be able to run a polished demo confidently.

**Independent Test**: Can be tested by starting from a fresh environment, logging in as `trader_demo`, `broker_demo`, and `exchange_demo`, and completing the scripted demo steps using only the UI, while observing clear banners, predictable landing pages, and sensible error/loading/empty states.

**Acceptance Scenarios**:

1. **Given** the predefined demo users are present, **When** a team member starts the app and logs in as each role, **Then** the Trader lands on Market Watch, the Broker lands on their Dashboard, and the Exchange Operator lands on an Exchange Overview screen, all with a visible “SIMULATED / NOT REAL MONEY” indicator for Trader and Broker.
2. **Given** a team member is following the demo script, **When** they place a trade, adjust funds, run EOD, and open statements using the demo users, **Then** they can complete the entire flow without editing configuration files, seeding data manually, or encountering confusing error messages or missing loading/empty states.

---

### Edge Cases

- What happens when EOD is run more than once for the same date (idempotency and consistent statements)?
- How does the system handle a trader with no trades or journal entries on a given day when generating statements (empty or minimal statements should still be valid and readable)?
- What happens if a broker attempts a funds journal entry that would result in a negative balance or violates internal limits?
- How does the system behave when order placement is attempted during heavy simulated load (orders should still be accepted, processed, and reflected without user-facing failures)?
- What happens when a demo user attempts an action outside their role (e.g., trader trying to access broker-only screens; access should be blocked with a clear message)?

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST allow authenticated traders to log in and manage a personal market watchlist, including adding and removing symbols such as RELIANCE on NSE.
- **FR-002**: The system MUST allow traders to place market orders in supported instruments and, upon execution, MUST update the trader’s positions and cash ledger consistently and immediately in the UI.
- **FR-003**: The system MUST ensure that role-based access is explicit and enforced for at least three roles: Trader, Broker Admin, and Exchange Operator, preventing access to restricted actions and screens for other roles.
- **FR-004**: The system MUST allow broker admins to create funds journal entries (credits and debits) for a trader and MUST update the trader’s buying power and ledger balances accordingly.
- **FR-005**: The system MUST allow an exchange operator to run an end-of-day process for a specific date that consolidates trades, positions, and journal entries into generated daily statements for traders and brokers.
- **FR-006**: The system MUST provide traders and brokers with access to their generated daily statements via the UI and MUST display balances, P&L, and key cash movements that reconcile with positions and ledger history.
- **FR-007**: The system MUST display a clear, always-visible "SIMULATED / NOT REAL MONEY" banner or equivalent warning on primary Trader and Broker views.
- **FR-008**: The system MUST define predictable default landing pages by role: Trader → Market Watch, Broker Admin → Broker Dashboard, Exchange Operator → Exchange Overview.
- **FR-009**: The system MUST address at least five high-friction UX "paper-cut" issues (e.g., unclear error messages, missing loading states, missing empty-state messages, or ambiguous button labels) such that core demo flows can be completed without confusion.
- **FR-010**: The system MUST provide automated end-to-end tests that cover at minimum: (a) the trader trade/ledger flow, (b) the broker funds journal and ledger flow, and (c) the exchange operator EOD and statement viewing flow.
- **FR-011**: The system MUST support basic performance testing of concurrent order placement and quote streaming that demonstrates acceptable responsiveness for a demo-scale environment (approximately 1,000 simulated concurrent traders, hundreds of quotes per second, and 5–10 orders per second).
- **FR-012**: The system MUST provide fixed demo users (e.g., `trader_demo`, `broker_demo`, `exchange_demo`) with stable starting balances and positions so that demo flows are repeatable across environments without manual data manipulation.

### Key Entities _(include if feature involves data)_

- **Trader**: Represents an end user placing simulated trades; key attributes include identity, credentials, role, buying power, open positions, and ledger history.
- **Broker Admin / Broker**: Represents an administrative user responsible for managing traders’ cash and overseeing accounts; key attributes include identity, role, and access to a set of trader accounts and journal entries.
- **Exchange Operator**: Represents an operational user who can run closing processes and oversee system-wide trading-day health; key attributes include identity, role, and access to EOD tools and reports.
- **Order**: Represents a requested trade by a trader, including symbol, side, quantity, price type (e.g., market), status, execution details, and timestamps.
- **Position**: Represents the aggregated holdings for a trader in a given instrument, including quantity, average price, and mark-to-market values used for P&L.
- **Ledger Entry / Funds Journal Entry**: Represents a cash movement (e.g., trade settlement, fees, broker credits/debits) with date, amount, type, description, and running balance.
- **Daily Statement**: Represents a generated end-of-day summary for a trader or broker, including opening balance, closing balance, realized/unrealized P&L, and key cash movements.
- **Watchlist**: Represents a list of instruments a trader is monitoring, including symbol identifiers and ordering preferences.
- **Demo User Configuration**: Represents predefined demo accounts, their starting balances, positions, and any special flags to keep their state stable and resettable.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: A team member can start from a standard development environment, launch the application, log in as `trader_demo`, `broker_demo`, and `exchange_demo`, and complete a full "day in the life" demo (place trade, adjust funds, run EOD, open statements) in under 15 minutes without editing configuration files or raw data.
- **SC-002**: The three critical end-to-end flows (trader trade/ledger, broker funds journal/ledger, exchange operator EOD/statements) are covered by automated tests that pass reliably in the continuous integration pipeline (e.g., no intermittent failures across multiple runs).
- **SC-003**: Under a realistic simulated load approximating 1,000 concurrent traders with hundreds of quotes per second and 5–10 orders per second, users experience order placement and core UI interactions completing within a few hundred milliseconds in typical cases, and without demo-breaking errors or timeouts.
- **SC-004**: During internal dry-run demos, participants report no blocking usability issues in the core flows, and the presence of "SIMULATED / NOT REAL MONEY" warnings and improved error/loading/empty states is sufficient for the product to be presented as a credible technology preview without further architecture changes.

### Assumptions & Dependencies

- The underlying authentication and authorization mechanisms already exist; this feature focuses on clarifying and enforcing role behaviors and navigation rather than redefining auth.
- Performance and load expectations are evaluated on a representative "target dev hardware" baseline agreed by the team, not on minimal or resource-constrained machines.
- Market data (quotes) and trade matching are already available in the simulator; this feature focuses on reliability, visibility of results in positions/ledgers/statements, and demo readiness rather than adding new instruments or markets.
- Demo user accounts and starting data can be seeded or reset through existing tooling or scripts outside the scope of the demo run itself.
