# Quickstart – M6 QA Hardening, Branding & Demo-Ready Polish

This quickstart explains how to run the RNExchange demo for M6 and how to execute the key automated tests.

---

## 1. Prerequisites

- JDK 17 installed and on your `PATH`.
- Node.js and npm matching the versions in `pom.xml` (Java 17) and `package.json` (Node 22.x via the embedded `target/node` or your system).
- Docker (optional but recommended) if you want to use the PostgreSQL or services docker-compose files.

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

### 4.1 Trader “Day Trade” Flow

1. Navigate to `http://localhost:9000` and log in as `trader_demo`.
2. Confirm the “SIMULATED / NOT REAL MONEY” banner is clearly visible on primary trader views.
3. Use the Market Watch module to add a symbol (e.g., RELIANCE on NSE) to the watchlist.
4. Place a market buy order in that symbol and wait for it to fill.
5. Verify:
   - The new position appears with the correct quantity and average price.
   - The cash ledger/transactions view shows the corresponding debit and any fees.
6. Navigate to the Trader Statements view and confirm a statement appears for the trading day after EOD has been run (see 4.3).

### 4.2 Broker Funds Journal Flow

1. Log out and log in as `broker_demo`.
2. Open the Broker Dashboard and navigate to the funds/journal entry screen.
3. Select the demo trader account and create a funds credit journal entry; verify that the trader’s buying power and ledger reflect the change.
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
./mvnw -ntp gatling:test -Pperformance
```

(If a dedicated `performance` profile is not yet present, it should be added as part of M6 work; otherwise, run the existing Gatling simulations using the standard test profile.)

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
