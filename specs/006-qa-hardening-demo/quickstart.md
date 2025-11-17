# Quickstart – M6 QA Hardening, Branding & Demo-Ready Polish

This quickstart explains how to run the RNExchange demo for M6 and how to execute the key automated tests.

---

## 1. Prerequisites

- JDK 17 installed and on your `PATH`.
- Node.js and npm matching the versions in `pom.xml` (Java 17) and `package.json` (Node 22.x via the embedded `target/node` or your system).
- Docker (optional but recommended) if you want to use the PostgreSQL or services docker-compose files.

### 1.1 M6-Specific Setup

Before running M6 demo flows or tests, ensure the following:

1. **OpenAPI Contracts Merged**: The M6 contracts from `specs/006-qa-hardening-demo/contracts/m6-qa-hardening.openapi.yaml` have been merged into `src/main/resources/swagger/api.yml` and code generation has been run:

   ```bash
   ./mvnw generate-sources
   ```

   This generates delegate interfaces in `src/main/java/com/rnexchange/web/api/` and DTOs in `src/main/java/com/rnexchange/service/api/dto/`.

2. **Baseline Seed Applied**: Ensure the baseline seed has been run to create demo users (`trader_demo`, `broker_demo`, `exchange_demo`) with known starting balances:

   ```bash
   # Start the application with baseline context
   npm run watch
   # Then trigger baseline seed via API or use the admin endpoint
   ```

3. **Environment Variables** (if needed): Check `src/main/resources/config/application-dev.yml` for any M6-specific configuration, such as:

   - Liquibase contexts (should include `baseline` for demo data)
   - Demo user credentials and initial balances
   - WebSocket configuration for real-time updates

4. **Test Scripts Verified**: Confirm that Cypress, Jest, and Gatling are properly configured:
   - Cypress: `cypress.config.ts` exists and `npm run e2e:headless` works
   - Jest: `npm test` runs React component tests
   - Gatling: `./mvnw gatling:test` runs performance simulations

---

## 2. Start the Application (Dev Baseline)

From the repository root (`/home/explorer/Development/rnexchange`):

```bash
# Install JS dependencies once (if not already done)
npm install

# Start the Spring Boot + Webpack dev stack with dev+baseline Liquibase contexts
npm run watch
```

This will:

- Run the backend with the `dev` profile (and `baseline` Liquibase context) using H2 (`application-dev.yml`).
- Serve the React front-end on `http://localhost:9000`.

Once the app is up, open `http://localhost:9000` in a browser to see the RNExchange-branded landing page (after M6 implementation).

---

## 3. Demo Users & Roles

M6 assumes three fixed demo users (credentials to be aligned with the seed/baseline tooling):

- Trader demo: `trader_demo` (role `TRADER`)
- Broker demo: `broker_demo` (role `BROKER_ADMIN`)
- Exchange operator demo: `exchange_demo` (role `EXCHANGE_OPERATOR`)

These users should be provisioned and resettable via the existing baseline seed tooling (see the M1/M3 specs and seed scripts); before running demos or Cypress tests, ensure the baseline seed has been applied so accounts and balances are in a known state.

---

## 4. Running the M6 Demo Flows

### 4.1 Trader "Day Trade" Flow

1. Navigate to `http://localhost:9000` and log in as `trader_demo`.
2. Confirm the "SIMULATED / NOT REAL MONEY" banner is clearly visible on primary trader views.
3. Use the Market Watch module to add a symbol (e.g., RELIANCE on NSE) to the watchlist.
4. Place a market buy order in that symbol and wait for it to fill.
5. Verify:
   - The new position appears with the correct quantity and average price.
   - The cash ledger/transactions view shows the corresponding debit and any fees.
6. Navigate to the Trader Statements view and confirm a statement appears for the trading day after EOD has been run (see 4.3).

### 4.2 Broker Funds Journal Flow

1. Log out and log in as `broker_demo`.
2. Open the Broker Dashboard and navigate to the funds/journal entry screen.
3. Select the demo trader account and create a funds credit journal entry; verify that the trader's buying power and ledger reflect the change.
4. Create a debit large enough to push the balance negative; confirm:
   - The journal entry is accepted.
   - The UI clearly flags the trader as negative or at-risk (badge/colour/warning).
5. After EOD, verify that the trader/broker statements reflect these journal entries and clearly show the negative or at-risk status.

### 4.3 Exchange EOD & Statements Flow

1. Log out and log in as `exchange_demo`.
2. Open the Exchange Overview / EOD console.
3. Trigger EOD for the current trade date; under the hood this calls `POST /api/settlements/eod?date=YYYY-MM-DD`.
4. Confirm that an EOD settlement batch appears in the UI and that metrics (accounts processed, positions processed, net P&L) look reasonable.
5. Re-run EOD for the same date and confirm:
   - The operation succeeds and overwrites prior EOD MTM adjustments and statements (idempotent behaviour).
6. Log back in as `trader_demo` and `broker_demo` to open statements and verify balances/P&L reconcile with positions and ledger entries for that date.

---

## 4.4 "Day in the Life" Demo Script (M6 Phase 8, T061)

This comprehensive demo script walks through a complete trading day across all three roles. **Target completion time: <15 minutes** (SC-001, SC-004).

### Prerequisites

- Application running at `http://localhost:9000`
- Baseline seed has been applied (demo users exist with known balances)
- Current date: Use today's date for EOD operations

### Demo Script Steps

#### Part 1: Trader Day Trade (5 minutes)

1. **Landing & Login** (30 seconds)

   - Open `http://localhost:9000`
   - Verify RNExchange branding (logo, name, description)
   - Click "Sign In" or navigate to login
   - Log in as `trader_demo` / `trader_demo`
   - Verify redirect to Market Watch (`/market-watch`)
   - Confirm "SIMULATED / NOT REAL MONEY" banner is visible

2. **Watchlist Management** (1 minute)

   - In Market Watch, verify watchlist is loaded
   - Add symbol "RELIANCE" to watchlist (if not already present)
   - Verify symbol appears in Market Watch table with live price updates
   - Confirm real-time price updates are visible

3. **Place Market Order** (2 minutes)

   - Click "Trade" button for RELIANCE symbol
   - In order ticket:
     - Side: BUY
     - Type: MARKET
     - Quantity: 10
   - Review order preview (estimated cost, fees)
   - Click "Submit Order"
   - Wait for order fill confirmation (<2 seconds)
   - Verify order status shows "FILLED"

4. **Verify Position & Ledger** (1.5 minutes)
   - Navigate to Portfolio view
   - Verify new position appears:
     - Symbol: RELIANCE
     - Quantity: 10
     - Average price matches execution price
   - Navigate to Cash/Ledger view
   - Verify ledger entry shows:
     - Debit for trade amount
     - Any fees deducted
     - Running balance updated
   - Confirm cash balance decreased by trade amount + fees

#### Part 2: Broker Funds Adjustment (3 minutes)

5. **Broker Login & Dashboard** (30 seconds)

   - Log out from trader account
   - Log in as `broker_demo` / `broker_demo`
   - Verify redirect to Broker Dashboard (`/broker/dashboard`)
   - Confirm "SIMULATED / NOT REAL MONEY" banner is visible

6. **Create Funds Credit** (1 minute)

   - Navigate to Broker Journal / Funds Entry
   - Select trader account (the `trader_demo` account)
   - Create credit journal entry:
     - Direction: Credit
     - Amount: 5,000.00
     - Reason: "Demo funds credit"
   - Submit journal entry
   - Verify success message
   - Navigate to trader account overview
   - Confirm buying power increased by 5,000.00

7. **Create Funds Debit (Negative Balance)** (1.5 minutes)
   - Return to Broker Journal
   - Create debit journal entry:
     - Direction: Debit
     - Amount: 10,000.00 (large enough to push balance negative)
     - Reason: "Demo funds debit - negative balance test"
   - Submit journal entry
   - Verify entry is accepted (negative balances allowed per FR-004)
   - Navigate to trader account overview
   - Confirm account shows negative/at-risk flag:
     - Red badge or warning indicator
     - Clear messaging about negative balance
   - Verify ledger shows the debit entry with updated (negative) balance

#### Part 3: Exchange EOD & Statements (4 minutes)

8. **Exchange Login & Overview** (30 seconds)

   - Log out from broker account
   - Log in as `exchange_demo` / `exchange_demo`
   - Verify redirect to Exchange Overview (`/exchange/overview`)
   - Review exchange metrics and system status

9. **Run EOD Settlement** (2 minutes)

   - Navigate to EOD Console
   - Enter today's date (format: YYYY-MM-DD)
   - Click "Run EOD" or trigger EOD settlement
   - Wait for EOD completion (should complete within 5 minutes for demo-scale data)
   - Verify EOD batch appears in settlement list:
     - Status: PROCESSED or COMPLETED
     - Metrics displayed:
       - Accounts processed
       - Positions processed
       - Net P&L
   - Note the settlement batch ID for reference

10. **Verify EOD Idempotency** (1 minute)

    - Re-run EOD for the same date
    - Verify operation succeeds
    - Confirm no duplicate entries created
    - Verify statements are overwritten (not duplicated)
    - Confirm MTM adjustments are recomputed (idempotent behavior)

11. **View Statements** (1.5 minutes)
    - Log back in as `trader_demo`
    - Navigate to Statements view (`/trader/statements`)
    - Locate statement for today's date
    - Open statement (HTML view)
    - Verify statement includes:
      - Opening balance
      - Closing balance
      - Trade entries (BUY order)
      - Journal entries (credit and debit)
      - P&L calculations
      - "SIMULATED / NOT REAL MONEY" disclaimer (FR-018)
    - Verify balances reconcile:
      - Opening balance + credits - debits = closing balance
      - Position P&L matches unrealized P&L
    - Log back in as `broker_demo`
    - Navigate to Broker Settlements view
    - Open broker summary for today's date
    - Verify broker summary shows:
      - All trader accounts under broker
      - Aggregated balances and P&L
      - Journal entries summary
      - Simulation disclaimer

#### Part 4: Verification & Wrap-up (2 minutes)

12. **Cross-Role Verification** (1 minute)

    - Verify all three roles can access their respective dashboards
    - Confirm role-based navigation shows only relevant items
    - Verify generic JHipster menus (Entities, Administration) are hidden for Trader and Broker
    - Confirm Exchange Operator sees only relevant admin items

13. **Help Content Review** (1 minute)
    - As Trader: Open "How to use RNExchange" help panel
    - Verify help content explains trader flows and concepts
    - As Broker: Open broker help panel
    - Verify help content explains broker responsibilities
    - As Exchange: Open exchange help panel
    - Verify help content explains EOD and system operations

### Expected Results

- **Total Time**: <15 minutes (SC-001, SC-004)
- **All flows complete** without manual data edits or configuration changes
- **All balances reconcile** across positions, ledger, and statements
- **All role-based access** works correctly
- **All simulation disclaimers** are visible
- **No blocking usability issues** encountered

### Troubleshooting

If you encounter issues:

1. **Demo users not found**: Run baseline seed via Exchange Operator dashboard or API
2. **Balances don't reconcile**: Check that EOD was run for the correct date
3. **Statements not appearing**: Verify EOD completed successfully and check date filter
4. **Negative balance not flagged**: Check broker journal entry was created and account view is refreshed

### Automated Test Coverage

All steps in this demo script are covered by automated tests:

- **Cypress E2E**: `trader/trader-trading.cy.ts`, `broker_journal.cy.ts`, `settlement_eod.cy.ts`, `trader_statements.cy.ts`
- **Integration Tests**: `TraderDayTradeIT.java`, `BrokerFundsJournalIT.java`, `EodIdempotencyIT.java`
- **Contract Tests**: Settlement and statement API contracts
- **Performance Tests**: Gatling simulations for order placement and EOD

See Section 5 for details on running automated tests.

---

## 5. Automated Tests

### 5.1 Cypress End-to-End Tests

With the dev stack running (`npm run watch`):

```bash
# Run Cypress E2E tests headless against the dev stack
npm run e2e:headless
```

This will execute scenarios including:

- Trader trading flow (`trader/trader-trading.cy.ts`).
- Broker dashboard/journal/settlement flows (`broker_dashboard.cy.ts`, `broker_journal.cy.ts`, `broker_settlements.cy.ts`).
- Trader statements flow (`trader_statements.cy.ts`).
- EOD/settlement flow (`settlement_eod.cy.ts`).

### 5.2 Jest & React Component Tests

```bash
npm test
```

This runs the Jest suite, including React tests for core settlement and statement components (e.g., `TraderStatements.spec.tsx`).

### 5.3 Backend Tests & Cucumber

```bash
npm run backend:unit:test
```

This executes the Maven-based backend suite, including contract, integration, and Cucumber tests for broker, settlement, and trading services.

### 5.4 Gatling Performance Tests

To evaluate demo-scale performance for M6:

```bash
# Run Gatling performance tests (no profile needed, uses default test profile)
./mvnw -ntp gatling:test
```

**Note**: A dedicated `performance` profile can be added to `pom.xml` in the future to gate heavy load tests separately from standard builds. For now, Gatling simulations run with the default test profile.

**M6 Performance Targets** (from constitution):

- p95 order placement latency <250 ms
- EOD settlement for ~10,000 positions completes within 5 minutes
- WebSocket tick throughput ~10,000 updates/sec without demo-breaking errors

Gatling simulations should be configured to approximate:

- ~1,000 concurrent traders.
- Hundreds of market data updates per second.
- 5–10 orders per second.  
  and verify that p95 order placement latency and EOD completion times meet the constitution’s targets.

---

## 6. Where to Go Next

- For implementation details and technical constraints, see `plan.md` and `data-model.md`.
- For API shapes and integration points, see `contracts/m6-qa-hardening.openapi.yaml`.
- For broader project principles, consult `.specify/memory/constitution.md`.
