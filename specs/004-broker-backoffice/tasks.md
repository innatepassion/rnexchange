# Tasks — M3 Broker Back Office

## Phase 1 — Setup

- [x] T001 Ensure OpenAPI contracts merged into `src/main/resources/swagger/api.yml`
- [x] T002 Generate server stubs and DTOs via `./mvnw generate-sources`
- [x] T003 Create backend module skeletons `src/main/java/com/rnexchange/web/rest/broker/` and `src/main/java/com/rnexchange/service/broker/`
- [x] T004 Create frontend module skeleton `src/main/webapp/app/modules/broker/`
- [x] T005 Configure role guard for `BROKER_ADMIN` routes in `src/main/webapp/app/shared/auth/private-route.tsx`
- [x] T006 Add Liquibase changelog placeholder for IdempotencyToken table `src/main/resources/db/changelog/` (only if not existing)

## Phase 2 — Foundational

- [x] T007 Implement broker scoping helper in `src/main/java/com/rnexchange/service/broker/BrokerScopeService.java`
- [x] T008 [P] Add broker-scoped queries in `src/main/java/com/rnexchange/repository/TradingAccountRepository.java`
- [x] T008a [P] Add broker-scoped queries in `src/main/java/com/rnexchange/repository/TraderProfileRepository.java`
- [x] T008b [P] Add broker-scoped queries in `src/main/java/com/rnexchange/repository/PositionRepository.java`
- [x] T008c [P] Add broker-scoped queries in `src/main/java/com/rnexchange/repository/LedgerEntryRepository.java`
- [x] T009 Define DTOs for overview, traders, and journal in `src/main/java/com/rnexchange/service/dto/`
- [x] T010 Wire `@PreAuthorize('hasRole("BROKER_ADMIN")')` on controllers in `src/main/java/com/rnexchange/web/rest/broker/BrokerOverviewResource.java`, `src/main/java/com/rnexchange/web/rest/broker/BrokerTradersResource.java`, `src/main/java/com/rnexchange/web/rest/broker/BrokerJournalResource.java`

### Foundational — Entity/JDL and DB

- [x] T035 Add `IdempotencyToken` entity to `rnexchange.jdl` (fields: brokerId, tradingAccountId, token, ledgerEntryId, createdAt)
- [ ] T036 Regenerate entities from JDL (`./mvnw jhipster-jdl:import`) and commit generated mappers/repos
- [x] T037 Add Liquibase changelog `src/main/resources/db/changelog/2025-11-16T00-IdempotencyToken.xml`

## Phase 3 — User Story 1 (P1): Broker Dashboard

- [x] T038 [P] [US1] Contract test for GET `/api/broker/overview` in `src/test/java/com/rnexchange/contract/broker/BrokerOverviewContractTest.java`
- [x] T011 [US1] Implement overview service computing activeTraderCount, totalCash, totalEquityExposure, top utilization in `service/broker/BrokerOverviewService.java`
- [x] T012 [P] [US1] Add REST endpoint GET `/api/broker/overview` in `web/rest/broker/BrokerOverviewResource.java`
- [x] T013 [P] [US1] Compute utilization with ε=1.0 and exclude stale (>60s) prices in `service/broker/BrokerOverviewService.java`
- [x] T014 [US1] Frontend Dashboard page and cards in `webapp/app/modules/broker/dashboard/index.tsx`
- [x] T015 [P] [US1] Frontend service for overview API in `webapp/app/modules/broker/services/overview.service.ts`
- [x] T016 [US1] Add table for top utilization with rank and indicators in `webapp/app/modules/broker/dashboard/components/UtilizationTable.tsx`
- [x] T042 [US1] Backend integration test for RBAC and broker scoping in `src/test/java/com/rnexchange/integration/broker/BrokerOverviewIT.java`
- [x] T046 [P] [US1] Cypress E2E for dashboard overview in `src/test/javascript/cypress/e2e/broker_dashboard.cy.ts`

## Phase 4 — User Story 2 (P1): Clients list and details

- [x] T039 [P] [US2] Contract test for GET `/api/broker/traders` in `src/test/java/com/rnexchange/contract/broker/BrokerTradersContractTest.java`
- [x] T040 [P] [US2] Contract test for GET `/api/broker/traders/{traderId}` in `src/test/java/com/rnexchange/contract/broker/BrokerTraderDetailsContractTest.java`
- [x] T017 [US2] Implement list traders endpoint GET `/api/broker/traders` in `web/rest/broker/BrokerTradersResource.java`
- [x] T018 [P] [US2] Service method to fetch trader summaries with P&L in `service/broker/BrokerTradersService.java`
- [x] T019 [US2] Implement trader details endpoint GET `/api/broker/traders/{traderId}` in `web/rest/broker/BrokerTradersResource.java`
- [x] T020 [P] [US2] Include recent ledger snippet (last 10) in details in `service/broker/BrokerTradersService.java`
- [x] T021 [US2] Frontend Clients page with table in `webapp/app/modules/broker/clients/index.tsx`
- [x] T022 [P] [US2] Frontend service for traders APIs in `webapp/app/modules/broker/services/traders.service.ts`
- [x] T023 [US2] Details drawer/modal showing core account and last 10 ledger rows in `webapp/app/modules/broker/clients/components/TraderDetailsDrawer.tsx`
- [x] T043 [US2] Backend integration test for broker scoping on list/details in `src/test/java/com/rnexchange/integration/broker/BrokerTradersIT.java`
- [x] T047 [P] [US2] Cypress E2E for clients list/details in `src/test/javascript/cypress/e2e/broker_clients.cy.ts`

## Phase 5 — User Story 3 (P2): Funds journal credit/debit

- [x] T041 [P] [US3] Contract test for POST `/api/broker/traders/{tradingAccountId}/journal` in `src/test/java/com/rnexchange/contract/broker/BrokerJournalContractTest.java`
- [x] T024 [US3] Implement POST `/api/broker/traders/{tradingAccountId}/journal` in `web/rest/broker/BrokerJournalResource.java`
- [x] T025 [P] [US3] Journal application service with idempotency (header `Idempotency-Key`) in `service/broker/BrokerJournalService.java`
- [x] T026 [P] [US3] Add `IdempotencyTokenRepository` in `src/main/java/com/rnexchange/repository/IdempotencyTokenRepository.java`
- [x] T026a [US3] Enforce 6h retention via cleanup job in `src/main/java/com/rnexchange/service/broker/IdempotencyCleanupService.java`
- [x] T027 [US3] Adjust cash balance and create `JOURNAL_CREDIT/DEBIT` ledger entries in `service/broker/BrokerJournalService.java`
- [x] T028 [US3] Frontend journal drawer form (credit/debit, amount, reason) in `webapp/app/modules/broker/clients/components/JournalForm.tsx`
- [x] T029 [P] [US3] Frontend wire-up to refresh balances and recent ledger in `webapp/app/modules/broker/clients/index.tsx`
- [x] T044 [US3] Backend integration test: journal updates balance and creates ledger in `src/test/java/com/rnexchange/integration/broker/BrokerJournalIT.java`
- [ ] T045 [US3] Integration test (FR‑008): downstream trading/risk reflect updated balance in `src/test/java/com/rnexchange/integration/broker/BrokerDownstreamBalanceRespectIT.java`
- [ ] T048 [P] [US3] Cypress E2E for journal flow in `src/test/javascript/cypress/e2e/broker_journal.cy.ts`
      **_ End Patch_**"`  `json
      {"error":"Invalid patch content"} `  `json
      {"note":"The previous error occurred due to extra formatting. Retrying with proper patch content."} ``` \*\*\* Begin Patch

## Phase 6 — Polish & Cross-Cutting

- [x] T030 Add stale-price indicator in UI rows where exposure excluded in `webapp/app/modules/broker/dashboard/components/UtilizationTable.tsx`
- [x] T031 Add audit logging for all journal actions in `src/main/java/com/rnexchange/service/broker/BrokerJournalService.java`
- [x] T032 Ensure RBAC tests/assertions on endpoints in `src/test/java/com/rnexchange/integration/**`
- [x] T033 Update Swagger doc annotations/comments in `src/main/java/com/rnexchange/web/rest/broker/BrokerOverviewResource.java`, `src/main/java/com/rnexchange/web/rest/broker/BrokerTradersResource.java`, `src/main/java/com/rnexchange/web/rest/broker/BrokerJournalResource.java`
- [x] T034 Update README/quickstart links if needed in `specs/004-broker-backoffice/quickstart.md`
- [x] T050 Add Gatling smoke test for overview and traders APIs in `src/test/gatling/simulations/BrokerBackofficeSimulation.scala` (targets: GET /broker/overview p95 < 250ms; GET /broker/traders p95 < 300ms; error rate < 1%)
- [x] T051 Ensure idempotency key logged in audit trail in `src/main/java/com/rnexchange/service/broker/BrokerJournalService.java`

## Dependencies

- Phase order: Setup → Foundational → US1 → US2 → US3 → Polish
- US1 has no dependency on US2/US3
- US2 depends on Foundational
- US3 depends on Foundational and US2 (for details UI integration)

## Parallel Execution Examples

- Backend vs Frontend tasks in the same story can run in parallel where marked [P]
- Repository queries (T008), REST wiring (T012, T017), and frontend services (T015, T022) are parallelizable

## Implementation Strategy

- Deliver MVP with US1 first (dashboard overview and top utilization)
- Add US2 (clients list/details) next
- Implement US3 (journals) after US2 to leverage the details drawer
