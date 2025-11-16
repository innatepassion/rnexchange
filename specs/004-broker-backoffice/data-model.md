# Data Model — M3 Broker Back Office

## Entities

### Broker

- Fields: `id`, `name`, `status`
- Relationships: 1:N `TraderProfile`, 1:N `TradingAccount`

### TraderProfile

- Fields: `id`, `name`, `login`, `status` (active/disabled), `broker_id`
- Relationships: N:1 `Broker`, 1:N `TradingAccount`
- Validation: `login` unique per broker

### TradingAccount

- Fields: `id`, `trader_profile_id`, `broker_id`, `cash_balance`, `status`
- Relationships: N:1 `TraderProfile`, N:1 `Broker`, 1:N `Position`, 1:N `LedgerEntry`
- Validation: `cash_balance` numeric; allow negative per M3

### Position

- Fields: `id`, `trading_account_id`, `instrument_id`, `quantity`, `last_price`, `last_price_timestamp`, `cost_basis`
- Relationships: N:1 `TradingAccount`
- Validation: `last_price_timestamp` must be within 60s for exposure; otherwise treated as stale

### LedgerEntry

- Fields: `id`, `trading_account_id`, `type` (`JOURNAL_CREDIT` | `JOURNAL_DEBIT` | existing types), `amount`, `reason`, `created_at`, `created_by_user_id`
- Relationships: N:1 `TradingAccount`
- Validation: `amount` > 0; reason non-empty; audit fields required

### IdempotencyToken (new)

- Fields: `id`, `broker_id`, `trading_account_id`, `token`, `ledger_entry_id`, `created_at`
- Constraints: unique `(broker_id, trading_account_id, token)`
- Purpose: Ensure at-most-once journal application

### RiskSnapshot (derived view)

- Not persisted
- Fields: `trading_account_id`, `cash`, `unrealized_pnl`, `exposure`, `equity`, `utilization_pct`, `stale_price_flag`
- Formulae:
  - `exposure = Σ |position.quantity × last_price|`
  - `equity = cash + unrealized_pnl`
  - `utilization = clamp( exposure / max(equity, ε=1.0), 0, 1 ) × 100`

## State Transitions

- Journal Credit:
  - Input: amount > 0, reason, idempotency token
  - Effect: create `LedgerEntry(JOURNAL_CREDIT)`, increase `cash_balance` by amount
- Journal Debit:
  - Input: amount > 0, reason, idempotency token
  - Effect: create `LedgerEntry(JOURNAL_DEBIT)`, decrease `cash_balance` by amount (can go negative)

## Validation Rules

- Idempotency: duplicate `(broker_id, account_id, token)` MUST return original result without new ledger entries
- RBAC: All reads/writes scoped to `broker_id` from authenticated `BROKER_ADMIN`
- Price Freshness: positions with `last_price_timestamp > 60s` treated as `stale_price_flag = true` and excluded from exposure
