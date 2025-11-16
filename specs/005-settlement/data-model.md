# Data Model — M5 Settlement & Reporting

**Feature**: `005-settlement`  
**Date**: 2025-11-16

This feature reuses core RNExchange entities (`TradingAccount`, `Instrument`, `Position`, `LedgerEntry`, `DailySettlementPrice`, `SettlementBatch`) and adds a simple report-link concept to support per-account statements and broker summaries.

## Entities

### TradingAccount (existing, reused)

- Fields (relevant here):
  - `id`
  - `broker_id`
  - `trader_profile_id`
  - `cash_balance` / ledger-derived balance
- Relationships:
  - One `Broker` → many `TradingAccount`
  - One `TradingAccount` → many `Position`, many `LedgerEntry`
- Settlement Rules:
  - Closing balance for a trade date must equal opening balance plus all ledger movements for that date, including EOD MTM entries.

### Instrument (existing, reused)

- Fields (relevant here):
  - `id`
  - `symbol`
  - `exchange_id`
  - `status` (active/inactive)
- Relationships:
  - One `Exchange` → many `Instrument`
  - One `Instrument` → many `Position`, many `DailySettlementPrice`
- Settlement Rules:
  - Only instruments with `status = active` participate in EOD settlement for that date.

### DailySettlementPrice (existing, reused)

- Fields:
  - `id`
  - `refDate` (trade date)
  - `instrumentSymbol`
  - `settlePrice`
  - `instrument_id`
- Relationships:
  - Many `DailySettlementPrice` → one `Instrument`
- Settlement Rules:
  - For a given `refDate` and `instrument`, at most one official settlement price is used for MTM.
  - If no record exists, the system attempts to fall back to the last 1-minute bar close for that instrument on `refDate`; if still unavailable, the batch fails.

### Position (existing, reused with EOD semantics)

- Fields (relevant here):
  - `id`
  - `trading_account_id`
  - `instrument_id`
  - `qty`
  - `avgCost`
  - `lastPx` (used as end-of-day mark)
  - `unrealizedPnl`
  - `realizedPnl`
- Relationships:
  - Many `Position` → one `TradingAccount`
  - Many `Position` → one `Instrument`
- Settlement Rules:
  - EOD batch sets `lastPx` to the official settlement price for the instrument on `refDate`.
  - `unrealizedPnl` and `realizedPnl` represent cumulative P&L up to and including the trade date after settlement is applied.

### LedgerEntry (existing, extended conceptually)

- Fields (core):
  - `id`
  - `trading_account_id`
  - `type` (currently `DEBIT` / `CREDIT`; to be extended with EOD-specific types)
  - `amount`
  - `description`
  - `createdAt`
- Relationships:
  - Many `LedgerEntry` → one `TradingAccount`
- Settlement Rules:
  - For each trading account and EOD run, the batch posts a single net MTM entry:
    - Positive net P&L → `EOD_MTM_CREDIT`
    - Negative net P&L → `EOD_MTM_DEBIT`
  - EOD entries are identifiable by `type` and date so they can be superseded on batch re-run without double-counting.

### SettlementBatch (existing, reused)

- Fields:
  - `id`
  - `refDate`
  - `kind` (`EOD`, `VARIATION`, `EXPIRY` – this feature focuses on `EOD`)
  - `status` (`CREATED`, `PROCESSED`, `REVERSED`; may be extended with `FAILED` at implementation time)
  - `remarks` (free text/JSON summary)
  - `exchange_id`
- Relationships:
  - Many `SettlementBatch` → one `Exchange`
- Settlement Rules:
  - For each EOD run, one `SettlementBatch` row is created for the exchange.
  - Multiple batches for the same `refDate` and `kind = EOD` are allowed; the latest processed batch is treated as authoritative.
  - Summary statistics (e.g., number of accounts processed, net P&L totals) may be captured in `remarks`.

### ReportLink (new conceptual entity)

- Fields (conceptual, to be mapped via JDL/Liquibase if persisted):
  - `id`
  - `refDate`
  - `reportType` (`TRADER_STATEMENT`, `BROKER_SUMMARY`)
  - `tradingAccountId` (nullable; required for trader statements)
  - `brokerId` (nullable; required for broker summaries)
  - `settlementBatchId`
  - `relativeUrl` (path to HTML view, e.g., `/reports/statement?accountId=...&date=...`)
- Relationships:
  - Many `ReportLink` → one `SettlementBatch`
  - Many `ReportLink` → one `Broker` or `TradingAccount` (depending on type)
- Rules:
  - There is at most one active `ReportLink` per `(refDate, reportType, tradingAccountId)` and per `(refDate, reportType, brokerId)` for the authoritative batch.
  - UIs use `ReportLink` records to populate “Statements” and “Settlements/Reports” lists.

## State Transitions

### SettlementBatch Lifecycle (EOD)

1. `CREATED`: batch row inserted when EOD run is triggered; `status = CREATED`.
2. `PROCESSED`: after all positions, ledgers, and report links are successfully updated, `status` transitions to `PROCESSED`.
3. `FAILED` (conceptual): if a fatal error occurs (e.g., missing prices), the batch is marked as failed and no partial updates should remain active; implementation may introduce a dedicated status or represent failures via `REVERSED` semantics and `remarks`.

### Position & Ledger Updates on EOD

- For each open position:
  - Set `lastPx` to settlement price.
  - Recalculate `unrealizedPnl` using `(settlePrice − avgCost) × qty` and keep `realizedPnl` consistent with prior executions.
- For each trading account:
  - Aggregate per-position MTM for that account.
  - Create a single `LedgerEntry` with `type = EOD_MTM_CREDIT` or `EOD_MTM_DEBIT`, `amount = |netPnl|`, and description including `refDate`.

## Validation Rules

- EOD batch must not complete successfully if any active instrument with open positions lacks a usable internal price for the trade date.
- For a given `(refDate, account)`, at most one active EOD MTM ledger entry set is considered in balances; previous batch runs’ EOD entries are superseded or reversed.
- RBAC:
  - Only `EXCHANGE_OPERATOR` can create settlement batches and view system-wide batch lists.
  - `BROKER_ADMIN` can only see statements and summaries for their broker’s accounts.
  - `TRADER` can only see statements for their own trading accounts.
