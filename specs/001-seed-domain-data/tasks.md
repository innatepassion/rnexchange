# Tasks: Seed Domain Trading Data Baseline

**Input**: Design documents from `/specs/001-seed-domain-data/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: TDD is mandated by the constitution; test tasks are listed ahead of implementation within each story phase and must execute (fail) before code changes.

**Organization**: Tasks are grouped by user story to keep increments independently testable. Complete Setup → Foundational before beginning story work.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency ordering conflicts)
- **[Story]**: Label appears only for user-story phases (e.g., [US1])
- Include exact file paths in every description

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Align Spring profiles with the new `baseline` Liquibase context and disable legacy faker seeding.

- [x] T000 Add regression test `BaselineProfileConfigIT` ensuring dev/prod/test profiles expose the `baseline` Liquibase context before configuration changes in `src/test/java/com/rnexchange/config/BaselineProfileConfigIT.java`
- [x] T000A [P] Add configuration smoke test `BaselineSeedBeanIT` asserting faker seed beans remain disabled before removal in `src/test/java/com/rnexchange/config/BaselineSeedBeanIT.java`
- [x] T001 Update Liquibase contexts to `baseline` in `src/main/resources/config/application-dev.yml`
- [x] T002 [P] Update Liquibase contexts to `baseline` in `src/main/resources/config/application-prod.yml`
- [x] T003 [P] Align embedded test Liquibase contexts to `test,baseline` in `src/test/resources/config/application.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core migration plumbing required before any user story begins.

**⚠️ CRITICAL**: Finish this phase before starting any user-story work.

- [x] T004 Update `src/main/resources/config/liquibase/master.xml` to include `config/liquibase/data/0001-seed-domain-data.xml` and remove `fake-data` context references
- [x] T005 [P] Add `BASELINE_LIQUIBASE_CONTEXT` constant to `src/main/java/com/rnexchange/config/Constants.java`
- [x] T006 [P] Remove faker seed bean wiring from `src/main/java/com/rnexchange/config/` to prevent legacy data resurrection

**Checkpoint**: Foundation ready — user stories can now begin (tests first).

---

## Phase 3: User Story 1 – Provision clean baseline (Priority: P1) 🎯 MVP

**Goal**: Reset the trading database and seed production-grade baseline data (exchanges, brokers, instruments, contracts, holidays, margin rules, user mappings) so all roles operate without demo clutter.

**Independent Test**: Start a fresh environment, verify the curated dataset loads exclusively, and confirm no generic demo records remain.

### Tests for User Story 1

- [x] T007 [P] [US1] Create integration test `BaselineSeedServiceIT` covering cleanup, deterministic seed values (including exact market holiday dates), rerun idempotency, and asserting exactly two distinct trader profiles with unique user mappings in `src/test/java/com/rnexchange/service/seed/BaselineSeedServiceIT.java`
- [x] T007A [P] [US1] Create integration test `BaselinePrerequisiteValidatorIT` ensuring prerequisite failures do not trigger truncation and surface descriptive errors, and that successful validation enables cleanup in `src/test/java/com/rnexchange/service/seed/BaselinePrerequisiteValidatorIT.java`
- [x] T008 [P] [US1] Create REST and security contract tests for baseline seed endpoints ensuring only `EXCHANGE_OPERATOR` receives 2xx and other roles receive 403 in `src/test/java/com/rnexchange/web/rest/BaselineSeedResourceIT.java`
- [x] T009 [P] [US1] Create integration test `BaselineAccessIT` validating `EXCHANGE_OPERATOR`, `BROKER_ADMIN`, trader logins and absence of demo data in `src/test/java/com/rnexchange/service/seed/BaselineAccessIT.java`
- [x] T010 [P] [US1] Add seeding duration metric test `BaselineSeedMetricsIT` ensuring completion under threshold in `src/test/java/com/rnexchange/service/seed/BaselineSeedMetricsIT.java`
- [x] T010A [P] [US1] Add verification gate integration test `BaselineSeedVerificationIT` that injects duplicate/partial seed scenarios, asserts checksum/invariant failures, and confirms database remains unchanged in `src/test/java/com/rnexchange/service/seed/BaselineSeedVerificationIT.java`
- [x] T011 [P] [US1] Create negative-path integration test `BaselineSeedFailureIT` verifying missing or inactive prerequisites surface descriptive errors without partial data in `src/test/java/com/rnexchange/service/seed/BaselineSeedFailureIT.java`
- [x] T036 [P] [US1] Create structured logging integration test `BaselineSeedLoggingIT` asserting cleanup/seeding/validation JSON payloads include phase, entityType, status, durationMs, failureReason, actorId, actorRole, instrument, and outcome fields, covering both success and a forced duplicate-instrument failure path, in `src/test/java/com/rnexchange/service/seed/BaselineSeedLoggingIT.java`

### Implementation for User Story 1

- [x] T012 [P] [US1] Update OpenAPI definition in `src/main/resources/swagger/api.yml` using `specs/001-seed-domain-data/contracts/baseline-seed.openapi.yaml` and run `./mvnw generate-sources`
- [x] T012A [US1] Implement `BaselinePrerequisiteValidator` service invoked at startup to confirm RBAC users, timezones, and Liquibase contexts before cleanup in `src/main/java/com/rnexchange/service/seed/BaselinePrerequisiteValidator.java`
- [x] T013 [P] [US1] Implement `BaselineSeedCleanupRunner` to trigger truncation only after `BaselinePrerequisiteValidator` succeeds in `src/main/java/com/rnexchange/config/BaselineSeedCleanupRunner.java`
- [x] T014 [P] [US1] Implement `BaselineTruncateService` handling non-system table truncation in `src/main/java/com/rnexchange/service/seed/BaselineTruncateService.java`
- [x] T015 [US1] Implement `BaselineSeedService` orchestrating Liquibase reruns and job lifecycle in `src/main/java/com/rnexchange/service/seed/BaselineSeedService.java`
- [x] T016 [P] [US1] Add job response DTOs in `src/main/java/com/rnexchange/service/dto/BaselineSeedJobDTO.java`
- [x] T017 [US1] Implement in-memory job registry/tracker in `src/main/java/com/rnexchange/service/seed/BaselineSeedJobRegistry.java`
- [x] T018 [US1] Implement `BaselineSeedResource` exposing `/api/admin/baseline-seed/*` with `@PreAuthorize("hasAuthority('EXCHANGE_OPERATOR')")` enforcement in `src/main/java/com/rnexchange/web/rest/BaselineSeedResource.java`
- [x] T019 [P] [US1] Author Liquibase changelog `0001-seed-domain-data.xml` with exchange/broker/instrument/contract/holiday/margin/trader seeds in `src/main/resources/config/liquibase/data/0001-seed-domain-data.xml`
- [x] T020 [US1] Implement startup validation runner ensuring `EXCHANGE_OPERATOR`, `BROKER_ADMIN`, and `TRADER` mappings, verifying each role can access the seeded dataset, and failing if any legacy demo entities are detected in `src/main/java/com/rnexchange/service/startup/BaselineValidationRunner.java`
- [x] T021 [P] [US1] Instrument `BaselineSeedService` with Micrometer duration metrics and ensure they surface via `BaselineSeedMetricsIT` in `src/main/java/com/rnexchange/service/seed/BaselineSeedService.java`
- [x] T021A [US1] Implement verification gate logic in `src/main/java/com/rnexchange/service/seed/BaselineSeedService.java` (checksums/invariants with descriptive failures) and wire safeguarding helpers in `src/main/java/com/rnexchange/service/seed/BaselineSeedJobRegistry.java`
- [x] T037 [US1] Emit structured JSON logs including phase, entityType, status, durationMs, failureReason, actorId, actorRole, instrument, and outcome across cleanup, seeding, and validation flows, emitting the duplicate-instrument failure telemetry exercised in `T036`, in `src/main/java/com/rnexchange/config/BaselineSeedCleanupRunner.java`, `src/main/java/com/rnexchange/service/seed/BaselineSeedService.java`, and `src/main/java/com/rnexchange/service/startup/BaselineValidationRunner.java`

**Checkpoint**: User Story 1 delivers MVP baseline and passes all listed tests independently.

---

## Phase 4: User Story 2 – Broker workstation readiness (Priority: P2)

**Goal**: `BROKER_ADMIN` user logs in and sees RN DEMO BROKING with exchange memberships and instrument catalog populated from the new baseline.

**Independent Test**: `BROKER_ADMIN` user signs in, opens the broker workspace, and confirms broker profile plus instrument metadata reflect the seeded dataset.

### Tests for User Story 2

- [ ] T022 [P] [US2] Add Cypress spec `broker-seed.cy.ts` verifying broker desk view, capturing login-to-ready duration, persisting the metric artifact, and asserting ≤60 s readiness in `src/test/javascript/cypress/e2e/broker/broker-seed.cy.ts`
- [ ] T023 [P] [US2] Add Spring MVC test `BrokerResourceIT` covering enriched broker DTO in `src/test/java/com/rnexchange/web/rest/BrokerResourceIT.java`

### Implementation for User Story 2

- [ ] T024 [P] [US2] Extend broker repository query to eager load exchange + instrument relations in `src/main/java/com/rnexchange/repository/BrokerRepository.java`
- [ ] T025 [US2] Expose enriched broker DTO via `src/main/java/com/rnexchange/web/rest/BrokerResource.java`
- [ ] T026 [US2] Render seeded exchange membership & instrument metadata in `src/main/webapp/app/entities/broker/broker-detail.tsx`
- [ ] T027 [US2] Add display strings for broker baseline data in `src/main/webapp/i18n/en/broker.json`

**Checkpoint**: Broker workstation ready, independent from trader flow and maintaining US1 integrity.

---

## Phase 5: User Story 3 – Trader order simulation (Priority: P3)

**Goal**: Seeded trader places a simulated order on a seeded instrument with margin checks powered by the new baseline dataset.

**Independent Test**: Trader profile logs in, sees funded trading account, and submits a buy order for RELIANCE on NSE—order is accepted with seeded margin rules.

### Tests for User Story 3

- [ ] T028 [P] [US3] Add Cucumber scenario `baseline_seed.feature` for trader order flow in `src/test/resources/com/rnexchange/cucumber/baseline_seed.feature`
- [ ] T029 [P] [US3] Add margin rule integration test in `src/test/java/com/rnexchange/service/MarginServiceIT.java`
- [ ] T030 [P] [US3] Add Cypress spec `trader-seed.cy.ts` validating trader order path across three consecutive runs in `src/test/javascript/cypress/e2e/trader/trader-seed.cy.ts`
- [ ] T038 [P] [US3] Add trader audit logging integration test `TraderAuditLogIT` ensuring structured audit entries emit actor, role, instrument, and outcome in `src/test/java/com/rnexchange/service/TraderAuditLogIT.java`
- [ ] T040 [US3] Extend Gatling order latency scenario in `src/test/java/gatling/simulations/OrderGatlingTest.java` to assert `<250 ms p95` after seeding and make it a blocking exit check for User Story 3

### Implementation for User Story 3

- [ ] T031 [US3] Load seeded `MarginRule` data inside `src/main/java/com/rnexchange/service/MarginService.java`
- [ ] T032 [US3] Ensure `OrderService` enforces margin outcomes using seeded accounts in `src/main/java/com/rnexchange/service/OrderService.java`
- [ ] T033 [US3] Default order ticket to seeded instruments & respect tick/lot sizes in `src/main/webapp/app/entities/order/order-update.tsx`
- [ ] T039 [US3] Emit structured audit logs for trader order submissions in `src/main/java/com/rnexchange/service/OrderService.java` and ensure they persist via existing audit infrastructure

**Checkpoint**: Trader journey demonstrates seeded trading experience without regressing US1 or US2 outcomes.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Documentation and hardening tasks impacting multiple stories, reinforcing Educational Transparency and success criteria.

- [ ] T034 [P] Document `EXCHANGE_OPERATOR`/`BROKER_ADMIN`/`TRADER` validation steps in `specs/001-seed-domain-data/quickstart.md`
- [ ] T035 Summarize baseline seeding rollout and verification checklist in `README.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** → prerequisite for Foundational.
- **Foundational (Phase 2)** → prerequisite for all user stories.
- **User Story Phases (3–5)** → execute sequentially by priority (P1 → P2 → P3) or in parallel once shared prerequisites complete and dependent data exists.
- **Polish (Phase 6)** → after targeted stories finish.

### Story Dependencies

- **US1 (P1)**: Provides seed dataset and API contracts consumed by later stories.
- **US2 (P2)**: Depends on seeded data from US1 but is otherwise independent.
- **US3 (P3)**: Depends on US1 seed data and margin rules; does not require US2 UI work.

### Within Each User Story

- Tests (e.g., T007–T011, T022–T023, T028–T030, T036, T038, T040) precede implementation per constitution Principle I.
- Service/domain logic lands before REST/UI layers where applicable.
- DTO/resource changes stay aligned with OpenAPI contract.
- Complete all tasks in a story before progressing to the next priority to preserve independent increments.

### Parallel Opportunities

- Setup tasks T002 and T003 parallelize once context decisions finalize.
- Foundational updates T005 and T006 can run concurrently after T004 merges.
- US1 offers parallelism between test suite creation (T007–T011, T036) and discrete implementations (T013, T014, T016, T019, T021, T037).
- US2 Cypress test (T022) can begin while repository/UI enhancements (T024–T026) progress; contract test T023 runs in parallel with UI work.
- US3 tests (T028–T030, T038) and service/UI updates (T031–T033, T039) can execute concurrently once seed data is stable.
- Phase 6 performance verification (T040) can run in parallel after core order flow stabilises.

---

## Implementation Strategy

### MVP First (User Story 1)

1. Complete Phase 1 (Setup) and Phase 2 (Foundational).
2. Deliver Phase 3 (US1) with passing tests and deterministic seed data.
3. Validate via integration, REST, and quickstart smoke checks.
4. Ship MVP baseline for stakeholder review before expanding scope.

### Incremental Delivery

1. Finish Setup + Foundational → baseline infrastructure ready.
2. Add User Story 1 → verify independently → optional deploy/demo.
3. Add User Story 2 → verify broker flow → optional deploy/demo.
4. Add User Story 3 → verify trader flow → optional deploy/demo.
5. Execute Phase 6 polish items to finalize documentation and rollout notes.

### Parallel Team Strategy

- **Developer A**: Leads US1 backend services (T012–T021) after tests fail.
- **Developer B**: Owns US2 repository/UI adjustments (T024–T027) once US1 seeds exist.
- **Developer C**: Drives US3 margin + trader order updates (T028–T033) contingent on seeded data.
- **Shared**: Tests, Cypress specs, and documentation tasks marked [P] (e.g., T007–T011, T022–T023, T028–T030, T034).
