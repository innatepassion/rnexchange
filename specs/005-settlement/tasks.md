# Tasks — M5 Settlement & Reporting

**Input**: Design documents from `/specs/005-settlement/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/settlement.openapi.yaml`

Tasks are grouped by user story so each story can be implemented and tested independently. TDD is assumed: write tests before implementation for each story.

## Phase 1 — Setup (Shared Infrastructure)

**Purpose**: Hook settlement contracts into the existing JHipster app and create skeleton modules.

- [x] T001 Ensure settlement contracts from `specs/005-settlement/contracts/settlement.openapi.yaml` are merged into `src/main/resources/swagger/api.yml`
- [x] T002 Run code generation for updated OpenAPI via `./mvnw generate-sources`
- [x] T003 Create backend settlement service package skeleton `src/main/java/com/rnexchange/service/settlement/` (e.g., `SettlementService.java`, `StatementService.java` interfaces)
- [x] T004 Create backend REST package skeleton `src/main/java/com/rnexchange/web/rest/settlement/` for settlement and statements resources
- [x] T005 Create frontend module skeletons:
  - `src/main/webapp/app/modules/exchange/settlement/`
  - `src/main/webapp/app/modules/broker/settlements/`
  - `src/main/webapp/app/modules/trader/statements/`

---

## Phase 2 — Foundational (Blocking Prerequisites)

**Purpose**: Core data model and service scaffolding needed before any user story.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

### Foundational — Data Model and Enums

- [x] T006 Update `rnexchange.jdl` to extend `LedgerEntryType` with `EOD_MTM_CREDIT` and `EOD_MTM_DEBIT`, regenerate entities, and update Liquibase changelog under `src/main/resources/db/changelog/`
- [x] T007 Update `rnexchange.jdl` to extend `SettlementStatus` with `FAILED`, regenerate entities, and update corresponding Liquibase changelog in `src/main/resources/db/changelog/`
- [x] T008 Define `ReportLink`-style entity in `rnexchange.jdl` (fields: refDate, reportType, tradingAccountId, brokerId, settlementBatchId, relativeUrl), regenerate entities, and add a Liquibase changelog for the new table in `src/main/resources/db/changelog/`

### Foundational — Repositories, DTOs, and Services

- [x] T009 [P] Add settlement-specific queries in `src/main/java/com/rnexchange/repository/PositionRepository.java` and `DailySettlementPriceRepository.java` to fetch open positions and daily settlement prices by `refDate` and instrument
- [x] T010 [P] Add repository for report links in `src/main/java/com/rnexchange/repository/ReportLinkRepository.java`
- [x] T011 Define DTOs for `SettlementBatchDTO`, `StatementSummary`, and `BrokerSettlementSummary` in `src/main/java/com/rnexchange/service/dto/`
- [x] T012 Create `SettlementService` interface and base implementation skeleton in `src/main/java/com/rnexchange/service/settlement/SettlementService.java` with method `runEod(LocalDate tradeDate)`

**Checkpoint**: Foundation ready — user story implementation can now begin in parallel.

---

## Phase 3 — User Story 1 (P1): Exchange Operator runs EOD settlement 🎯 MVP

**Goal**: Allow an Exchange Operator to run a deterministic EOD batch for a trade date, update positions and ledgers, and see batch status in the Exchange console.

**Independent Test**: After placing trades for multiple accounts, running EOD once for a date results in a single batch with status `PROCESSED`, updated positions and ledgers, and visible batch row in the Exchange “Settlement” tab.

### Tests for User Story 1 (write first)

- [x] T013 [P] [US1] Contract test for POST `/api/settlements/eod` in `src/test/java/com/rnexchange/contract/settlement/RunEodSettlementContractTest.java`
- [x] T014 [P] [US1] Contract test for GET `/api/settlements` in `src/test/java/com/rnexchange/contract/settlement/ListSettlementBatchesContractTest.java`
- [x] T015 [P] [US1] Unit tests for MTM calculations and aggregation in `src/test/java/com/rnexchange/service/settlement/SettlementServiceTest.java`
- [x] T016 [US1] Integration test for successful EOD run (positions, ledgers, batch status) in `src/test/java/com/rnexchange/integration/settlement/SettlementEodSuccessIT.java`
- [x] T017 [US1] Integration test for missing price / failure path (batch marked FAILED, no partial updates) in `src/test/java/com/rnexchange/integration/settlement/SettlementEodFailureIT.java`

### Implementation for User Story 1

- [x] T018 [US1] Implement `runEod(LocalDate tradeDate)` in `src/main/java/com/rnexchange/service/settlement/SettlementService.java` to:
  - resolve settlement prices from `DailySettlementPrice` or 1-minute bar close
  - compute per-position MTM and per-account net P&L
  - update `Position.lastPx`, `unrealizedPnl`, `realizedPnl`
  - post single `EOD_MTM_CREDIT/DEBIT` ledger entry per account
  - create/update `SettlementBatch` and associated `ReportLink` records
- [x] T019 [P] [US1] Implement REST endpoint POST `/api/settlements/eod` in `src/main/java/com/rnexchange/web/rest/settlement/SettlementResource.java` with `@PreAuthorize("hasRole('EXCHANGE_OPERATOR')")`
- [x] T020 [P] [US1] Implement REST endpoint GET `/api/settlements` in `src/main/java/com/rnexchange/web/rest/settlement/SettlementResource.java` returning `SettlementBatchDTO` list with basic metrics
- [x] T021 [US1] Implement re-run semantics in `SettlementService` (supersede prior EOD MTM entries and report links for the same `refDate` while keeping an audit trail)
- [x] T022 [US1] Add structured logging for each EOD batch (refDate, accountsProcessed, positionsProcessed, netPnl, status) in `src/main/java/com/rnexchange/service/settlement/SettlementService.java`

### Domain Events for User Story 1

- [x] T058 [P] [US1] Define `SettlementCompletedEvent` and `SettlementFailedEvent` domain events in `src/main/java/com/rnexchange/service/settlement/event/SettlementCompletedEvent.java` and `SettlementFailedEvent.java`
- [x] T059 [US1] Publish `SettlementCompletedEvent` and `SettlementFailedEvent` from `runEod` in `src/main/java/com/rnexchange/service/settlement/SettlementService.java` after successful completion or fatal failure
- [x] T060 [P] [US1] Extend `SettlementServiceTest` and EOD integration tests to assert that the appropriate domain events are emitted on success and failure in `src/test/java/com/rnexchange/service/settlement/SettlementServiceTest.java` and `src/test/java/com/rnexchange/integration/settlement/SettlementEodSuccessIT.java` / `SettlementEodFailureIT.java`

### UI for User Story 1 — Exchange Operator “Settlement” tab

- [x] T023 [P] [US1] Implement Exchange "Settlement" module route and page shell in `src/main/webapp/app/modules/exchange/settlement/index.tsx`
- [x] T024 [P] [US1] Implement table component showing batches (date, status, accountsProcessed, positionsProcessed, netPnl) in `src/main/webapp/app/modules/exchange/settlement/components/SettlementBatchTable.tsx`
- [x] T025 [P] [US1] Implement frontend service for settlement APIs in `src/main/webapp/app/modules/exchange/settlement/services/settlement.service.ts`
- [x] T026 [US1] Wire "Run EOD for Today" and "Re-run for this date" buttons with status polling and basic progress feedback in `src/main/webapp/app/modules/exchange/settlement/index.tsx`
- [x] T027 [P] [US1] Jest tests for `SettlementBatchTable` and EOD button behaviors in `src/test/javascript/spec/settlement/SettlementBatchTable.spec.tsx`
- [x] T028 [P] [US1] Cypress E2E for Exchange Operator running EOD and viewing batch status in `src/test/javascript/cypress/e2e/settlement_eod.cy.ts`

**Checkpoint**: User Story 1 fully functional and independently testable (Exchange Operator can run and inspect EOD).

---

## Phase 4 — User Story 2 (P2): Trader views daily statements

**Goal**: Allow a Trader to see a dated list of their daily statements, and open each as an HTML page showing opening balance, cash flows, trades, fees, EOD MTM P&L, and closing balance.

**Independent Test**: After EOD runs for dates where the trader has activity, logging in as that trader shows those dates in the “Statements” list, and each HTML statement reconciles with the ledger for that date.

### Tests for User Story 2 (write first)

- [x] T029 [P] [US2] Contract test for GET `/api/statements` in `src/test/java/com/rnexchange/contract/settlement/ListStatementsContractTest.java`
- [x] T030 [P] [US2] Contract test for GET `/api/statements/{statementId}/html` in `src/test/java/com/rnexchange/contract/settlement/GetStatementHtmlContractTest.java`
- [x] T031 [US2] Integration test verifying statement reconciliation (opening + cash flows + trades + fees + EOD MTM = closing) in `src/test/java/com/rnexchange/integration/settlement/TraderStatementsIT.java`

### Implementation for User Story 2

- [x] T032 [US2] Implement `StatementService` to assemble per-account daily statements from ledger entries, positions, and EOD MTM in `src/main/java/com/rnexchange/service/settlement/StatementService.java`
- [x] T033 [P] [US2] Implement REST endpoint GET `/api/statements` returning `StatementSummary` list for the authenticated trader in `src/main/java/com/rnexchange/web/rest/settlement/StatementResource.java` with `@PreAuthorize("hasRole('TRADER')")`
- [x] T034 [P] [US2] Implement REST endpoint GET `/api/statements/{statementId}/html` rendering an HTML statement in `src/main/java/com/rnexchange/web/rest/settlement/StatementResource.java`
- [x] T035 [US2] Enforce ownership checks in `StatementService` / `StatementResource` so traders can only access their own statements
- [x] T036 [US2] Add EOD simulated-environment disclaimer text to the HTML statement template in `src/main/resources/templates/settlement/statement.html`

### UI for User Story 2 — Trader “Statements” screen

- [x] T037 [P] [US2] Implement Trader “Statements” page listing `StatementSummary` rows in `src/main/webapp/app/modules/trader/statements/index.tsx`
- [x] T038 [P] [US2] Implement frontend service for statements APIs in `src/main/webapp/app/modules/trader/statements/services/statements.service.ts`
- [x] T039 [US2] Wire “View” link to open the `htmlUrl` in a new tab and ensure TRADER-only route guard in `src/main/webapp/app/modules/trader/statements/index.tsx`
- [x] T040 [P] [US2] Jest tests for Trader statements list and "View" behavior in `src/test/javascript/spec/settlement/TraderStatements.spec.tsx`
- [x] T041 [P] [US2] Cypress E2E for a trader viewing statements after EOD in `src/test/javascript/cypress/e2e/trader_statements.cy.ts`

**Checkpoint**: User Stories 1 and 2 both work independently (EOD + trader statements).

---

## Phase 5 — User Story 3 (P3): Broker Admin reviews settlement and client reports

**Goal**: Allow a Broker Admin to view per-day broker-level settlement summaries (aggregated balances and P&L) and drill into client statements for the same date.

**Independent Test**: After EOD runs, logging in as a Broker Admin shows a list of settlement days with aggregate totals; selecting a day reveals a summary that reconciles to the sum of client statements, and the admin can navigate to individual client statements.

### Tests for User Story 3 (write first)

- [x] T042 [P] [US3] Contract test for GET `/api/broker/settlements` in `src/test/java/com/rnexchange/contract/settlement/BrokerSettlementsContractTest.java`
- [x] T043 [US3] Integration test verifying broker-level totals and scoping (only own broker data) in `src/test/java/com/rnexchange/integration/settlement/BrokerSettlementsIT.java`

### Implementation for User Story 3

- [x] T044 [US3] Implement `BrokerSettlementService` to aggregate client balances and EOD MTM per broker and generate `BrokerSettlementSummary` in `src/main/java/com/rnexchange/service/settlement/BrokerSettlementService.java`
- [x] T045 [P] [US3] Implement REST endpoint GET `/api/broker/settlements` returning broker-level summaries for the authenticated broker in `src/main/java/com/rnexchange/web/rest/broker/BrokerSettlementResource.java` with `@PreAuthorize("hasRole('BROKER_ADMIN')")`
- [x] T046 [US3] Ensure summaries reconcile with underlying `StatementService` / ledger data for the same dates and broker
- [x] T047 [US3] Add simulated-environment disclaimer and basic totals table to broker summary HTML/CSV template in `src/main/resources/templates/settlement/broker-summary.html`

### UI for User Story 3 — Broker “Settlements/Reports” tab

- [x] T048 [P] [US3] Implement Broker “Settlements/Reports” page listing per-day summaries in `src/main/webapp/app/modules/broker/settlements/index.tsx`
- [x] T049 [P] [US3] Implement frontend service for broker settlement APIs in `src/main/webapp/app/modules/broker/settlements/services/broker-settlements.service.ts`
- [x] T050 [US3] Wire drill-down from summary rows to underlying client statements (reuse Trader statements view where appropriate) in `src/main/webapp/app/modules/broker/settlements/index.tsx`
- [x] T051 [P] [US3] Jest tests for Broker settlements page and drill-down behavior in `src/test/javascript/spec/settlement/BrokerSettlements.spec.tsx`
- [x] T052 [P] [US3] Cypress E2E for Broker Admin viewing summaries and drilling into client statements in `src/test/javascript/cypress/e2e/broker_settlements.cy.ts`

**Checkpoint**: All three user stories (EOD run, trader statements, broker summaries) are independently functional and testable.

---

## Phase 6 — Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple stories and overall robustness.

- [x] T053 Ensure RBAC and negative-path integration tests cover unauthorized/forbidden access for all new endpoints in `src/test/java/com/rnexchange/integration/settlement/` and `.../broker/`
- [x] T054 Add additional logging and correlation IDs for settlement-related requests in `src/main/java/com/rnexchange/web/rest/settlement/` and `src/main/java/com/rnexchange/service/settlement/`
- [x] T055 [P] Add documentation references and EOD usage notes to `specs/005-settlement/quickstart.md`
- [x] T056 [P] Add a lightweight Gatling smoke test for EOD and settlements APIs in `src/test/gatling/simulations/SettlementSimulation.scala` (e.g., p95 latency and error rate checks)
- [x] T057 Review and refactor settlement code paths for clarity and duplication across `SettlementService`, `StatementService`, and `BrokerSettlementService`
- [x] T061 [P] Add short educational tooltips or helper text explaining simulated EOD and training context on the Exchange “Settlement” tab in `src/main/webapp/app/modules/exchange/settlement/index.tsx`
- [x] T062 [P] Add educational tooltips/disclaimers on Broker “Settlements/Reports” and Trader “Statements” screens to reinforce simulated environment context in `src/main/webapp/app/modules/broker/settlements/index.tsx` and `src/main/webapp/app/modules/trader/statements/index.tsx`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion — blocks all user stories.
- **User Stories (Phases 3–5)**: All depend on Foundational completion.
  - User stories can then proceed in parallel (if staffed) or sequentially in priority order (US1 → US2 → US3).
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Phase 2 — no dependency on other stories; provides core EOD batch.
- **User Story 2 (P2)**: Can start after Phase 2 — depends on EOD data being available but is independently testable via its own APIs and UI.
- **User Story 3 (P3)**: Can start after Phase 2 — depends on EOD data and, optionally, on statement infrastructure; should still be testable using its own summary endpoints.

### Within Each User Story

- Tests (contract, unit, integration, UI) MUST be written and fail before implementation tasks for that story.
- Domain and repository work precedes service implementation.
- Services are implemented before REST resources.
- REST resources are implemented before frontend wiring.
- Story is considered complete only when all tests pass.

---

## Parallel Execution Examples

- Backend vs frontend tasks in the same user story can run in parallel where marked `[P]`.
- In Phase 2, data model changes (T006–T008) must complete before repository and DTO tasks (T009–T012), but T009–T011 can run in parallel once the schema is settled.
- For **User Story 1**, contract/unit tests (T013–T015) can be authored in parallel; REST endpoints (T019–T020) and UI tasks (T023–T025) can proceed in parallel once `SettlementService` (T018) stabilizes.
- For **User Story 2**, frontend tasks (T037–T041) can run in parallel with backend tasks (T032–T035) after DTOs and contracts are set.
- For **User Story 3**, backend aggregation logic (T044–T047) and frontend page wiring (T048–T052) can proceed in parallel after foundational pieces are in place.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup).
2. Complete Phase 2 (Foundational).
3. Implement Phase 3 (User Story 1 — EOD run + Exchange view).
4. **Stop and validate**: Verify EOD batch, batch list, and reconciliation against positions/ledgers via tests.
5. Demo EOD behavior from the Exchange Operator’s perspective.

### Incremental Delivery

1. Deliver MVP (US1) as above.
2. Add User Story 2 (Trader statements), ensuring reconciliation and independent tests.
3. Add User Story 3 (Broker summaries), verifying aggregation vs underlying statements.
4. Apply Phase 6 polish tasks across all stories.

### Parallel Team Strategy

With multiple developers:

- After Setup + Foundational:
  - Developer A: User Story 1 (SettlementService, EOD endpoints, Exchange UI).
  - Developer B: User Story 2 (StatementService, trader APIs, statements UI).
  - Developer C: User Story 3 (BrokerSettlementService, broker summary API, broker UI).
- Team converges on Phase 6 for cross-cutting improvements and performance checks.
