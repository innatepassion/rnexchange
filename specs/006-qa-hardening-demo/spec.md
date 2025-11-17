# Feature Specification: M6 QA Hardening, Branding & Demo-Ready Polish

**Feature Branch**: `006-qa-hardening-demo`  
**Created**: 2025-11-17  
**Status**: Draft  
**Input**: User description: "M6 – QA, Hardening & Demo-Ready Polish (stabilization, automated critical flows, demo-ready). Refine to include RNExchange-branded landing page, per-role help guides, and removal of irrelevant JHipster UI elements so each role sees only relevant, production-ready navigation."

## Clarifications

### Session 2025-11-17

- Q: How should EOD behave if run more than once for the same date? → A: Idempotent; recompute and overwrite statements.
- Q: What should happen if a broker posts a funds journal entry that would otherwise push a trader’s balance negative or violate internal limits? → A: Allow entry but clearly flag negative/at-risk state.

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

### User Story 4 - Demo-ready UX, branding, and role-based navigation (Priority: P2)

A team member using fixed demo users can quickly navigate and demonstrate a full “day in the life” across Trader, Broker, and Exchange Operator roles without confusion, raw data edits, or manual configuration, starting from an RNExchange-branded landing experience rather than the generic JHipster welcome page.

**Why this priority**: The milestone is explicitly about being demo-ready; anyone on the team should be able to run an RNExchange demo that satisfies the measurable success criteria (SC-001–SC-004) for time-to-complete, automated coverage, performance, and usability, with clearly branded, role-appropriate navigation.

**Independent Test**: Can be tested by starting from a fresh environment, opening the application entry URL, confirming that the primary welcome/landing page shows RNExchange branding (logo and name) instead of a generic JHipster page, then logging in as `trader_demo`, `broker_demo`, and `exchange_demo`, and completing the scripted demo steps using only the UI, while observing clear banners, predictable landing pages, relevant navigation, and sensible error/loading/empty states.

**Acceptance Scenarios**:

1. **Given** a user opens the root URL of the application, **When** the welcome/landing page loads, **Then** it shows RNExchange branding (logo with "RNX", application name "RNExchange"), a short description of the simulator, and clear calls-to-action to log in with available roles (without any generic JHipster welcome content).
2. **Given** the predefined demo users are present, **When** a team member starts the app and logs in as each role, **Then** the Trader lands on Market Watch, the Broker lands on their Dashboard, and the Exchange Operator lands on an Exchange Overview screen, all with a visible “SIMULATED / NOT REAL MONEY” indicator for Trader and Broker, and without irrelevant JHipster menu items such as generic "Entities" or "Performance" that do not apply to that role.
3. **Given** a team member is following the demo script, **When** they place a trade, adjust funds, run EOD, and open statements using the demo users, **Then** they can complete the entire flow without editing configuration files, seeding data manually, or encountering confusing error messages, missing loading/empty states, or dead-end navigation.

---

### User Story 5 - Per-role “How to use RNExchange” help guides (Priority: P3)

Each login type (Trader, Broker Admin, Exchange Operator) sees a simple “How to use RNExchange” help section from their main dashboard or a clearly labeled help entry, acting as a lightweight user manual that explains key concepts and step-by-step flows for that role.

**Why this priority**: Having integrated, role-specific help makes the platform easier to adopt, reduces demo friction, and allows new users to self-serve basic questions without needing separate documentation.

**Independent Test**: Can be tested by logging in as each role and confirming that a consistent help pattern exists (e.g., “How to use RNExchange” or “Help” panel) that describes role responsibilities, key screens, and primary flows in plain language, without requiring navigation to external tools or raw documentation.

**Acceptance Scenarios**:

1. **Given** a trader is logged in and on their primary dashboard/landing page, **When** they open the “How to use RNExchange (Trader)” help section, **Then** they see a concise guide covering concepts such as watchlists, placing orders, viewing positions, and reviewing ledger/statement information, with links or cues to the relevant screens.
2. **Given** a broker admin is logged in, **When** they access the “How to use RNExchange (Broker Admin)” help section, **Then** they see guidance on managing traders, posting funds journal entries, reviewing risk and statements, and understanding what they can and cannot do compared to other roles.
3. **Given** an exchange operator is logged in, **When** they access the “How to use RNExchange (Exchange Operator)” help section, **Then** they see guidance on monitoring exchange health, running EOD, generating statements, and any other operational responsibilities, written in clear, non-technical language.

---

### Edge Cases

- When EOD is run more than once for the same date, it MUST behave idempotently: the system recomputes results and overwrites that date’s statements without double-counting trades or cash movements.
- How does the system handle a trader with no trades or journal entries on a given day when generating statements (empty or minimal statements should still be valid and readable)?
- If a broker attempts a funds journal entry that would result in a negative balance or violate internal limits, the system MUST still allow the entry but MUST clearly flag the trader account as negative or at-risk in both UI and statements, without silently masking the condition.
- How does the system behave when order placement is attempted during heavy simulated load (orders should still be accepted, processed, and reflected without user-facing failures)?
- What happens when a demo user attempts an action outside their role (e.g., trader trying to access broker-only screens; access should be blocked with a clear message)?
- For all generated trader and broker statements, the simulation disclaimer (e.g., "This is a simulated environment — not real trading or money") MUST remain visible even when printed or exported, and MUST NOT be removable via simple theme or CSS changes.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST allow authenticated traders to log in and manage a personal market watchlist, including adding and removing symbols such as RELIANCE on NSE.
- **FR-002**: The system MUST allow traders to place market orders in supported instruments and, upon execution, MUST update the trader’s positions and cash ledger consistently and immediately in the UI.
- **FR-003**: The system MUST ensure that role-based access is explicit and enforced for at least three roles: Trader, Broker Admin, and Exchange Operator, preventing access to restricted actions and screens for other roles.
- **FR-004**: The system MUST allow broker admins to create funds journal entries (credits and debits) for a trader and MUST update the trader’s buying power and ledger balances accordingly, even when this results in a negative balance, provided that such negative or limit-breaching conditions are clearly flagged in the UI and statements.
- **FR-005**: The system MUST allow an exchange operator to run an end-of-day process for a specific date that consolidates trades, positions, and journal entries into generated daily statements for traders and brokers, and this process MUST be idempotent for a given date (re-running recomputes results and overwrites statements without duplicating effects).
- **FR-006**: The system MUST provide traders and brokers with access to their generated daily statements via the UI and MUST display balances, P&L, and key cash movements that reconcile with positions and ledger history.
- **FR-007**: The system MUST display a clear, always-visible "SIMULATED / NOT REAL MONEY" banner or equivalent warning on primary Trader and Broker views.
- **FR-008**: The system MUST define predictable default landing pages by role: Trader → Market Watch, Broker Admin → Broker Dashboard, Exchange Operator → Exchange Overview, all reachable immediately after login without exposing generic JHipster home content.
- **FR-009**: The system MUST provide a primary welcome/landing page for the application that is NOT the generic JHipster welcome page, and instead prominently displays RNExchange branding (logo showing "RNX", application name "RNExchange") and a brief description of the simulator.
- **FR-010**: The RNExchange logo asset used in the application MUST support at least a 3:1 horizontal aspect ratio and be provided in a minimum resolution of 600×200 pixels (preferably vector or high-resolution PNG) so it renders crisply on common desktop and mobile displays.
- **FR-011**: The system MUST provide per-role, easily discoverable “How to use RNExchange” help sections accessible from each role’s main dashboard or header, written in plain language as a lightweight user manual for that role.
- **FR-012**: The system MUST ensure that menu items and UI elements are filtered by role, such that irrelevant generic JHipster sections (e.g., "Entities", "Administration", "Performance") are not visible to Trader and Broker Admin users and only truly relevant administrative items are shown to Exchange Operator users.
- **FR-013**: The system MUST ensure that each user role (Trader, Broker Admin, Exchange Operator) has all relevant functionality and navigation accessible from their main dashboard or primary navigation (e.g., Traders can access watchlists, order entry, positions, and ledger; Broker Admins can access trader management, funds journals, and statements; Exchange Operators can access EOD tools, statements, and system overview).
- **FR-014**: The system MUST address at least five high-friction UX "paper-cut" issues (e.g., unclear error messages, missing loading states, missing empty-state messages, or ambiguous button labels), tracked as `PC-001`–`PC-00N` in `specs/006-qa-hardening-demo/research.md`, such that core demo flows can be completed without confusion.
- **FR-015**: The system MUST provide automated end-to-end, contract, and integration tests for the three critical demo flows, as specified in **NFR-002 (Reliability & Testability)** and **SC-002**, and these tests MUST run as part of CI.
- **FR-016**: The system MUST include performance tests sufficient to validate **NFR-001 (Performance)** and **SC-003**, including order placement latency, EOD duration, and WebSocket market data throughput targets defined in the RNExchange constitution.
- **FR-017**: The system MUST provide fixed demo users (e.g., `trader_demo`, `broker_demo`, `exchange_demo`) with stable starting balances and positions so that demo flows are repeatable across environments without manual data manipulation.
- **FR-018**: All trader and broker statements and reports (including HTML or printable exports) MUST prominently display a clear simulation disclaimer such as "This is a simulated environment — not real trading or money" in line with the RNExchange constitution’s Educational Transparency rules.

### Non-Functional Requirements

- **NFR-001 (Performance)**: Under demo-scale load (approximately 1,000 concurrent traders with hundreds of quotes per second and 5–10 orders per second), order placement latency MUST be <250 ms p95, EOD settlement for roughly 10,000 positions MUST complete within 5 minutes, and WebSocket tick broadcast MUST sustain at least 10,000 updates per second without demo-breaking errors or timeouts, consistent with the RNExchange constitution.
- **NFR-002 (Reliability & Testability)**: The three critical end-to-end flows (trader trade/ledger, broker funds journal/ledger, exchange operator EOD/statements) MUST be covered by automated contract, integration, and end-to-end tests that run reliably in CI, with flakiness low enough that repeated pipeline runs pass without intermittent failures (target: ≥95% pass rate over any rolling window of 20 CI runs for critical suites).
- **NFR-003 (UX & Clarity)**: Error, loading, and empty states for core demo screens MUST avoid dead ends and confusing messaging so that a team member can complete the “day in the life” demo in under 15 minutes without manual data edits (see SC-001 and SC-004).

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
- **SC-002**: The three critical end-to-end flows (trader trade/ledger, broker funds journal/ledger, exchange operator EOD/statements) are covered by automated tests that pass reliably in the continuous integration pipeline (e.g., no intermittent failures across multiple runs), satisfying NFR-002.
- **SC-003**: Under a realistic simulated load approximating 1,000 concurrent traders with hundreds of quotes per second and 5–10 orders per second, users experience order placement and core UI interactions completing within a few hundred milliseconds in typical cases (target <250 ms p95), and without demo-breaking errors or timeouts, satisfying NFR-001.
- **SC-004**: During internal dry-run demos, participants report no blocking usability issues in the core flows, and the presence of "SIMULATED / NOT REAL MONEY" warnings and improved error/loading/empty states is sufficient for the product to be presented as a credible technology preview without further architecture changes, satisfying NFR-003.

### Assumptions & Dependencies

- The underlying authentication and authorization mechanisms already exist; this feature focuses on clarifying and enforcing role behaviors and navigation rather than redefining auth.
- Performance and load expectations are evaluated on a representative "target dev hardware" baseline agreed by the team, not on minimal or resource-constrained machines.
- Market data (quotes) and trade matching are already available in the simulator; this feature focuses on reliability, visibility of results in positions/ledgers/statements, and demo readiness rather than adding new instruments or markets.
- Demo user accounts and starting data can be seeded or reset through existing tooling or scripts outside the scope of the demo run itself.
