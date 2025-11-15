# Feature Specification: Simple Trading & Portfolio (M2)

**Feature Branch**: `003-simple-trading-portfolio`  
**Created**: 2025-11-15  
**Status**: Draft  
**Input**: Feature derived from /speckit.specify description for “M2 – Simple Trading & Portfolio”

## User Scenarios & Testing _(mandatory)_

### User Story 1 – Place a cash buy order and see updated portfolio (Priority: P1)

A Trader wants to place a simple cash-funded BUY order in a supported instrument (e.g., RELIANCE or INFY), have it immediately filled at the current market conditions, and then see their holdings and cash balance updated in a single, easy-to-understand flow.

**Why this priority**: This proves the end-to-end core trading loop (Order → Fill → Position → Cash movement) for cash accounts, which is the main business objective of this milestone.

**Independent Test**: Using a test Trader with sufficient cash balance and no existing position, place a single BUY order for a supported symbol and verify that the order, execution, position, and cash ledger all update correctly without any other features enabled.

**Acceptance Scenarios**:

1. **Given** a Trader with a CASH trading account, sufficient available balance, and an active instrument with a current price, **When** the Trader submits a valid BUY order (Market or Limit that can execute immediately), **Then** the system records the order, immediately fills it, creates a position with the correct quantity and average cost, and reduces the account cash balance by trade value plus a flat fee.
2. **Given** a Trader who has just completed a BUY, **When** they open the “Orders & Trades” and “Portfolio & Cash” views, **Then** they see the new order and execution in the recent activity list, the new position with quantity and average cost, and a corresponding cash ledger entry showing the debit.
3. **Given** a Trader holding a position created via this flow, **When** mock market prices move, **Then** the position’s mark-to-market (MTM) value on the “Portfolio & Cash” view changes in line with the latest prices without requiring a page reload.

---

### User Story 2 – Sell holdings and realize P&L (Priority: P2)

A Trader who already holds a position wants to SELL part or all of that position, see the realized profit or loss, and confirm that cash is credited back to their CASH account with fees reflected.

**Why this priority**: Selling is the natural complement to buying; together they demonstrate that the portfolio and cash ledger stay internally consistent through round-trip trades.

**Independent Test**: Seed a Trader with a long position and a known average cost, then execute a SELL order and verify resulting order status, execution price, realized P&L, remaining quantity, and cash movement.

**Acceptance Scenarios**:

1. **Given** a Trader with an existing long position in an active instrument and sufficient quantity, **When** they submit a SELL order (Market or a Limit that can execute immediately), **Then** the system records and fills the order, reduces the position quantity, calculates realized P&L based on average cost, and credits the trading account cash balance with trade value minus a flat fee.
2. **Given** the SELL has executed, **When** the Trader checks “Orders & Trades”, **Then** they see the order with a final status and execution price, and **When** they check “Portfolio & Cash”, **Then** they see the updated position (possibly zero quantity) plus a ledger entry showing the cash credit and, where applicable, realized P&L.

---

### User Story 3 – Broker Admin views trading activity and balances (Priority: P3)

A Broker Admin wants to review orders, positions, and cash balances for Traders under their broker, so they can monitor risk, activity, and overall exposure at a glance.

**Why this priority**: While not required for the Trader’s immediate trading experience, this provides essential oversight and evidences that back-office views can be built on the same data.

**Independent Test**: With several test Traders placing BUY and SELL orders under a single broker, log in as a Broker Admin and confirm that orders, positions, and balances can be viewed and filtered by broker.

**Acceptance Scenarios**:

1. **Given** multiple Traders with CASH accounts under the same broker have placed trades, **When** a Broker Admin views back-office lists filtered by that broker, **Then** they see a list of recent orders with key status fields (e.g., Accepted, Filled, Rejected) and execution prices.
2. **Given** positions and cash balances have changed due to trading activity, **When** the Broker Admin inspects portfolio and cash views for that broker’s Traders, **Then** they see position quantities, indicative valuations, and current cash balances that reconcile to the executed trades and ledger entries.

---

### Edge Cases

- What happens when a Trader submits a BUY order but the CASH account balance is not enough to cover trade value plus the flat fee?  
  → The order is rejected with a clear insufficient-funds message, and no position or cash changes occur.
- How does the system handle an order where the quantity is zero, negative, or not a multiple of the instrument’s lot size?  
  → The order is rejected with a clear validation error before any booking.
- What happens when the instrument is inactive or missing from the price feed?  
  → The order is rejected with an instrument/price availability error, and Traders are not allowed to trade that instrument.
- How does the system behave for Limit orders where the current price does not satisfy the limit condition at the time of submission?  
  → The order is immediately rejected as not marketable under current prices, and no positions or cash movements are created.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001 (Supported accounts and instruments)**: The system MUST support CASH trading accounts in which Traders hold a cash balance and may trade a predefined set of exchange instruments (e.g., selected NSE/BSE equities and basic MCX futures) that have attributes such as exchange, symbol, status, and lot size.
- **FR-002 (Order capture)**: Traders MUST be able to submit single-leg orders specifying at minimum: trading account, instrument, side (BUY/SELL), order type (Market or Limit), quantity, and for Limit orders a limit price.
- **FR-003 (Instrument and quantity validation)**: For every submitted order, the system MUST validate that the chosen instrument is active for trading and that the requested quantity is strictly positive and an exact multiple of the instrument’s lot size; invalid inputs MUST cause the order to be rejected with a human-readable reason.
- **FR-004 (Cash sufficiency validation for BUY)**: For BUY orders, the system MUST validate that the Trader’s CASH account balance is at least equal to the expected trade value (quantity × applicable price) plus a fixed, simple fee; if funds are insufficient, the order MUST be rejected with a clear explanation.
- **FR-005 (Simple order lifecycle and matching)**: For valid orders, the system MUST use a simple lifecycle with no queuing or partial fills: a new order is created, accepted, and either fully filled immediately based on the latest available market price or rejected if it cannot be filled under the current price conditions.
- **FR-006 (Pricing rules)**: Market orders MUST be evaluated using the latest available price from the mock market data; Limit orders MUST only be filled if the current price satisfies the limit condition (BUY at or below the limit, SELL at or above the limit); otherwise, they MUST be rejected with a clear message.
- **FR-007 (Execution recording)**: For every filled order, the system MUST record a single execution record capturing at least: trading account, instrument, side, quantity, execution price, and execution time so that trades can be audited and reported.
- **FR-008 (Position maintenance and average cost)**: The system MUST maintain positions per trading account and instrument, updating them on each execution using a straightforward average cost method:
  - For BUY executions: new quantity = previous quantity + executed quantity; new average cost = \((\text{previous quantity} × \text{previous average cost} + \text{executed quantity} × \text{execution price}) / \text{new quantity}\).
  - For SELL executions on long positions: quantity is reduced, and any realized profit or loss is computed based on the difference between execution price and average cost multiplied by the executed quantity.
- **FR-009 (Cash ledger and balance updates)**: For each filled order, the system MUST create a ledger entry describing the cash movement (for BUY, a debit for trade value plus fee; for SELL, a credit for trade value minus fee) and MUST update the trading account cash balance accordingly so that the ledger and balance remain in sync.
- **FR-010 (Trader views – Orders & Trades)**: Traders MUST be able to see a recent list of their orders and related executions (for example, the last 20 items) with key fields such as instrument, side, quantity, order type, limit price where applicable, status (e.g., New, Accepted, Filled, Rejected), and execution price when filled.
- **FR-011 (Trader views – Portfolio & Cash)**: Traders MUST be able to view a portfolio summary for each trading account including, per instrument: quantity, average cost, latest indicative price, and current MTM value, along with a recent list of cash ledger entries (credits and debits) showing dates, amounts, and short descriptions.
- **FR-012 (Real-time visibility of activity)**: When a new order is created or an execution occurs for a trading account, the Trader interface MUST be notified so that the “Orders & Trades” and “Portfolio & Cash” views reflect the change within **2 seconds** at least **95%** of the time, without requiring a full page reload, consistent with SC-004.
- **FR-013 (Broker Admin oversight)**: Broker Admins MUST be able to view orders, positions, and cash balances for the Traders associated with their broker, using at least a broker-level filter to narrow the lists.
- **FR-014 (Scope boundaries)**: The system MUST restrict this feature to long-only, fully-funded cash trades with no margin, intraday-only products, short-selling, complex fees, or risk-management rules beyond the simple validations described above.

### Key Entities _(include if feature involves data)_

- **Trader**: The end user placing orders; associated with one or more trading accounts and a broker.
- **Trading Account (CASH)**: Represents a Trader’s cash trading account, holding a cash balance, being linked to a broker, and used to place trades and hold positions.
- **Instrument**: A tradable security (e.g., equity or futures contract) with attributes such as symbol, exchange, trading status (active/inactive), and lot size.
- **Order**: A request from a Trader to buy or sell a specific instrument in a given quantity and order type; has a simple lifecycle from creation through acceptance to either filled or rejected.
- **Execution / Trade**: A record of a completed trade resulting from an order, capturing the final executed quantity and price at a specific time.
- **Position**: The net holdings of a given instrument in a specific trading account, including current quantity, average cost, and derived MTM value.
- **Ledger Entry**: A dated record of cash movement into or out of a trading account (debits and credits) with enough detail to reconcile to orders, executions, and current balance.
- **Broker Admin**: A back-office user associated with a broker who can view trading activity, positions, and balances for Traders under that broker.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001 (End-to-end trading loop)**: In user acceptance testing, at least 95% of test Traders are able to place a cash-funded BUY order in a supported instrument and see the resulting order, execution, position, and cash debit reflected correctly within 5 seconds of submission. _Primary coverage: User Story 1 acceptance scenarios._
- **SC-002 (Portfolio and cash correctness)**: Across a test set of at least 100 trades (mix of BUY and SELL on supported instruments), calculated positions, realized P&L, and cash ledger balances reconcile exactly with expected accounting outcomes (no unexplained differences) when independently recomputed. _Primary coverage: User Stories 1 and 2 acceptance scenarios._
- **SC-003 (Validation and error clarity)**: For negative test scenarios (e.g., insufficient funds, inactive instrument, invalid quantity, non-marketable limit orders), 100% of orders are automatically rejected with clear, human-readable reasons, and test users can explain why their order failed and what to change in at least 90% of observed cases. _Covers edge cases listed in “Edge Cases” and User Stories 1 and 2._
- **SC-004 (Real-time feedback)**: Under normal load in a test environment, new orders and executions initiated by a Trader are reflected in that Trader’s “Orders & Trades” and “Portfolio & Cash” views within 2 seconds at least 95% of the time, without requiring a manual page reload. _Directly refines FR-012 for measurable latency._
- **SC-005 (Broker oversight)**: In test scenarios with multiple Traders under the same broker, Broker Admins can retrieve and review consolidated lists of orders, positions, and balances filtered by broker, and confirm that data matches what Traders see in at least 95% of sampled cases. _Primary coverage: User Story 3 acceptance scenarios._
