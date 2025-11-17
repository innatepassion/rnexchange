## Tasks — M6 QA Hardening, Branding & Demo-Ready Polish

**Input**: Design documents from `/specs/006-qa-hardening-demo/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `quickstart.md`, `contracts/m6-qa-hardening.openapi.yaml`

Tasks are grouped by user story so each story can be implemented and tested independently. TDD is assumed: write or extend tests before implementation for each story, with API-first workflow for any endpoint changes.

---

## Phase 1 — Setup (Shared Infrastructure for M6)

**Purpose**: Align OpenAPI contracts, generators, and baseline test tooling to support M6 QA hardening work.

- [ ] T001 Ensure M6 contracts from `specs/006-qa-hardening-demo/contracts/m6-qa-hardening.openapi.yaml` are merged into `src/main/resources/swagger/api.yml` and consistent with existing settlement, statements, and ledger paths
- [ ] T002 Run OpenAPI code generation for updated API spec via `./mvnw generate-sources` and verify updated delegate interfaces under `src/main/java/com/rnexchange/web/rest/` and DTOs under `src/main/java/com/rnexchange/service/dto/`
- [ ] T003 [P] Verify Cypress, Jest, and Gatling scripts for M6 flows are present and wired in `package.json` and `src/test/javascript/cypress.config.ts`, adjusting commands (e.g., `npm run e2e:headless`, `./mvnw -ntp gatling:test -Pperformance`) as documented in `specs/006-qa-hardening-demo/quickstart.md`
- [ ] T004 [P] Document any additional M6-specific setup steps (e.g., environment variables for demo users) in `specs/006-qa-hardening-demo/quickstart.md` so that the QA and demo flows are reproducible from a clean repo clone

---

## Phase 2 — Foundational (Blocking Prerequisites)

**Purpose**: Core seeds, RBAC conventions, and shared infrastructure needed before user story work.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

### Foundational — Demo Seeds & Baseline Data

- [ ] T005 Create or extend Liquibase changelog to define fixed demo users (`trader_demo`, `broker_demo`, `exchange_demo`) and starting balances/positions in `src/main/resources/config/liquibase/changelog/` and ensure it is enabled for the `baseline` context in `src/main/resources/config/application-dev.yml`
- [ ] T006 [P] Add or update baseline seed script/utilities so Cypress and Gatling can reset demo accounts to a known state before tests (e.g., `src/main/java/com/rnexchange/config/DemoDataInitializer.java` and related `src/test/resources` SQL/JSON fixtures)

### Foundational — RBAC & Navigation Conventions

- [ ] T007 Ensure authority constants for `TRADER`, `BROKER_ADMIN`, and `EXCHANGE_OPERATOR` are defined and documented in `src/main/java/com/rnexchange/security/AuthoritiesConstants.java` and used consistently by new controllers and services
- [ ] T008 [P] Centralise per-role default landing route resolution in a dedicated helper (e.g., `src/main/webapp/app/shared/auth/role-landing-resolver.ts`) to map roles → routes (`/market-watch`, `/broker/dashboard`, `/exchange/overview`)
- [ ] T009 [P] Define or update a single source of truth for role-aware menu entries in `src/main/webapp/app/shared/layout/menus.tsx` (or equivalent) so that M6 changes to navigation are driven from one configuration module

### Foundational — QA & Test Harness

- [ ] T010 Add or update Cypress login helpers to support demo users and role-specific assertions in `src/test/javascript/cypress/support/commands.ts`
- [ ] T011 [P] Ensure existing Gatling simulations and configuration file paths (e.g., `src/test/gatling/simulations/SettlementSimulation.scala` and Maven `pom.xml` profiles) are ready to be extended for M6 performance scenarios

**Checkpoint**: Foundational seeds, RBAC conventions, and QA harness are ready — user story implementation can now begin in parallel.

---

## Phase 3 — User Story 1 (P1): Trader day trade flow is reliable 🎯 MVP

**Goal**: A trader can log in, curate a watchlist, place a market order in a common symbol, see it fill, and immediately see both positions and cash ledger updated predictably.

**Independent Test**: Using a single trader demo account, run login → watchlist update → order placement → fill → position and ledger verification entirely from the UI, without broker or exchange operator actions, and confirm all views reconcile with each other.

### Tests for User Story 1 (write or extend first)

- [ ] T012 [P] [US1] Extend Cypress trader E2E flow to cover watchlist curation, market order placement, and position/ledger reconciliation in `src/test/javascript/cypress/e2e/trader/trader-trading.cy.ts`
- [ ] T013 [P] [US1] Add or extend backend integration test verifying order → execution → position → ledger consistency for a demo trader in `src/test/java/com/rnexchange/integration/trading/TraderDayTradeIT.java`
- [ ] T014 [P] [US1] Add Jest/React Testing Library tests for Market Watch and portfolio components to assert correct rendering and updates of positions and balances in `src/test/javascript/spec/trader/MarketWatch.spec.tsx` and `src/test/javascript/spec/trader/Portfolio.spec.tsx`

### Implementation for User Story 1

- [ ] T015 [US1] Harden order placement and execution flow to guarantee immediate position/ledger updates (e.g., `src/main/java/com/rnexchange/web/rest/trading/OrderResource.java` and `src/main/java/com/rnexchange/service/trading/PositionService.java`)
- [ ] T016 [P] [US1] Ensure watchlist add/remove actions are low-friction and durable in `src/main/webapp/app/modules/trader/market-watch/MarketWatchPage.tsx` and related store/service modules under `src/main/webapp/app/modules/trader/market-watch/`
- [ ] T017 [P] [US1] Ensure the trader ledger/cash view renders entries with clear labels and running balances for trade debits/credits in `src/main/webapp/app/modules/trader/ledger/TraderLedgerPage.tsx`

### Real-Time Integrity for User Story 1

- [ ] T018 [US1] Verify and, if needed, adjust WebSocket topics and subscriptions for order status and portfolio updates in `src/main/java/com/rnexchange/web/websocket` and `src/main/webapp/app/modules/trader/portfolio/PortfolioPage.tsx` to ensure updates under load
- [ ] T019 [P] [US1] Add Cypress assertions for WebSocket-driven updates (e.g., order status and portfolio tiles) in `src/test/javascript/cypress/e2e/trader/trader-trading.cy.ts` to confirm timely UI updates after order placement
- [ ] T020 [P] [US1] Add logging and basic error banners for WebSocket disconnects/retries in `src/main/webapp/app/shared/websocket/WebsocketConnectionBanner.tsx`

### UX Polish for User Story 1

- [ ] T021 [US1] Ensure all primary trader views used in the day trade flow (Market Watch, order entry, portfolio, ledger) include a persistent “SIMULATED / NOT REAL MONEY” banner component in `src/main/webapp/app/modules/trader/layout/TraderShell.tsx`

---

## Phase 4 — User Story 2 (P2): Broker adjusts trader funds safely

**Goal**: A broker admin can credit or debit funds via a funds journal entry and immediately see the trader’s buying power and ledger updated, with negative or at-risk states clearly flagged.

**Independent Test**: Using a broker demo account and a single trader demo account, create credit and debit journal entries and verify buying power and ledger entries update correctly, including clear flags when balances go negative—all without placing new trades or involving EOD.

### Tests for User Story 2 (write or extend first)

- [ ] T022 [P] [US2] Add contract test for `POST /api/ledger-entries` to validate payload, status codes, and response shape in `src/test/java/com/rnexchange/contract/ledger/CreateLedgerEntryContractTest.java`
- [ ] T023 [P] [US2] Add backend integration test verifying that broker-created journal entries adjust balances (including negative) and persist correctly in `src/test/java/com/rnexchange/integration/ledger/BrokerFundsJournalIT.java`
- [ ] T024 [P] [US2] Extend Cypress broker flow to cover funds credit/debit entry and UI flagging of negative/at-risk accounts in `src/test/javascript/cypress/e2e/broker/broker_journal.cy.ts`

### Implementation for User Story 2

- [ ] T025 [US2] Implement or update controller/service handling `POST /api/ledger-entries` to allow negative balances while enforcing field validation in `src/main/java/com/rnexchange/web/rest/ledger/LedgerEntryResource.java` and `src/main/java/com/rnexchange/service/ledger/LedgerEntryService.java`
- [ ] T026 [P] [US2] Ensure buying power calculation logic incorporates funds journal entries and is reused consistently in `src/main/java/com/rnexchange/service/trading/BuyingPowerService.java`
- [ ] T027 [P] [US2] Implement broker funds journal UI for creating credits/debits and viewing recent entries in `src/main/webapp/app/modules/broker/journal/BrokerJournalPage.tsx`

### Negative/At-Risk Flagging

- [ ] T028 [US2] Add domain-level detection of negative or at-risk balances and expose flags in DTOs returned from broker/trader account APIs in `src/main/java/com/rnexchange/service/dto/AccountSummaryDTO.java` and related mappers/services under `src/main/java/com/rnexchange/service/account/`
- [ ] T029 [P] [US2] Render negative/at-risk flags in broker and trader UIs (e.g., badges or banners) in `src/main/webapp/app/modules/broker/dashboard/BrokerDashboardPage.tsx` and `src/main/webapp/app/modules/trader/portfolio/PortfolioPage.tsx`

---

## Phase 5 — User Story 3 (P3): Exchange operator runs EOD and statements

**Goal**: An exchange operator can run EOD for a trading day, and traders/brokers can open generated daily statements showing consistent balances, P&L, and cash movements.

**Independent Test**: Run a full trading day with at least one trader and broker, execute EOD for the date, then verify that statements for traders and brokers reconcile with ledger and position data; rerun EOD for the same date and confirm idempotent behaviour (no double-counting, statements overwritten).

### Tests for User Story 3 (write or extend first)

- [ ] T030 [P] [US3] Add contract tests for `POST /api/settlements/eod` and `GET /api/settlements` based on M6 contracts in `src/test/java/com/rnexchange/contract/settlement/RunEodAndListBatchesContractTest.java`
- [ ] T031 [P] [US3] Add contract tests for `GET /api/statements` and `GET /api/statements/{statementId}/html` to validate statement summary and HTML responses in `src/test/java/com/rnexchange/contract/settlement/StatementsContractTest.java`
- [ ] T032 [US3] Add integration test verifying EOD idempotency (reruns overwrite MTM adjustments and statements without duplication) in `src/test/java/com/rnexchange/integration/settlement/EodIdempotencyIT.java`
- [ ] T033 [P] [US3] Extend Cypress E2E flow for EOD and statements to assert reconciliation across positions, ledger, and statements in `src/test/javascript/cypress/e2e/settlement/settlement_eod.cy.ts` and `src/test/javascript/cypress/e2e/trader/trader_statements.cy.ts`

### Implementation for User Story 3

- [ ] T034 [US3] Ensure `runEod` implementation is idempotent per `(refDate)` by recomputing and overwriting EOD MTM entries and statements in `src/main/java/com/rnexchange/service/settlement/SettlementService.java`
- [ ] T035 [P] [US3] Ensure EOD service emits or updates `SettlementBatch` metrics (accountsProcessed, positionsProcessed, netPnl) and surfaces them via `src/main/java/com/rnexchange/web/rest/settlement/SettlementResource.java`
- [ ] T036 [P] [US3] Ensure statement generation covers no-activity days and negative/at-risk accounts with clear messaging in `src/main/java/com/rnexchange/service/settlement/StatementService.java` and `src/main/resources/templates/settlement/statement.html`

### UI for User Story 3 — Exchange Overview & Statements

- [ ] T037 [US3] Implement or refine an Exchange Overview / EOD console showing EOD batches and key metrics in `src/main/webapp/app/modules/exchange/overview/ExchangeOverviewPage.tsx`
- [ ] T038 [P] [US3] Ensure trader and broker statement screens allow opening generated HTML statements and highlight reconciliation status in `src/main/webapp/app/modules/trader/statements/TraderStatementsPage.tsx` and `src/main/webapp/app/modules/broker/settlements/BrokerSettlementsPage.tsx`
- [ ] T039 [P] [US3] Add Cypress coverage for traders and brokers opening daily statements after EOD in `src/test/javascript/cypress/e2e/trader/trader_statements.cy.ts` and `src/test/javascript/cypress/e2e/broker/broker_settlements.cy.ts`

---

## Phase 6 — User Story 4 (P2): Demo-ready UX, branding, and role-based navigation

**Goal**: A team member can start from the RNExchange-branded landing page and perform a full “day in the life” demo across all roles using only the UI, with role-tailored navigation and clear simulator disclaimers.

**Independent Test**: From a fresh environment, load the app root to see RNExchange branding (logo and name), then log in as `trader_demo`, `broker_demo`, and `exchange_demo` and confirm each lands on a role-appropriate dashboard with relevant navigation only, visible simulator banners, and no generic JHipster pages or confusing dead ends.

### Tests for User Story 4 (write or extend first)

- [ ] T040 [P] [US4] Add Cypress tests for RNExchange-branded landing page and role-specific default landings in `src/test/javascript/cypress/e2e/core/landing_and_login.cy.ts`
- [ ] T041 [P] [US4] Add Jest/React tests to assert that navigation menus render role-appropriate items and hide generic JHipster links in `src/test/javascript/spec/layout/RoleBasedMenu.spec.tsx`

### Implementation for User Story 4 — Landing Page & Branding

- [ ] T042 [US4] Replace the default JHipster home page with an RNExchange-branded landing page (logo, name, description, role CTAs) in `src/main/webapp/app/modules/home/HomePage.tsx` and associated styles/assets under `src/main/webapp/content/images/`
- [ ] T043 [P] [US4] Ensure the RNExchange logo asset meets aspect ratio and resolution requirements and is referenced consistently in `src/main/webapp/content/images/rnexchange-logo.png` and `src/main/webapp/app/shared/layout/header/Header.tsx`

### Implementation for User Story 4 — Role-Based Navigation

- [ ] T044 [US4] Implement role-based default routing after login using the landing resolver in `src/main/webapp/app/routes.tsx` and `src/main/webapp/app/shared/auth/role-landing-resolver.ts`
- [ ] T045 [P] [US4] Update main navigation menus to hide generic JHipster sections (Entities, Administration, Performance) for Trader and Broker Admin roles in `src/main/webapp/app/shared/layout/menus.tsx`
- [ ] T046 [P] [US4] Ensure Exchange Operator sees only relevant administrative items and an Exchange Overview entry in `src/main/webapp/app/shared/layout/menus.tsx`

### UX Polish for User Story 4

- [ ] T047 [US4] Add or refine loading and empty states for key demo screens (landing, Market Watch, broker dashboard, exchange overview) in `src/main/webapp/app/modules/trader/market-watch/MarketWatchPage.tsx`, `src/main/webapp/app/modules/broker/dashboard/BrokerDashboardPage.tsx`, and `src/main/webapp/app/modules/exchange/overview/ExchangeOverviewPage.tsx`
- [ ] T048 [P] [US4] Address at least five high-friction UX paper cuts (unclear errors, missing messages, ambiguous labels) discovered during dry-run demos and document them in `specs/006-qa-hardening-demo/research.md`
- [ ] T049 [P] [US4] Ensure all primary Trader and Broker views include a visible “SIMULATED / NOT REAL MONEY” banner component shared from `src/main/webapp/app/shared/components/SimulatedBanner.tsx`

---

## Phase 7 — User Story 5 (P3): Per-role “How to use RNExchange” help guides

**Goal**: Each role sees an integrated “How to use RNExchange” help section from their main dashboard, acting as a lightweight user manual.

**Independent Test**: Log in as each role (Trader, Broker Admin, Exchange Operator) and access a clearly labeled help section that explains core concepts and flows for that role, in plain language, without needing external documentation.

### Tests for User Story 5 (write or extend first)

- [ ] T050 [P] [US5] Add Jest/React tests verifying that the help panel component renders the correct role-specific content and links in `src/test/javascript/spec/help/RoleHelpPanel.spec.tsx`
- [ ] T051 [P] [US5] Add Cypress coverage to ensure each role can discover and open their “How to use RNExchange” help from the dashboard in `src/test/javascript/cypress/e2e/core/role_help.cy.ts`

### Implementation for User Story 5 — Help Content & Components

- [ ] T052 [US5] Implement a shared role-aware help panel component in `src/main/webapp/app/shared/components/RoleHelpPanel.tsx` that reads content from i18n JSON
- [ ] T053 [P] [US5] Create role-specific help content files describing key concepts and flows in `src/main/webapp/i18n/en/trader-help.json`, `src/main/webapp/i18n/en/broker-help.json`, and `src/main/webapp/i18n/en/exchange-help.json`
- [ ] T054 [P] [US5] Integrate the help panel into the trader, broker, and exchange dashboards in `src/main/webapp/app/modules/trader/dashboard/TraderDashboardPage.tsx`, `src/main/webapp/app/modules/broker/dashboard/BrokerDashboardPage.tsx`, and `src/main/webapp/app/modules/exchange/overview/ExchangeOverviewPage.tsx`
- [ ] T055 [US5] Ensure help content references demo users and flows documented in `specs/006-qa-hardening-demo/quickstart.md` so that written guidance and automated tests stay aligned

---

## Phase 8 — Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple stories and overall robustness, including RBAC, logging, performance, and documentation.

- [ ] T056 Add or extend negative-path integration tests for unauthorized/forbidden access across new or modified endpoints in `src/test/java/com/rnexchange/integration/security/RbacNegativePathIT.java`
- [ ] T057 [P] Add structured logging and correlation IDs for key M6 flows (trader trade, broker journal, EOD/statement runs) in `src/main/java/com/rnexchange/web/rest/` and `src/main/java/com/rnexchange/service/`
- [ ] T058 [P] Extend Gatling performance simulations to cover trader trade flow, broker funds journal, and EOD/statement runs with M6 load targets in `src/test/gatling/simulations/BrokerBackofficeSimulation.scala` and `src/test/gatling/simulations/SettlementSimulation.scala`
- [ ] T059 [P] Add or refine Jest and Cypress tests to reduce flakiness (e.g., better waits, selectors, and test data isolation) in `src/test/javascript/spec/` and `src/test/javascript/cypress/e2e/`
- [ ] T060 Review and refactor cross-cutting domain logic for trades, ledger, and statements for clarity and duplication in `src/main/java/com/rnexchange/service/trading/`, `src/main/java/com/rnexchange/service/ledger/`, and `src/main/java/com/rnexchange/service/settlement/`
- [ ] T061 [P] Update `specs/006-qa-hardening-demo/quickstart.md` with final demo script steps that reflect the implemented UI and automated tests
- [ ] T062 [P] Cross-check `specs/006-qa-hardening-demo/plan.md`, `specs/006-qa-hardening-demo/research.md`, and `specs/006-qa-hardening-demo/data-model.md` against the implemented code and tests, updating any outdated assumptions or diagrams

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion — blocks all user stories.
- **User Stories (Phases 3–7)**: All depend on Foundational completion.
  - User stories can then proceed in parallel (if staffed) or sequentially in priority order (US1 → US4 → US2 → US3 → US5) based on P1/P2/P3 priorities.
- **Polish (Phase 8)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Phase 2 — no dependency on other stories; provides the core trader day trade flow and real-time integrity checks.
- **User Story 2 (P2)**: Can start after Phase 2 — depends on basic trading and ledger plumbing but is independently testable via its own journal endpoints and broker/trader UIs.
- **User Story 3 (P3)**: Can start after Phase 2 — builds on existing settlement/statement services; relies on trading and journal data but is testable via EOD and statement-specific APIs and UIs.
- **User Story 4 (P2)**: Can start after Phase 2 — primarily front-end work but should coordinate with seeds and demo users to ensure navigation and branding align with demo scripts.
- **User Story 5 (P3)**: Can start after Phase 2 — depends on role-based navigation patterns from User Story 4 but is otherwise independent.

### Within Each User Story

- Tests (contract, integration, UI, E2E) MUST be written and fail before implementation tasks for that story.
- Domain and repository/service work precedes REST resource changes.
- REST resources are implemented before frontend wiring.
- Story is considered complete only when all tests pass and independent test criteria from `specs/006-qa-hardening-demo/spec.md` are satisfied.

---

## Parallel Execution Examples

- Backend vs frontend tasks within the same user story can run in parallel where marked `[P]` (e.g., trader UI tests and backend integration tests).
- In **Phase 2**, demo seed tasks (T005–T006) must complete before relying on demo users in Cypress or Gatling, but RBAC/navigation helpers (T007–T009) and QA harness tasks (T010–T011) can proceed in parallel.
- For **User Story 1**, Cypress, integration, and Jest tests (T012–T014) can be authored in parallel; WebSocket integrity and banner tasks (T018–T021) can run alongside domain hardening (T015–T017) once basic order flow assumptions are stable.
- For **User Story 2**, backend journal logic (T025–T026) and UI work (T027–T029) can proceed in parallel after contract and integration tests (T022–T024) are outlined.
- For **User Story 3**, EOD idempotency and metrics (T034–T036) can be developed in parallel with Exchange Overview and statement UIs (T037–T039) after contract tests (T030–T031) are in place.
- For **User Story 4**, landing page branding (T042–T043) and menu/landing routing (T044–T046) can run in parallel once the role landing resolver (T008) exists.
- For **User Story 5**, help panel component implementation (T052–T054) and test coverage (T050–T051) can be developed concurrently.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup).
2. Complete Phase 2 (Foundational).
3. Implement Phase 3 (User Story 1 — trader day trade flow, including real-time updates and simulator banners).
4. **Stop and validate**: Verify the trader day trade flow end-to-end via Cypress and integration tests and ensure WebSocket updates behave under load.
5. Demo the trader-centric flow from the perspective of `trader_demo`.

### Incremental Delivery

1. Deliver MVP (US1) as above.
2. Add User Story 4 (demo-ready branding and role-based navigation) so the application entry and landing experiences match demo expectations.
3. Add User Story 2 (broker funds journals) and User Story 3 (EOD and statements), ensuring reconciliation and independent tests for each.
4. Add User Story 5 (per-role help guides) to complete the in-app documentation experience.
5. Apply Phase 8 polish tasks for RBAC negative paths, logging, performance, and documentation alignment.

### Parallel Team Strategy

With multiple developers:

- After Setup + Foundational:
  - Developer A: User Story 1 (trader trading flow reliability, real-time verification, trader UI polish).
  - Developer B: User Story 2 (broker funds journals, negative/at-risk flags) and User Story 5 (broker help content).
  - Developer C: User Story 3 (EOD idempotency, statements) and User Story 4 (landing page, navigation, banners).
- Team converges on Phase 8 for cross-cutting improvements, performance tuning, and final spec/plan/quickstart reconciliation.
