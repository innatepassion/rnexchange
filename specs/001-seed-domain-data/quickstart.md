# Quickstart: Seed Domain Trading Data Baseline

## Prerequisites

- Checkout branch `001-seed-domain-data`.
- Java 17 toolchain with Maven Wrapper and Node.js 22.x available.
- Existing `EXCHANGE_OPERATOR`, `BROKER_ADMIN`, and `TRADER` users present in the database.
- Liquibase contexts configured for `baseline` (faker context disabled) in `application-*.yml`.

## Baseline Reset & Seed Workflow

1. Stop any running RNExchange instance to release database connections.
2. Run the full test suite to observe initial red state (`./mvnw clean verify` and `npm run lint && npm run test`).
3. Start the application with dev profile:
   ```bash
   SPRING_PROFILES_ACTIVE=dev ./mvnw -Dskip.installnodenpm -Dskip.npm
   ```
4. Observe startup logs:
   - `BaselineSeedCleanupRunner` truncates non-system tables.
   - Liquibase applies `config/liquibase/data/0001-seed-domain-data.xml` under the `baseline` context.
5. Trigger asynchronous reseed (optional hot refresh):
   ```bash
   http POST :8080/api/admin/baseline-seed/run Authorization:"Bearer <EXCHANGE_OPERATOR_TOKEN>"
   ```
6. Poll job status:
   ```bash
   http GET :8080/api/admin/baseline-seed/status/{jobId} Authorization:"Bearer <EXCHANGE_OPERATOR_TOKEN>"
   ```

## Verification Checklist

- **Database sanity**: `SELECT COUNT(*) FROM exchange;` returns `3` (NSE, BSE, MCX).
- **Broker readiness**: Log in as `BROKER_ADMIN`; RN DEMO BROKING dashboard loads within 60 seconds and shows seeded instruments.
- **Trader flow**: Log in as trader, confirm trading account balance `₹1,000,000`, submit buy order for RELIANCE on NSE → order accepted with seeded margin rules.
- **Market holidays**: `/api/market-holidays?exchange=NSE` includes `2025-12-31` entry.
- **Margin rules**: `/api/margin-rules?exchange=NSE` exposes `NSE_CASH (0.20/0.15)` and `NSE_FNO (0.40/0.30)`.

## Role Validation Steps

1. **EXCHANGE_OPERATOR**

   - Generate a JWT for the seeded exchange operator account (reuse existing CI helper or `npm run admin:token` flow).
   - Trigger a reseed job: `http POST :8080/api/admin/baseline-seed/run Authorization:"Bearer <EXCHANGE_OPERATOR_TOKEN>"`.
   - Poll job status until it reports `COMPLETED` and confirm no faker data rows reappear: `http GET :8080/api/admin/baseline-seed/status/{jobId} Authorization:"Bearer <EXCHANGE_OPERATOR_TOKEN>"`.
   - Review structured logs for `phase=VALIDATION` entries capturing `actorRole=EXCHANGE_OPERATOR` with `status=SUCCESS`.

2. **BROKER_ADMIN**

   - Sign in through the web UI as the seeded broker administrator.
   - Open the Broker workspace and capture the login-to-ready duration (Cypress spec `broker-seed.cy.ts` persists this metric under `cypress/results/broker-seed.json`).
   - Verify the dashboard lists RN DEMO BROKING with the curated instrument catalog and no legacy demo brokers.
   - Confirm `/api/brokers/{id}` includes `exchangeMemberships` seeded by Liquibase when called with the broker admin token.

3. **TRADER**
   - Log in as each seeded trader persona; confirm the landing page shows an active trading account balance of `₹1,000,000`.
   - Place a buy order for RELIANCE on NSE (respecting seeded tick/lot sizes) and observe acceptance under the Micrometer duration threshold.
   - Download Cypress artifact `cypress/results/trader-seed.json` to review order flow duration and assertions captured in `trader-seed.cy.ts`.
   - Inspect structured audit logs (`service=OrderService`, `actorRole=TRADER`) to ensure entries include `instrument`, `outcome`, and `durationMs`.

## Test Suite Guardrails

- **Integration**: `./mvnw -Dtest=BaselineSeedServiceIT,BaselineSeedFailureIT test` ensures cleanup, reseed deterministic values, and failure paths.
- **REST**: `BaselineSeedResourceIT` validates OpenAPI contract and RBAC enforcement.
- **Cypress**: `npm run e2e -- broker/broker-seed.cy.ts trader/trader-seed.cy.ts` measures broker SLA and trader order success.
- **Cucumber**: `./mvnw -Pdev -Dskip.installnodenpm -Dskip.npm verify -Dcucumber.filter.tags="@baseline_seed"` validates trader journey end-to-end.

## Troubleshooting

- **Seed job stuck**: Check `/api/admin/baseline-seed/status/{jobId}` for `FAILED`; inspect application logs for truncated table errors or missing user mappings.
- **Residual faker data**: Ensure `spring.liquibase.contexts` excludes `faker` and rerun with `force=true` in request payload.
- **Missing users**: Confirm referenced logins exist in `user.csv`/database; update Liquibase changelog and rerun seed.
- **Performance regressions**: If SC-002 fails, capture Cypress artifacts and profile broker workspace API calls; leverage browser dev-tools to identify slow endpoints.
