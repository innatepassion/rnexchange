# Quickstart: Simple Trading & Portfolio (M2)

**Feature**: `003-simple-trading-portfolio`  
**Audience**: Developers, testers, and traders learning the platform  
**Goal**: Exercise the cash-only trading loop end-to-end with minimal setup and understand key trading concepts.

**Duration**: ~15–20 minutes | **Prerequisites**: Java 17+, Node.js, PostgreSQL running

---

## 1. Pre-requisites

✅ **Setup Checks**:

- RNExchange repository checked out on branch `003-simple-trading-portfolio`.
- Backend and frontend build steps already working per main README.
- Mock market data (M1) running so instruments like `RELIANCE` / `INFY` have prices.
- PostgreSQL database initialized with seed data (TradingAccounts, Instruments, etc.).

**Learning Objective**: This guide teaches you how cash trading works end-to-end:

- How to place BUY and SELL orders
- How positions track your holdings and average cost
- How cash balances and ledger entries record all transactions
- How WebSocket notifications keep your UI in sync in real-time

---

## 2. Backend: Run application

**🚀 Start the Backend**

From repo root:

```bash
./mvnw spring-boot:run
```

**Expected Output**:

- Spring Boot starts on port 8080
- Logs show "Started RNExchangeApplication"
- WebSocket STOMP endpoints are ready

**Learning Objective**: The backend is a Java Spring Boot application. It:

- Manages your trading account and cash balance
- Validates orders against business rules (funds, instrument status, lot sizes)
- Executes orders immediately using mock prices
- Broadcasts changes to the frontend via WebSocket in real-time

**⏱️ Timing Note (SC-004)**: From this point, subsequent requests should see UI updates within ~2 seconds thanks to WebSocket.

---

## 3. Seed a Trader with CASH account

**📋 Verify Your Test Account**

Use existing seed or admin UI to ensure:

- A `TRADER` user exists and can log in (e.g., `trader1` / password provided in docs).
- That Trader has a `CASH` `TradingAccount` with sufficient `balance` (e.g., ₹100,000 for testing).
- At least one active `Instrument` (e.g., `RELIANCE`) is available with a `lotSize` (e.g., 1 or 100 units).

**Learning Objective**: Understanding Account Types:

- **CASH Account**: You can only trade with funds you have on hand. No margin or short selling (FR-014 scope).
- **Lot Size**: Most exchanges standardize contracts in multiples (e.g., 1 or 100). You must order in these multiples.
- **Balance**: Your available cash. Each trade (BUY debit, SELL credit) updates this immediately.

---

## 4. Place a BUY order (API)

**📊 Learning Objective**: Understand how orders flow through the system and get filled.

**Order Types**:

- **MARKET orders**: Execute immediately at the best available price in the market.
- **LIMIT orders**: Execute only if the market price reaches your specified limit. They're protective but may not fill if prices move against you.

**Place Your First BUY Order**

Call the order endpoint (example using curl; adjust IDs as needed):

```bash
# Step 1: Get a JWT token (or use existing session)
# See main README for JWT auth flow

# Step 2: Place a MARKET BUY order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TRADER_JWT>" \
  -d '{
    "tradingAccountId": "<account-id>",
    "instrumentId": "<instrument-id>",
    "side": "BUY",
    "type": "MARKET",
    "qty": 100
  }'
```

**✅ Expected Behavior**:

- Response shows status = `FILLED` (or `PENDING` if price delayed)
- Includes `executionPrice` (the price you actually paid)
- Quantity confirmed: 100 units

**Learning Objective**:

- **Immediate Fill**: In this mock environment, MARKET orders fill immediately. In real trading, this depends on market depth.
- **Order Cost**: Your order costs `quantity × executionPrice + ₹25 fee`. If insufficient funds, the order is `REJECTED`.

---

## 5. Check portfolio & cash (API)

**📈 Learning Objective**: After a BUY order, you own shares. Understand how positions and cash work.

**Your Positions**

```bash
curl -H "Authorization: Bearer <TRADER_JWT>" \
  http://localhost:8080/api/trading-accounts/<account-id>/positions
```

**✅ Expected Output** (example):

```json
{
  "id": 1,
  "instrumentSymbol": "RELIANCE",
  "qty": 100,
  "avgCost": 2500.5,
  "lastPx": 2505.0,
  "unrealizedPnl": 450.0,
  "createdAt": "2025-11-15T10:00:00Z"
}
```

**Key Concepts**:

- **Qty**: Units you own (100 shares of RELIANCE).
- **Avg Cost**: Average price you paid per unit. If you buy 100 at ₹2500 and 50 more at ₹2600, avgCost recalculates.
- **Last Price (Mark-to-Market)**: The current market price. Used to calculate **Unrealized P&L**.
- **Unrealized P&L**: Your current profit/loss if you sold now = `(lastPx - avgCost) × qty`.

**Your Ledger (Transaction History)**

```bash
curl -H "Authorization: Bearer <TRADER_JWT>" \
  http://localhost:8080/api/trading-accounts/<account-id>/ledger-entries
```

**✅ Expected Output** (example):

```json
[
  {
    "type": "DEBIT",
    "amount": 250075.0,
    "fee": 25.0,
    "description": "BUY RELIANCE x100 @ 2500.50",
    "balanceAfter": 99925.0,
    "createdAt": "2025-11-15T10:00:00Z"
  }
]
```

**Confirm**:

- Your **balance** decreased by (quantity × executionPrice + fee).
- A **DEBIT** ledger entry records the transaction.
- Position qty and avgCost match the order.

---

## 6. Exercise SELL flow

**💰 Learning Objective**: Understand realized P&L and closing positions.

**Place a SELL Order**

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TRADER_JWT>" \
  -d '{
    "tradingAccountId": "<account-id>",
    "instrumentId": "<instrument-id>",
    "side": "SELL",
    "type": "MARKET",
    "qty": 50
  }'
```

**✅ Expected Behavior**:

- Order status = `FILLED`
- Your position qty reduces from 100 → 50
- New ledger entry shows CREDIT

**Check Portfolio Again**

```bash
curl -H "Authorization: Bearer <TRADER_JWT>" \
  http://localhost:8080/api/trading-accounts/<account-id>/positions
```

**What Changed**:

- Qty: 100 → 50 (you sold 50 units)
- Avg Cost: **unchanged** (stays at original purchase price, e.g., 2500.50)
- Realized P&L: Appears in ledger entry

**Check Ledger**

```bash
curl -H "Authorization: Bearer <TRADER_JWT>" \
  http://localhost:8080/api/trading-accounts/<account-id>/ledger-entries
```

**✅ New CREDIT Entry** (example):

```json
{
  "type": "CREDIT",
  "amount": 125225.0,
  "fee": 25.0,
  "description": "SELL RELIANCE x50 @ 2505.00, P&L: 225.00",
  "balanceAfter": 225150.0,
  "createdAt": "2025-11-15T10:01:00Z"
}
```

**Realized P&L Explained**:

- Sold 50 units at ₹2505
- Avg cost was ₹2500.50
- P&L per unit = 2505.00 - 2500.50 = ₹4.50
- Total P&L = 50 × 4.50 = **₹225** (profit!)

---

## 7. Frontend: Real-Time Trading UI

**🎮 Learning Objective**: See how the UI reflects trades in real-time via WebSocket.

**Frontend Setup**

From repo root:

```bash
cd src/main/webapp
npm run start  # or npm run dev
```

Open `http://localhost:9000` and log in as your test Trader.

**Test the Order Ticket**

1. Navigate to **Market Watch**
2. Click **Order Ticket** drawer for an instrument
3. Enter:
   - Side: **BUY** or **SELL**
   - Type: **MARKET**
   - Quantity: 10
4. Submit

**✅ Expected**:

- Toast notification shows success or error (e.g., "Order filled at ₹2505.50")
- No page reload needed

**View Orders & Trades**

1. Open **Orders & Trades** panel
2. See your recent orders and executions
3. Click an order to see its details (execution price, fill time, etc.)

**View Portfolio & Cash**

1. Open **Portfolio & Cash**
2. See:
   - **Positions table**: All holdings with qty, avg cost, MTM, P&L
   - **Ledger entries**: Recent debits and credits with running balance
   - **Cash balance**: Top of page, updates after each trade

**Real-Time Updates** ⏱️ (SC-004 Requirement)

1. In another window, place a BUY order via API (curl)
2. Watch the **Portfolio & Cash** page
3. Within ~2 seconds, you should see:
   - New position appear (or qty increase)
   - New ledger entry
   - Balance update
4. **All changes happen without refresh** thanks to WebSocket notifications

---

## 8. Common Issues & Troubleshooting

| Issue                                               | Cause                        | Solution                                                           |
| --------------------------------------------------- | ---------------------------- | ------------------------------------------------------------------ |
| Order rejected: "Insufficient funds"                | Balance too low              | Deposit more via admin or seed higher initial balance              |
| Order rejected: "Inactive instrument"               | Instrument not active        | Check market watch; ensure instrument status is `ACTIVE`           |
| Order rejected: "Quantity not multiple of lot size" | 100 units ordered but lot=1? | Check instrument lot size and adjust order qty                     |
| UI doesn't update after order                       | WebSocket not connected      | Check browser console for `/user/queue/orders` subscription errors |
| Position shows 0 qty after SELL                     | You sold all holdings        | Check ledger to confirm realized P&L. Now you can BUY again        |

---

## 9. Success Criteria Checklist

✅ **You've completed the quickstart when**:

- [ ] Placed a BUY order and saw status = FILLED
- [ ] Position created with correct qty and avg cost
- [ ] Cash balance decreased by (qty × price + fee)
- [ ] Placed a SELL order and saw realized P&L
- [ ] Position qty decreased and cash credited
- [ ] Viewed Orders & Trades UI without page refresh
- [ ] Viewed Portfolio & Cash and saw real-time updates via WebSocket
- [ ] Understood the connection between back-end ledger, position, balance and front-end UI

**Congratulations!** 🎉 You now understand the RNExchange M2 simple trading loop.
