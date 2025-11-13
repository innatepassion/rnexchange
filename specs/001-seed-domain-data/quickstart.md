# Quickstart — Seed Domain Trading Data Baseline

## Prerequisites

- Checkout branch `001-seed-domain-data`.
- Ensure dev profile uses the new Liquibase context (`baseline`) and that faker context is disabled.
- Optional: run `npm install` and `./mvnw verify -DskipITs` once to download dependencies.

## Baseline Reset & Seed

1. Stop any running RNExchange instance.
2. Truncate legacy demo data:
   ```bash
   ./mvnw -Pdev -DskipTests liquibase:clearCheckSums
   ```
   (The upcoming cleanup runner handles table truncation automatically.)
3. Launch the application so Liquibase rebuilds schema and applies `0001-seed-domain-data.xml`:
   ```bash
   ./mvnw -Dskip.installnodenpm -Dskip.npm
   ```
4. (Optional admin trigger) Call the asynchronous refresh endpoint if you need to re-seed without restarting:
   ```bash
   http POST :8080/api/admin/baseline-seed/refresh Authorization:"Bearer <token>" force:=true
   ```

## Verification Checklist

- **Database sanity**: `SELECT COUNT(*) FROM exchange;` should return `3`.
- **Broker desk**: Login as broker user, confirm RN DEMO BROKING is active and linked to NSE instruments.
- **Trader workflow**:
  - Login as seeded trader.
  - Confirm trading account balance `₹1,000,000`.
  - Place a mock order for `RELIANCE` on NSE; order should validate margin using seeded rules.
- **Market holidays**: `/api/market-holidays?exchange=NSE` returns 2025-12-31 entry.
- **Margin rules**: `/api/margin-rules?exchange=NSE` includes `NSE_CASH` (0.20/0.15) and `NSE_FNO` (0.40/0.30).

## Test Suite (TDD Guardrails)

- Run Liquibase idempotency test: `./mvnw -Dtest=BaselineSeedIT test`.
- Update Cucumber scenario `baseline_seed.feature` to cover admin/broker/trader smoke flows.
- Execute Cypress smoke test `broker-seed.cy.ts` to validate UI wiring.

## Troubleshooting

- **Seed job stuck**: Call `/api/admin/baseline-seed/status/{jobId}`; if `FAILED`, inspect `errors` array and application logs.
- **Residual faker data**: Ensure `spring.liquibase.contexts` excludes `faker`; re-run refresh with `force=true`.
- **User mapping errors**: Verify entries in `user.csv` still include broker/trader logins referenced by Liquibase changelog.
