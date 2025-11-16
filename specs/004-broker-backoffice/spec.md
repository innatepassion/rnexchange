# Feature Specification: M3 Broker Back Office

**Feature Branch**: `004-broker-backoffice`  
**Created**: 2025-11-16  
**Status**: Draft  
**Input**: Broker Back Office M3 description focusing on broker-scoped visibility into traders, simple simulated fund movements via journals, and lightweight risk snapshots (text from `/speckit.specify` command).

## Clarifications

### Session 2025-11-16

- Q: What utilization formula should the Broker Dashboard use for ranking risk? → A: Utilization = exposure / max(equity, ε), where equity = cash + unrealized P&L; clamp result to 0–100%.
- Q: What exact ε value should be used in the utilization formula? → A: ε = 1.0 (currency units).
- Q: How are “active traders” defined for dashboard counts? → A: Active = trader account status is “active”.
- Q: Should journal debits be blocked if equity would go negative? → A: Allow negative equity (no floor) in M3.
- Q: How should we prevent duplicate journal debits/credits on retries? → A: Require idempotency key per journal request.
- Q: What price freshness is required for exposure calculation? → A: Require price ≤ 1 minute old.
- Q: How should stale (>60s) prices be handled in exposure? → A: Exclude stale prices from exposure and mark the snapshot as stale.

## User Scenarios & Testing _(mandatory)_

### User Story 1 - See broker-wide overview and most leveraged traders (Priority: P1)

A Broker Admin wants a single Broker Dashboard where they can see, at a glance, how their book of simulated business is doing (active traders, total balances, equity exposure) and which clients are currently the most “levered” based on a simple utilization measure, without needing to export data or run reports.

**Why this priority**: This gives immediate situational awareness and risk visibility for the broker, enabling them to monitor overall exposure and spot outlier clients before taking further action; it is the foundation for all other back-office decisions.

**Independent Test**: Can a Broker Admin, starting from a clean login with existing M2 data, open the Broker Dashboard and answer within one screen: “How many active traders do I have, what is my total simulated AUM/cash, and who are my top 10 highest-utilization clients?”

**Acceptance Scenarios**:

1. **Given** a Broker Admin with one or more active traders and open positions across multiple client accounts, **When** they open the Broker Dashboard, **Then** they see cards summarizing active trader count, total simulated assets under management (cash plus exposure), total combined cash balance, and a table listing at least the top 10 trading accounts ranked by utilization.
2. **Given** positions and balances for some trading accounts under a broker, **When** utilization is calculated for each account, **Then** each row in the utilization table includes trader identity, cash balance, notional exposure (based on current positions and last known prices), and a utilization value that reflects exposure relative to combined balance and exposure.
3. **Given** an environment where the Broker Admin has multiple independent brokers set up, **When** a Broker Admin logs in and opens the Broker Dashboard, **Then** all counts, totals, and utilization rows are restricted to trading accounts that belong to that Broker Admin’s broker and do not include data from any other broker.

---

### User Story 2 - List and inspect all traders under a broker (Priority: P1)

A Broker Admin wants a Clients screen that lists all trader accounts under their broker, showing key operational fields like trader name, login, status, cash balance, and current P&L so they can quickly inspect individual clients and drill into details when needed.

**Why this priority**: Before adjusting balances or taking risk-related decisions, the broker must be able to reliably see who their clients are and basic financial state per client; without this, the other flows are unusable.

**Independent Test**: Can a Broker Admin, starting from the Broker Back Office, navigate to a Clients view and see a complete, broker-scoped list of all traders with the specified columns, and verify that traders from other brokers never appear?

**Acceptance Scenarios**:

1. **Given** a Broker Admin with multiple traders under their broker, **When** they open the Clients view, **Then** they see a tabular list of traders that includes at least trader name, login identifier, account status (e.g., active, disabled), current cash balance, and a simple current P&L indicator derived from M2 data.
2. **Given** there are traders belonging to other brokers in the system, **When** a Broker Admin views the Clients screen, **Then** only traders whose trading accounts are associated with that broker are shown, and no cross-broker leakage occurs.
3. **Given** a Broker Admin selects a specific trader row, **When** they open the details drawer or modal, **Then** they can see core account details and a recent ledger snippet sufficient to understand recent balance-affecting events before deciding on any new journal entries.

---

### User Story 3 - Move simulated funds via a simple journal action (Priority: P2)

A Broker Admin wants a simple way to credit or debit a trader’s simulated cash balance using a journal entry form, such that changes are immediately reflected in the trader’s account, ledger, and downstream ability to place orders in the existing M2 trading flow, without going through a complex approval workflow.

**Why this priority**: Adjusting client balances is the primary operational action the broker needs in M3 to manage simulated funds; while it builds on visibility, it directly affects client experience and risk and therefore must be reliable and auditable.

**Independent Test**: Can a Broker Admin open a trader’s details, submit a single credit or debit via the journal form with a reason, and then see the updated cash balance and ledger entry, with the change affecting what that trader can do in the trading system?

**Acceptance Scenarios**:

1. **Given** a Broker Admin viewing a specific trader in the Clients screen, **When** they open the funds journal form and submit a credit with a positive amount and reason, **Then** the system records a new journal entry, increases the trader’s cash balance accordingly, shows a confirmation, and refreshes the trader’s visible balance and ledger snippet.
2. **Given** a Broker Admin viewing a specific trader, **When** they submit a debit with a positive amount and reason, **Then** the system records a new journal entry, decreases the trader’s cash balance accordingly (subject to allowed negative or minimum balance rules), shows a confirmation, and refreshes the visible balance and ledger snippet.
3. **Given** a Broker Admin attempts to submit a journal entry for a trading account that does not belong to their broker (e.g., via a manipulated identifier), **When** the journal request is processed, **Then** it is rejected and no balance or ledger changes are applied for that trading account.
4. **Given** a valid journal entry has been successfully applied to a trading account, **When** the corresponding trader continues to use the existing M2 trading functionality, **Then** order placement and risk checks use the updated balance so that available buying power reflects the journaled funds movement.

---

### Edge Cases

- Broker has no traders yet: the Broker Dashboard and Clients screen should clearly indicate “no data” while still loading correctly, with summary cards showing zero values and no utilization rows.
- Traders have cash balance but no open positions: total equity exposure and notional exposure should correctly show zero, and utilization for such accounts should be handled without errors (e.g., treated as 0% when there is no exposure).
- Traders have open positions but zero or near-zero cash balance: utilization calculations should still succeed and can approach 100% for heavily leveraged accounts, without causing divide-by-zero or rounding issues.
- Journal debits that cause negative equity: permitted in M3; balances may become negative without additional approval workflow.
- Multiple journal entries submitted in quick succession for the same account: balance updates and ledger entries should reflect all entries in the correct order, and the Broker Admin should see a consistent final balance after refresh.
- Journal submission retries/timeouts: duplicate client retries with the same idempotency key must not produce multiple ledger entries; server should return the original result.
- A journal is attempted with invalid input (e.g., negative amount, non-numeric amount, missing reason, or unsupported direction): the system should reject the request with a clear validation message and must not modify the trading account balance or ledger.
- A Broker Admin without access to a given broker’s accounts attempts to view or journal against those accounts (e.g., via direct URL or crafted request): the system should deny access and not leak information about accounts owned by other brokers.
- All instrument prices stale (>60s): exposure should compute as zero; utilization should reflect exposure=0, and the UI should display a “stale” indicator for affected account rows.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST provide broker-scoped access for Broker Admins to retrieve a list of all traders associated with their broker, including trader identity, login, account status, current cash balance, and current P&L derived from existing M2 data.
- **FR-002**: The system MUST provide a broker-level overview for a Broker Admin that summarizes, using current data, the total number of active traders under that broker (where “active” is defined as trader account status is “active”), total combined cash balance across their trading accounts, and a simple measure of total equity exposure based on open positions.
- **FR-003**: The system MUST compute a simple risk snapshot for each trading account under a broker, including notional exposure (based on position quantity and last known price no older than 1 minute) and a utilization metric defined as: utilization = exposure / max(equity, ε), where equity = cash + unrealized P&L and ε is a small positive constant to avoid divide-by-zero; the value MUST be clamped to 0–100%. The dashboard MUST present a ranked list ordered by highest utilization.
- Clarification (FR-003): Use ε = 1.0 (currency units) for the max(equity, ε) term.
- Clarification (FR-003): Positions with last price older than 60s MUST be excluded from exposure; per-account snapshot SHOULD include a stale indicator if any position price was excluded for staleness.
- **FR-004**: The system MUST restrict all Broker Back Office data access (trader lists, overview metrics, risk snapshots, journal operations) to the broker that is derived from the authenticated Broker Admin’s identity, without requiring the UI to specify a broker identifier.
- **FR-005**: The system MUST allow a Broker Admin to create a funds journal entry for a selected trading account under their broker by specifying an amount, a direction indicating credit or debit, and a free-text reason or note.
- **FR-006**: When a valid funds journal entry is submitted for a trading account that belongs to the Broker Admin’s broker, the system MUST create a corresponding ledger entry of the appropriate type (e.g., deposit-like for credits, withdrawal-like for debits) and MUST adjust the trading account’s cash balance by exactly the specified amount in the appropriate direction. In M3, debits MAY result in negative equity/balance (no equity floor enforcement).
- **FR-007**: After applying a funds journal entry, the system MUST return or display the updated cash balance for that trading account and ensure that any Broker Back Office screens that show the account’s balance and recent ledger entries reflect the change without requiring a full manual reload by the Broker Admin.
- **FR-008**: The system MUST ensure that changes to trading account balances caused by funds journal entries are respected by downstream trading and risk checks used in the existing M2 trading flow, so that a trader’s ability to place orders is consistent with the latest journaled balance.
- **FR-009**: The system MUST provide UI surfaces for Broker Admins, including a Broker Dashboard (with overview cards and a ranked utilization table) and a Clients screen (with a traders table and a journal-entry drawer or modal) that are understandable to non-technical users.
- **FR-010**: The system MUST record sufficient information in each journal/ledger entry (including at least timestamp, Broker Admin identity, trading account, direction, amount, reason, and idempotency key) to support basic auditing and deduplication of simulated fund movements initiated via the Broker Back Office.
- **FR-011**: The journal submission API MUST require a client-provided idempotency key and MUST ensure at-most-once application per unique key for a given broker account; repeated submissions with the same key MUST return the original result without creating additional ledger entries.

### Key Entities _(include if feature involves data)_

- **Broker Admin / BrokerDesk User**: Represents a user acting on behalf of a broker in the back-office portal; associated with exactly one broker context for the purposes of this feature, which determines which traders and accounts they can view and modify.
- **Broker**: Represents an institutional entity that owns one or more trader relationships and trading accounts; all visibility, journal entries, and risk calculations in this feature are scoped to a single broker at a time.
- **Trader Profile**: Represents an individual trader or client, including identifying information (e.g., name, login identifier) and status; linked to one or more trading accounts that are in turn associated with a broker.
- **Trading Account**: Represents a trading account that holds a simulated cash balance and open positions for a given trader under a broker; must track balance, status, and association to trader and broker.
- **Position / Holding**: Represents an open or recently held position in a financial instrument for a trading account, including at least quantity and the last known price needed to derive notional exposure.
- **Order**: Represents an order placed by a trader, used here primarily for counting currently open orders under a broker in the overview metrics.
- **Ledger / Journal Entry**: Represents a balance-affecting event on a trading account (such as a credit or debit initiated by a Broker Admin), including amount, direction, type, timestamp, reason, and linkage to the initiating user and trading account.
- **Risk Snapshot Row**: A derived, non-persisted view for a trading account showing notional exposure and utilization (per FR-003 formula), and optionally other simple risk indicators used to rank accounts for the Broker Dashboard.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: A Broker Admin can, starting from the Broker Back Office, identify the total number of active traders, total combined simulated cash balance, total equity exposure, and top 10 highest-utilization clients for their broker in under 2 minutes, without exporting data or leaving the portal.
- **SC-002**: In at least 95% of typical usage scenarios with up to a few hundred traders per broker, the Broker Dashboard overview metrics and utilization table load in a way that feels immediate to the Broker Admin (perceived within a couple of seconds) so they can reliably monitor their book.
- **SC-003**: In user testing or internal pilot use, at least 90% of Broker Admins can successfully locate a specific trader, open the funds journal form, submit a credit or debit with a reason, and observe the updated balance and ledger entry without assistance.
- **SC-004**: For a sample of journal entries executed during testing, 100% of entries result in consistent balances between the Broker Back Office views, the underlying ledger data, and the behavior of the M2 trading flow (e.g., order placement limits reflect the updated balances), with no cross-broker data leakage observed.

## Assumptions

- Broker Admins are already authenticated through an existing mechanism, and their broker association can be reliably determined from their authenticated identity without additional input from the UI.
- Existing M2 data sources already provide access to trader profiles, trading accounts, balances, positions (including last prices), orders, and ledger information, and this feature can rely on that data rather than introducing new pricing or risk engines.
- Balances adjusted through funds journal entries are allowed to be temporarily negative if the broader product permits it; if stricter rules are needed (e.g., minimum balance constraints, approvals, or automatic risk actions), these will be introduced in a later phase beyond M3.
