# Implementation Plan: M6 QA Hardening, Branding & Demo-Ready Polish

**Branch**: `006-qa-hardening-demo` | **Date**: 2025-11-17 | **Spec**: `/specs/006-qa-hardening-demo/spec.md`
**Input**: Feature specification from `/specs/006-qa-hardening-demo/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

This plan implements M6 “QA Hardening & Demo-Ready Polish” for RNExchange: stabilising the core trader trade/ledger flow, broker funds journal flow, and exchange EOD/statement flow; validating them with end-to-end automation and performance tests; and delivering an RNExchange-branded landing experience, role-tailored navigation, and per-role in-app help.
Implementation will follow the RNExchange constitution: API-first (OpenAPI-driven) and TDD across backend (contract, integration, unit), frontend (React tests, Cypress), and performance (Gatling), while preserving JHipster conventions, strict RBAC, and educational transparency (clear simulator disclaimers, helpful errors, loading and empty states).

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.4.5, JHipster 8.11.0) backend; TypeScript/React 18.3 (React-JHipster) frontend  
**Primary Dependencies**: Spring Boot, JHipster 8, Spring Security/JWT, Spring Data JPA, Liquibase, MapStruct, React 18, React Router, Redux Toolkit + React-Redux, Reactstrap, WebSocket (STOMP), Jest + React Testing Library, Cypress, Cucumber, Gatling  
**Storage**: H2 file DB for `dev` profile; PostgreSQL (`jdbc:postgresql://localhost:5432/rnexchange`) for `prod`, both via Spring Data JPA with Liquibase-managed schema  
**Testing**: JUnit 5 + Spring Boot Test, Cucumber integration tests, Gatling performance tests, Cypress E2E tests, Jest + React Testing Library component/unit tests  
**Target Platform**: Linux-hosted Spring Boot monolith (Java 17) with React SPA served from `src/main/webapp`, targeting modern desktop browsers (Chrome/Edge); although the constitution frames Trader as a mobile-first experience long term, M6 work is explicitly validated via this desktop SPA.  
**Project Type**: JHipster monolith web application (single repository with Spring Boot backend and React frontend)  
**Performance Goals**: Support ~1,000 simulated concurrent traders, hundreds of quotes/sec, and 5–10 orders/sec with order placement latency <250 ms p95 and EOD settlement for ~10,000 positions completing within 5 minutes, consistent with the RNExchange constitution’s Performance Targets and **NFR-001 (Performance)** in `spec.md`  
**Constraints**: Strict TDD (tests first), API-first workflow (OpenAPI-driven), adherence to JHipster conventions (JDL entities, Liquibase migrations, MapStruct DTOs), strict RBAC (TRADER/BROKER_ADMIN/EXCHANGE_OPERATOR), educational transparency (simulator disclaimers), and minimal flakiness in CI, meaning critical-path M6 suites for the three demo flows meet the NFR-002 reliability target (≥95% pass rate over any rolling window of 20 runs)  
**Scale/Scope**: Single JHipster monolith instance for demo environments, three primary roles (Trader, Broker Admin, Exchange Operator), a small set of critical end-to-end flows, and performance validated for demo-scale rather than full production-scale deployment

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

- **Gate 1 — TDD-first (NON-NEGOTIABLE)**: All new or modified behaviour for M6 (critical flows, UX improvements, role-based navigation, statements, EOD) MUST be driven by tests written first: OpenAPI contracts + contract tests, backend integration tests, frontend unit/interaction tests, and Cypress E2E coverage for the three critical demo flows.
- **Gate 2 — API-First & JHipster Conventions**: Any new or changed REST endpoints (e.g., statements access, EOD controls, demo helpers) MUST be defined in OpenAPI 3 (`src/main/resources/swagger/api.yml`) before implementation, generated via `./mvnw generate-sources`, wired via delegate interfaces, and backed by JDL-defined entities with Liquibase migrations and MapStruct DTOs where applicable.
- **Gate 3 — RBAC & Least Privilege**: All new backend endpoints and UI routes MUST enforce `TRADER`, `BROKER_ADMIN`, and `EXCHANGE_OPERATOR` roles consistently (using `@PreAuthorize` and role-aware queries), ensure statements and ledger views are scoped correctly, and hide or disable navigation/actions that fall outside the logged-in user’s role (e.g., no generic “Entities” or irrelevant admin menus for traders).
- **Gate 4 — Real-Time Integrity**: QA hardening MUST NOT regress existing WebSocket-based market data, order status, or MTM updates; automated tests MUST cover at least one real-time trader flow (watchlist + order + portfolio updates) under load to confirm acceptable responsiveness and stable reconnection behaviour.
- **Gate 5 — Educational Transparency & Disclaimers**: The RNExchange-branded landing page, trader and broker dashboards, and generated statements MUST prominently convey “SIMULATED / NOT REAL MONEY” and other educational context (tooltips, explanatory copy) as required by the constitution.
- **Gate 6 — DDD Alignment**: New business logic for EOD, statements, ledger display, and risk flags MUST reside in domain services and aggregates (not controllers), reuse existing domain concepts (Order, Execution, Position, LedgerEntry, SettlementBatch, RiskAlert), and emit appropriate domain events where new significant state transitions are introduced.
- **Gate 7 — Governance & CI Health**: All new work MUST integrate cleanly with existing linting (ESLint/Checkstyle), formatting (Prettier/Java format), and SonarQube rules, and CI MUST run the expanded test suite (contract, integration, Gatling, Cypress) without persistent flaky failures; any deliberate constitution deviations would have to be documented in the Complexity Tracking table (currently none expected).

_Post–Phase 1 design check_: The M6 research, data model, contracts, and quickstart documents are consistent with these gates; no constitution violations have been identified that require entries in the Complexity Tracking table.

## Project Structure

### Documentation (this feature)

```text
specs/006-qa-hardening-demo/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/rnexchange/...        # Spring Boot + JHipster backend (entities, services, resources)
│   ├── resources/                     # Liquibase changelogs, application-*.yml, OpenAPI (`swagger/api.yml`), templates
│   └── webapp/                        # React SPA (React-JHipster) served by Spring Boot
└── test/
    ├── java/com/rnexchange/...        # Contract, integration, and unit tests
    ├── gatling/                       # Gatling performance simulations
    └── javascript/                    # Jest + Cypress tests for frontend and E2E flows
```

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation                  | Why Needed         | Simpler Alternative Rejected Because |
| -------------------------- | ------------------ | ------------------------------------ |
| [e.g., 4th project]        | [current need]     | [why 3 projects insufficient]        |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient]  |
