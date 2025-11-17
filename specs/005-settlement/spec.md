# Feature Specification: M5 Settlement & Reporting

**Feature Branch**: `005-settlement`  
**Created**: 2025-11-16  
**Status**: Draft  
**Input**: User description: "For M5 – Settlement & Reporting (no Kite, still fully simulated), the goal is to make the simulator feel like a real broker back office at the end of the day, without adding any new external integrations or complex infra: using only the existing mock market data and entities, implement a simple but reliable EOD batch that the Exchange Operator can trigger from one place and that Brokers and Traders can see the results of. In the backend, add a SettlementService with a single main method like runEod(LocalDate tradeDate) that (1) reads a DailySettlementPrice for each active Instrument (if none exists, default to the last 1-minute bar close from the mock feed for that date), (2) for each open Position calculates MTM = qty \* (settlePrice − avgCost) and creates one or more LedgerEntry records per TradingAccount to reflect variation P&L (e.g., type EOD_MTM_CREDIT or EOD_MTM_DEBIT), (3) updates a snapshot field on Position to store the official EOD mark and cumulative realized/unrealized P&L, and (4) creates a SettlementBatch row with refDate, kind=EOD, status=CREATED/PROCESSED/FAILED, and a simple summary JSON or string remark. Expose this via a REST endpoint that only Exchange Operators can call, e.g. POST /api/settlements/eod?date=YYYY-MM-DD, and a read endpoint GET /api/settlements?from=…&to=… that returns batches with status, so the Exchange console can show a list and a “Run EOD” button for “today”. For reporting, generate plain HTML statements per TradingAccount for a given date (no fancy PDF engine yet): a small template that shows opening balance, cash flows (deposits/withdrawals), executed trades, fees, EOD MTM P&L and closing balance; store each statement’s URL or path on the SettlementBatch or in a simple ReportLink table so that a Trader can go to a “Statements” screen and see a list of their daily statements and click to open the HTML in a new tab, and a Broker Admin can see a Broker Settlement Summary CSV/HTML for their own traders (sum of all client P&L and balances). On the UI side, add for Exchange Operator a “Settlement” tab that shows all batches (date, status, total accounts processed) and a “Run EOD for Today” button that calls the REST endpoint and shows a simple progress/status; for Broker Admin, add a “Settlements/Reports” tab with a table of days and links to their broker-level summary and per-client statement lookup; and for the Trader, add a light “Statements” screen with a date list and “View” link. Keep it all single-pass and deterministic (no partial retries, no corporate actions, no intraday margin calls here): if something fails, mark the batch FAILED and log the error; you can add a simple “Re-run batch for this date” button in the Exchange view that just calls runEod again for that date after you fix the data. The acceptance test for this phase is: after placing a few trades in M2/M3, the Exchange Operator runs EOD for that date, and both Broker Admin and Trader can see consistent closing balances and P&L in their ledgers and statements, all computed from internal mock prices, with no dependency on Kite or any other external feed."

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Exchange Operator runs EOD settlement (Priority: P1)

An Exchange Operator wants to run an end-of-day (EOD) settlement for a specific trade date so that all open positions are marked to market using internal mock prices, account ledgers are updated with variation P&L, and daily statements and broker summaries are generated in one deterministic batch.

**Why this priority**: Without a reliable, single-trigger EOD process, the simulator cannot mimic a real broker back office, and downstream users (brokers and traders) would not see consistent balances and P&L for the day.

**Independent Test**: Can be fully tested by placing a set of trades across multiple traders and brokers for a single day, triggering EOD once from the Exchange Operator console, and verifying that batch status, positions, ledgers, and generated reports all reflect the same prices and P&L for that date.

**Acceptance Scenarios**:

1. **Given** trades and open positions exist for multiple instruments and trading accounts on a trade date, **When** the Exchange Operator runs EOD for that date, **Then** the system creates exactly one settlement batch for that date, computes settlement prices for each active instrument from internal data, updates each open position’s official EOD mark and P&L snapshot, posts variation P&L to each account’s ledger, generates account statements and broker summaries, and marks the batch as successfully processed.
2. **Given** a configuration or data issue (for example, a required settlement price cannot be determined from internal data), **When** the Exchange Operator runs EOD for that date, **Then** the system stops processing further updates for that batch, marks the batch status as failed, records a human-readable error summary, and leaves prior ledgers and statements for earlier dates unchanged.
3. **Given** a previously failed batch exists for a trade date, **When** the Exchange Operator re-runs EOD for the same date after fixing the data, **Then** the system creates a new successful batch (or re-processes the date in a way that does not double-count P&L), updates positions, ledgers, and reports so that the final state for that date is internally consistent and deterministic.

---

### User Story 2 - Trader views daily statements (Priority: P2)

A Trader wants to see a simple list of their daily statements and open each statement to understand how their opening balance, trades, fees, and end-of-day P&L led to the closing balance for a given date.

**Why this priority**: Traders need transparency into how their balances and P&L are derived so they can trust the simulator and reconcile activity, especially when testing trading strategies.

**Independent Test**: Can be fully tested by running EOD for a day with trades for a single trader, then logging in as that trader and verifying that the correct dates appear in the statements list and that each HTML statement accurately summarizes balances, cash flows, trades, fees, and EOD P&L.

**Acceptance Scenarios**:

1. **Given** EOD has been successfully run for a date on which the trader has activity or an open balance, **When** the Trader navigates to the “Statements” screen, **Then** they see an entry for that date and can click a link to open a human-readable HTML statement in a new tab.
2. **Given** the Trader opens a daily statement for a date with trades, **When** they review the statement, **Then** they can see opening balance, itemized cash flows (deposits/withdrawals), executed trades, applicable fees, EOD MTM P&L, and a closing balance that reconciles with their ledger for that date.

---

### User Story 3 - Broker Admin reviews settlement and client reports (Priority: P3)

A Broker Admin wants to review a daily overview of settlement results for their firm, including aggregated client P&L and balances and links to individual client statements, so they can verify that client accounts reconcile and that the broker-level totals make sense.

**Why this priority**: Broker Admins need a consolidated view of their clients’ settlement outcomes, not just per-account detail, to understand overall exposure and to support operational workflows.

**Independent Test**: Can be fully tested by creating multiple traders under a single broker, running EOD for a date with trades, then logging in as the Broker Admin and verifying that they can see a list of settlement days, a broker-level summary report per day, and links or navigation to client statements that reconcile with the broker totals.

**Acceptance Scenarios**:

1. **Given** multiple client trading accounts exist under a broker and EOD has been run for a date with activity, **When** the Broker Admin opens their “Settlements/Reports” view, **Then** they see a list of settlement dates with status and can open a broker-level summary (CSV or HTML) that shows aggregate balances and P&L across their clients for that date.
2. **Given** the Broker Admin is viewing a broker-level summary for a date, **When** they drill down into an individual client’s statement for that same date, **Then** the client’s closing balance and P&L in the statement reconcile to the totals shown in the broker summary for that client.

---

### Edge Cases

- What happens when EOD is run for a date with no trades but with existing open positions from prior days (for example, carry-forward positions)? The system should still compute settlement prices, MTM P&L, and statements so that balances roll forward correctly.
- How does the system handle a trade date for which no mock price data or settlement price can be determined for one or more active instruments? The system should fail the batch with a clear error summary rather than producing incomplete or inconsistent results.
- What happens if EOD is run for a future date or for a date prior to the earliest available mock data? The system should prevent running the batch or immediately fail with a descriptive error.
- How does the system behave if EOD is triggered multiple times for the same date (for example, after fixing a data issue)? The system must avoid double-counting P&L and should ensure that the final state (positions, ledgers, and reports) for that date is deterministic and internally consistent.
- What happens when an unauthorized user (for example, a Trader or Broker Admin) attempts to invoke the EOD run action directly? The system should reject the request and not create or modify any settlement batches.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST allow an Exchange Operator to trigger an end-of-day settlement run for a selected trade date from a single control in the Exchange-facing interface.
- **FR-002**: For each EOD run, the system MUST determine a single official settlement price for every active instrument on that trade date, using an internally stored daily settlement price where available and otherwise deriving the price from internal mock market data for that date.
- **FR-003**: For each open position associated with the trade date, the system MUST calculate mark-to-market profit or loss using the position quantity, the chosen settlement price, and the position’s average cost.
- **FR-004**: For each trading account with positions or relevant activity on the trade date, the system MUST post one or more ledger entries that reflect the net variation P&L from the EOD MTM calculation, clearly distinguishable from other ledger movements (for example, labeled as EOD MTM credits or debits).
- **FR-005**: The system MUST update each open position’s end-of-day snapshot for the trade date, including at least the official settlement price used, cumulative realized P&L to date, and cumulative unrealized P&L as of that date.
- **FR-006**: For every EOD run, the system MUST create a settlement batch record that includes the reference date, a type indicating end-of-day processing, a status that can move through at least “created”, “processed/success”, and “failed”, and a concise human-readable summary or JSON remark of the results.
- **FR-007**: The settlement process for a given trade date MUST be single-pass and deterministic: either the batch completes and all intended updates (positions, ledgers, statements, and summaries) are applied consistently for that date, or the batch is marked as failed and no partial or contradictory updates remain.
- **FR-008**: The system MUST expose a view for Exchange Operators that lists settlement batches over a configurable date range, showing at minimum the trade date, batch type, status, and a count of accounts or positions processed, and offering a way to trigger EOD for “today” and to re-run a batch for a specific date when needed.
- **FR-009**: The system MUST generate a daily statement in a simple human-readable format (such as HTML) for each trading account included in a successfully processed EOD batch, containing opening balance, cash flows (deposits and withdrawals), executed trades, applied fees, EOD MTM P&L, and closing balance.
- **FR-010**: The system MUST store a stable reference (such as a URL or path) for each generated daily statement so that users can later access the exact version that was produced as part of that EOD run.
- **FR-011**: Traders MUST be able to view a list of their available daily statements (by date) and open any statement in a new browser tab or equivalent, without seeing statements for accounts they do not own.
- **FR-012**: Broker Admins MUST be able to view a “Settlements/Reports” area that lists settlement dates relevant to their broker, provides access to a broker-level summary report (CSV or HTML) per date that aggregates balances and P&L across their clients, and enables navigation to individual client statements for that date.
- **FR-013**: The system MUST enforce role-based access so that only Exchange Operators can initiate or re-run EOD settlement batches, Broker Admins can see broker-level summaries only for their own clients, and Traders can only see statements for their own trading accounts.
- **FR-014**: In the event of an internal error during an EOD run, the system MUST record sufficient diagnostic information (for example, in logs or batch remarks) to allow operators to understand what failed and to correct data before re-running, without exposing sensitive technical details in end-user interfaces.
- **FR-015**: After an EOD run (including a re-run) completes successfully for a given trade date, the system MUST ensure that the closing balances and P&L shown in trader statements, broker summaries, and account ledgers all reconcile within expected rounding rules for that date.

### Key Entities _(include if feature involves data)_

- **Trading Account**: Represents an individual client account that holds positions and a cash balance; associated with a broker, has a ledger of all balance-impacting events, and is the subject of daily statements.
- **Instrument**: Represents a tradable symbol in the simulator; participates in positions and has an associated daily settlement price used for EOD marking.
- **Daily Settlement Price**: Represents the official price chosen for a specific instrument and trade date; can be entered explicitly or derived from internal mock price data and is used as the basis for all EOD MTM calculations.
- **Position**: Represents the quantity and average cost for a specific instrument in a trading account, along with an EOD snapshot that captures the official mark and cumulative realized and unrealized P&L as of each processed trade date.
- **Ledger Entry**: Represents an atomic change in a trading account’s balance, including deposits, withdrawals, trade-related cash movements, fees, and EOD MTM credits or debits that arise from the settlement process.
- **Settlement Batch**: Represents a single EOD processing run for a specific trade date and kind (for example, EOD), tracking status, basic metrics (such as counts or totals), and a summary remark, and linking to the generated statements and reports created during that run.
- **Daily Statement**: Represents a human-readable per-account report for a given trade date, summarizing opening balance, cash flows, trades, fees, EOD MTM P&L, and closing balance, and accessible via a stored URL or path.
- **Broker Settlement Summary**: Represents a broker-level report for a given trade date that aggregates balances and P&L across all client trading accounts for that broker and provides an overview suitable for operational review.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: In a standard test scenario with multiple traders and brokers placing trades during a simulated day, an Exchange Operator can complete an EOD run for that date from a single screen and see the batch recorded as successfully processed, with no inconsistent or partially updated data observed in the UI or underlying records.
- **SC-002**: For at least 95% of EOD runs under expected simulator usage, the Exchange Operator can see the final batch status and access to generated statements and reports within a short, predictable time window (for example, within tens of seconds) after triggering the run.
- **SC-003**: For 100% of trading accounts included in a successfully processed EOD batch during acceptance testing, the daily statement’s opening balance, itemized cash flows, trades, fees, EOD MTM P&L, and closing balance reconcile with the account ledger entries for that same date within agreed rounding rules.
- **SC-004**: For 100% of test scenarios where a Broker Admin has multiple active clients, the broker-level settlement summary for a processed EOD date accurately aggregates client balances and P&L, and these totals reconcile to the sum of the underlying client statements for that date.
