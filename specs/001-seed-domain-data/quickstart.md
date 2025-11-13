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
