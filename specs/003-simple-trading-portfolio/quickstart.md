# Quickstart: Simple Trading & Portfolio (M2)

**Feature**: `003-simple-trading-portfolio`  
**Audience**: Developers and testers  
**Goal**: Exercise the cash-only trading loop end-to-end with minimal setup.

## 1. Pre-requisites

- RNExchange repository checked out on branch `003-simple-trading-portfolio`.
- Backend and frontend build steps already working per main README.
- Mock market data (M1) running so instruments like `RELIANCE` / `INFY` have prices.

## 2. Backend: Run application

From repo root:

```bash
./mvnw
```

Confirm the app starts and WebSocket endpoints are available.

## 3. Seed a Trader with CASH account

Use existing seed or admin UI to ensure:

- A `TRADER` user exists and can log in.
- That Trader has a `CASH` `TradingAccount` with sufficient `balance`.
- At least one active `Instrument` (e.g., `RELIANCE`) is available with a `lotSize`.

## 4. Place a BUY order (API)

Call the order endpoint (example using curl; adjust IDs as needed):

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TRADER_JWT>" \
  -d '{
    "tradingAccountId": "<account-id>",
    "instrumentId": "<instrument-id>",
    "side": "BUY",
    "type": "MARKET",
    "quantity": 100
  }'
```

Verify the response shows the order as `FILLED` (or equivalent) and includes an execution price.

## 5. Check portfolio & cash (API)

- Positions:

```bash
curl -H "Authorization: Bearer <TRADER_JWT>" \
  http://localhost:8080/api/trading-accounts/<account-id>/positions
```

- Ledger entries:

```bash
curl -H "Authorization: Bearer <TRADER_JWT>" \
  http://localhost:8080/api/trading-accounts/<account-id>/ledger-entries
```

Confirm:

- A position exists for the chosen instrument with correct quantity and average cost.
- A debit ledger entry exists for trade value + fee and cash balance decreased appropriately.

## 6. Exercise SELL flow

Repeat the order call with `"side": "SELL"` and a suitable quantity. Then re-check:

- Position quantity reduced or gone.
- Credit ledger entry created.
- Cash balance increased appropriately.

## 7. Frontend sanity check

Using the Trader UI:

- Open Market Watch and use the Order Ticket drawer to submit a BUY and SELL.
- Observe success/failure toasts.
- Open:
  - **Orders & Trades** to see last orders and their status.
  - **Portfolio & Cash** to see positions, MTM, and recent ledger entries.

Verify that when an order is placed and filled, WebSocket notifications cause these views to update after a refetch, without needing a full page reload.
