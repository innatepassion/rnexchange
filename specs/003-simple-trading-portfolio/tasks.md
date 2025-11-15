# Tasks: Simple Trading & Portfolio (M2)

**Input**: Design documents from `/specs/003-simple-trading-portfolio/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Tests are required by the RNExchange constitution; this plan focuses them on the core trading loop while still following strict TDD (tests written before implementation) and expanding coverage as needed to meet the constitution’s backend and frontend coverage targets.

**Organization**: Tasks are grouped by user story so each story can be implemented and tested independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- All descriptions include an explicit file path or repo root (`.`).

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm branch and baseline build; no new infrastructure.

- [x] T001 Confirm branch `003-simple-trading-portfolio` is checked out and baseline build passes in `.`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Minimal domain and API contracts required before user stories.

**⚠️ CRITICAL**: No user story work should begin until this phase is complete.

- [x] T002 Define `Order`, `Execution`, `Position`, and `LedgerEntry` entities in `rnexchange.jdl`, and verify they align with the Key Entities section of `specs/003-simple-trading-portfolio/spec.md` and the detailed models in `specs/003-simple-trading-portfolio/data-model.md`
- [x] T003 Regenerate JHipster entities and Liquibase changelogs from updated JDL in `rnexchange.jdl` and `src/main/resources/config/liquibase/`, confirming schema matches the spec and `data-model.md`
- [x] T004 [P] Merge minimal order/portfolio endpoints from `specs/003-simple-trading-portfolio/contracts/orders-and-portfolio.openapi.yaml` into `src/main/resources/swagger/api.yml`
- [x] T005 [P] Run OpenAPI-driven code generation (`./mvnw generate-sources`) and verify new REST stubs exist in `src/main/java/com/rnexchange/web/rest/`
- [x] T006 Ensure repositories for new entities (order, execution, position, ledger) exist and are wired to JPA in `src/main/java/com/rnexchange/repository/`
- [x] T030 Ensure CASH `TradingAccount` and tradable `Instrument` seed and test data exist for this feature (or are created if missing) in Liquibase changelogs and test fixtures so that FR-001 is satisfied
- [x] T031 [P] Add contract tests for the new order and portfolio-related REST endpoints based on `src/main/resources/swagger/api.yml` in appropriate `*ResourceIT`/contract test classes under `src/test/java/com/rnexchange/web/rest/`

**Checkpoint**: Domain entities, DB schema, and basic API contracts ready; user story implementation can now begin.

---

## Phase 3: User Story 1 – Place a cash buy order and see updated portfolio (Priority: P1) 🎯 MVP

**Goal**: Trader can place a cash-funded BUY order that is immediately matched, creates a position, and updates cash and ledger; Trader UI shows the new order, position, and cash movement.

**Independent Test**: Follow `quickstart.md` to place a BUY order as a Trader and verify order, execution, position, and cash ledger updates without relying on SELL or Broker Admin features.

### Tests for User Story 1

- [ ] T007 [P] [US1] Add integration test covering successful BUY flow and key rejection cases (insufficient funds, inactive instrument, invalid quantity, non-marketable limit orders) in `src/test/java/com/rnexchange/web/rest/OrderResourceIT.java`, including assertions for FR-014 scope boundaries (e.g., rejecting margin/short-style requests)
- [ ] T008 [P] [US1] Add unit tests for BUY validation, average cost, and cash debit logic (including lot-size validation, FR-014 scope boundaries, and “Edge Cases” from `specs/003-simple-trading-portfolio/spec.md`) in `src/test/java/com/rnexchange/service/TradingServiceTest.java`

### Implementation for User Story 1

- [ ] T009 [P] [US1] Implement BUY-side validation and orchestration in `src/main/java/com/rnexchange/service/TradingService.java`
- [ ] T010 [P] [US1] Implement `MatchingService` to obtain latest mock price and decide immediate fill for Market/Limit BUY in `src/main/java/com/rnexchange/service/MatchingService.java`
- [ ] T011 [US1] Implement REST delegate for `POST /api/orders` to call `TradingService` and return `OrderResponse` in `src/main/java/com/rnexchange/web/rest/OrderResource.java`
- [ ] T012 [US1] Implement position update (create/update) and average-cost calculations for BUY executions in `src/main/java/com/rnexchange/service/TradingService.java`
- [ ] T013 [US1] Implement cash ledger debit and `TradingAccount.balance` update for BUY executions in `src/main/java/com/rnexchange/service/TradingService.java`
- [ ] T014 [US1] Publish WebSocket notifications on `/topic/orders/{tradingAccountId}` and `/topic/executions/{tradingAccountId}` after BUY fills in `src/main/java/com/rnexchange/service/TradingService.java`
- [ ] T015 [P] [US1] Add minimal order ticket drawer component to Market Watch that posts to `/api/orders` and shows success/failure toasts in `src/main/webapp/app/modules/market-watch/order-ticket-drawer.tsx`
- [ ] T016 [P] [US1] Add Trader “Orders & Trades” table (showing recent orders and executions for the current trading account) in `src/main/webapp/app/modules/trader/orders-trades.tsx`
- [ ] T017 [US1] Add Trader “Portfolio & Cash” view showing positions (qty/avg cost/last price/MTM) and recent ledger entries in `src/main/webapp/app/modules/trader/portfolio-cash.tsx`
- [ ] T018 [US1] Wire WebSocket subscriptions so orders and portfolio views refetch on `/topic/orders/{tradingAccountId}` and `/topic/executions/{tradingAccountId}` in `src/main/webapp/app/modules/trader/portfolio-cash.tsx` and `src/main/webapp/app/modules/trader/orders-trades.tsx`

**Checkpoint**: User Story 1 delivers an end-to-end BUY flow with UI and can be tested independently via `quickstart.md`.

---

## Phase 4: User Story 2 – Sell holdings and realize P&L (Priority: P2)

**Goal**: Trader can SELL existing positions, see realized P&L, and confirm the correct cash credit and updated quantities.

**Independent Test**: Using a Trader with an existing long position, place SELL orders and verify that positions, cash balance, and ledger entries change as expected without relying on Broker Admin views.

### Tests for User Story 2

- [ ] T019 [P] [US2] Extend unit tests to cover SELL quantity checks, realized P&L, and cash credit logic (including attempts to SELL beyond position, non-marketable limits, and FR-014 scope boundaries) in `src/test/java/com/rnexchange/service/TradingServiceTest.java`

### Implementation for User Story 2

- [ ] T020 [US2] Extend `TradingService` to handle SELL validation (sufficient position quantity) and realized P&L calculation in `src/main/java/com/rnexchange/service/TradingService.java`
- [ ] T021 [US2] Ensure SELL executions reduce or close positions and create credit `LedgerEntry` records in `src/main/java/com/rnexchange/service/TradingService.java`
- [ ] T022 [US2] Update Trader portfolio and orders views to display SELL executions and realized P&L where appropriate in `src/main/webapp/app/modules/trader/portfolio-cash.tsx` and `src/main/webapp/app/modules/trader/orders-trades.tsx`

**Checkpoint**: User Stories 1 and 2 together support round-trip trading with correct portfolio and cash behavior.

---

## Phase 5: User Story 3 – Broker Admin views trading activity and balances (Priority: P3)

**Goal**: Broker Admin can view orders, positions, and cash balances for Traders under their broker.

**Independent Test**: As a Broker Admin, load back-office views and confirm orders, positions, and balances match what sample Traders see for the same broker.

### Tests for User Story 3

- [ ] T023 [P] [US3] Add integration test ensuring Broker Admin only sees data for Traders under their broker in `src/test/java/com/rnexchange/web/rest/BrokerAdminPortfolioResourceIT.java`

### Implementation for User Story 3

- [ ] T024 [P] [US3] Add repository methods to filter orders, positions, and ledger entries by broker in `src/main/java/com/rnexchange/repository/`
- [ ] T025 [US3] Implement or extend Broker Admin REST resources to return broker-scoped orders, positions, and cash balances in `src/main/java/com/rnexchange/web/rest/`
- [ ] T026 [US3] Add simple back-office screens for Broker Admin to view orders, positions, and balances filtered by current broker in `src/main/webapp/app/modules/broker-admin/broker-portfolio.tsx`

**Checkpoint**: All three user stories are independently testable and align with the M2 “simple trading & portfolio” goal.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Small improvements that support all stories without adding new scope.

- [ ] T027 [P] Review and refine educational error messages for common order rejections (insufficient funds, inactive instrument, invalid quantity, non-marketable limit orders, and FR-014 scope violations) in `src/main/java/com/rnexchange/service/TradingService.java`
- [ ] T028 [P] Add a minimal Cypress E2E flow for BUY then SELL verifying UI portfolio and cash updates in `src/test/javascript/cypress/integration/trader-trading.e2e-spec.ts`, and capture basic timing metrics to ensure changes appear within SC-004 latency thresholds
- [ ] T029 Run through `specs/003-simple-trading-portfolio/quickstart.md` end-to-end and update any steps that no longer match behavior in `specs/003-simple-trading-portfolio/quickstart.md`, explicitly calling out the educational/learning objectives for each major step
- [ ] T032 [P] Add a lightweight performance test scenario (e.g., Gatling or Spring-based) to validate p95 order placement latency and WebSocket-driven UI updates against SC-004 and constitution performance targets in an appropriate `src/test` location
- [ ] T033 [P] Add clear “simulated environment” disclaimers and educational tooltips to key Trader and Broker Admin UI components (`orders-trades.tsx`, `portfolio-cash.tsx`, `broker-portfolio.tsx`) in `src/main/webapp/app/modules/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies – can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion – **blocks all user stories**.
- **User Stories (Phases 3–5)**: All depend on Phase 2 completion.
  - User Story 1 (P1) should be implemented first as the MVP.
  - User Stories 2 and 3 can start after their dependencies in Phase 3 are in place.
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Phase 2; no dependency on other stories.
- **User Story 2 (P2)**: Depends on core BUY flow and entities from User Story 1.
- **User Story 3 (P3)**: Depends on entities and basic flows from User Stories 1 and 2.

### Within Each User Story

- Tests for the story MUST be added before implementation (per constitution TDD rules) and must cover the core behaviors as well as key edge and scope-boundary cases from the spec.
- Domain logic (services) should be implemented before or alongside REST resources.
- REST resources should be in place before front-end components that call them.
- WebSocket notifications and UI wiring should come after basic CRUD and business logic are working.

### Parallel Opportunities

- Tasks marked **[P]** are safe to work on in parallel (different files, minimal coupling).
- Schema-changing tasks (JDL/Liquibase) and core domain invariant work (e.g., TradingService validation/matching) must complete before dependent parallel tasks, even if the files differ.
- After Phase 2, backend and frontend tasks within a user story can often progress in parallel:
  - For example, T009/T010 (services) and T015/T016 (frontend) for User Story 1.
- Different user stories can also run in parallel once their prerequisites are satisfied, if team capacity allows.

---

## Parallel Example: User Story 1

```bash
# Backend tests in parallel:
- T007 [P] [US1] OrderResource integration test
- T008 [P] [US1] TradingService unit tests

# Backend implementation in parallel (after tests exist):
- T009 [P] [US1] TradingService BUY logic
- T010 [P] [US1] MatchingService for price and fill

# Frontend work in parallel:
- T015 [P] [US1] Order ticket drawer
- T016 [P] [US1] Orders & Trades table
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup).
2. Complete Phase 2 (Foundational).
3. Complete Phase 3 (User Story 1 – BUY flow and portfolio views).
4. **Stop and validate**: Use `quickstart.md` to confirm the Trader can BUY once prices tick, see a position with sensible average cost and MTM, and see a matching cash ledger entry.

### Incremental Delivery

1. Deliver MVP (User Story 1).
2. Add User Story 2 to support SELL and realized P&L; validate round-trip trades.
3. Add User Story 3 for Broker Admin views; validate broker-level oversight.
4. Apply Phase 6 polish once core behaviors are stable.

### Parallel Team Strategy

With two developers (Developer A and Developer B), a recommended split that respects dependencies and `[P]` markers is:

1. **Phase 1–2 (Setup & Foundational)**
   - Developer A: Focus on domain/schema work — T002, T003, T006, T030.
   - Developer B: Focus on API and contract work — T004, T005, T031.
   - Either developer can run T001; both confirm the Phase 2 checkpoint together.
2. **Phase 3 (User Story 1 – BUY flow)**
   - Developer A: Backend tests and services — T007, T008, then T009–T014.
   - Developer B: Frontend components and wiring — T015–T018.
   - Both coordinate on WebSocket topic usage (`/topic/orders/{tradingAccountId}`, `/topic/executions/{tradingAccountId}`) and ensure tests exist before implementation work.
3. **Phase 4 (User Story 2 – SELL and P&L)**
   - Developer A: Backend SELL behavior and tests — T019–T021.
   - Developer B: UI updates to surface SELL executions and P&L — T022.
4. **Phase 5 (User Story 3 – Broker Admin views)**
   - Developer A: Broker-scoped backend and tests — T023–T025.
   - Developer B: Broker Admin UI — T026.
5. **Phase 6 (Polish & Cross-Cutting)**
   - Developer A: Backend polish and performance validation — T027 and T032.
   - Developer B: E2E Cypress flow and educational UX/docs — T028, T029, T033.
   - Both developers review results together and confirm that quickstart, performance, and educational transparency goals match the spec and constitution.

---

## Notes

- Tasks are deliberately minimal and avoid new infrastructure or patterns beyond existing JHipster conventions.
- [P] tasks always touch different files or layers and can be safely parallelized.
- [US1]/[US2]/[US3] labels map tasks to user stories for traceability.
- Each user story phase is independently testable and aligns with the success criteria in the feature spec.
