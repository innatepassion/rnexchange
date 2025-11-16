# Data Model: Simple Trading & Portfolio (M2)

**Feature**: `003-simple-trading-portfolio`  
**Date**: 2025-11-15

This model reuses existing JHipster entities (e.g., `Trader`, `TradingAccount`, `Instrument`) and adds the minimal new entities needed to support the cash-only trading loop.

## Entities

### TradingAccount (existing, extended conceptually)

- **Key fields**:
  - `id`
  - `traderId` / link to Trader profile
  - `brokerId` / link to Broker
  - `accountType` (must include `CASH` for this feature)
  - `balance` (current cash balance)
- **Relationships**:
  - One `Trader` → many `TradingAccount`
  - One `TradingAccount` → many `Order`, `Position`, `LedgerEntry`
- **Rules**:
  - Only `CASH` accounts are in-scope for M2.
  - Balance must always equal initial funding plus net ledger movements.

### Instrument (existing, extended conceptually)

- **Key fields**:
  - `id`
  - `symbol` (e.g., `RELIANCE`, `INFY`)
  - `exchange` (e.g., `NSE`, `BSE`, `MCX`)
  - `status` (`active` / `inactive`)
  - `lotSize` (positive integer)
- **Rules**:
  - Only instruments with `status = active` are tradable.
  - Order quantity must be a positive multiple of `lotSize`.

### Order (new/extended)

- **Key fields**:
  - `id`
  - `tradingAccountId`
  - `instrumentId`
  - `side` (`BUY` / `SELL`)
  - `type` (`MARKET` / `LIMIT`)
  - `quantity`
  - `limitPrice` (nullable; required for `LIMIT`)
  - `state` (`NEW`, `ACCEPTED`, `FILLED`, `REJECTED`)
  - `rejectionReason` (nullable)
  - `createdAt`
  - `updatedAt`
- **Relationships**:
  - One `TradingAccount` → many `Order`
  - One `Order` → zero or one `Execution`
- **Rules**:
  - On creation, state is `NEW`; valid orders quickly transition to `ACCEPTED` then `FILLED` or `REJECTED`.
  - No partial fills; each order is either fully filled or rejected.

### Execution (new)

- **Key fields**:
  - `id`
  - `orderId`
  - `tradingAccountId`
  - `instrumentId`
  - `side` (`BUY` / `SELL`)
  - `quantity`
  - `price` (execution price)
  - `executedAt`
- **Relationships**:
  - One `Order` → one `Execution` (for filled orders)
  - One `Execution` → updates one `Position` and one `LedgerEntry`
- **Rules**:
  - Exists only for orders with state `FILLED`.

### Position (new/extended)

- **Key fields**:
  - `id`
  - `tradingAccountId`
  - `instrumentId`
  - `quantity` (net long quantity; shorts out of scope)
  - `averageCost`
- **Derived values** (for UI/queries):
  - `lastPrice` (from mock feed)
  - `mtm` = `(lastPrice − averageCost) × quantity`
- **Relationships**:
  - One `TradingAccount` + `Instrument` pair → at most one `Position`.
- **Rules**:
  - BUY execution: `newQty = oldQty + qty`, `newAvgCost = (oldQty * oldAvgCost + qty * execPrice) / newQty`.
  - SELL execution: reduces `quantity`; if `quantity` reaches zero, position can be kept with zero quantity or removed per implementation choice.

### LedgerEntry (new)

- **Key fields**:
  - `id`
  - `tradingAccountId`
  - `type` (`DEBIT` / `CREDIT`)
  - `amount`
  - `fee` (optional, may be included in `amount` description)
  - `description` (e.g., `BUY RELIANCE x100 @2500`)
  - `createdAt`
- **Relationships**:
  - One `TradingAccount` → many `LedgerEntry`
- **Rules**:
  - BUY: create `DEBIT` entry of `qty * execPrice + fee`.
  - SELL: create `CREDIT` entry of `qty * execPrice − fee`.
  - Ledger entries must reconcile with `TradingAccount.balance`.

## State Transitions (High-Level)

### Order Lifecycle

1. `NEW` → validation succeeds → `ACCEPTED`
2. `ACCEPTED` → matching succeeds → `FILLED` + `Execution` created
3. `NEW` or `ACCEPTED` → validation or matching fails → `REJECTED` (with `rejectionReason`)

### Position & Cash Updates on Execution

- **BUY**:
  - Update or create `Position` with new quantity and average cost.
  - Create `LedgerEntry` debit.
  - Decrease `TradingAccount.balance` by trade value + fee.
- **SELL**:
  - Decrease `Position.quantity` and compute realized P&L (for reporting).
  - Create `LedgerEntry` credit.
  - Increase `TradingAccount.balance` by trade value − fee.
