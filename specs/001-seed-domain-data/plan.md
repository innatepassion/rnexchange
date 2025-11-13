# Implementation Plan: Seed Domain Trading Data Baseline

**Branch**: `001-seed-domain-data` | **Date**: 2025-11-13 | **Spec**: `/specs/001-seed-domain-data/spec.md`
**Input**: Feature specification from `/specs/001-seed-domain-data/spec.md`

## Summary

- Establish a clean trading baseline by validating prerequisites, then truncating legacy demo data, replaying Liquibase migrations, seeding deterministic exchange/broker/instrument datasets aligned with MVP needs, and instrumenting structured observability and audit trails end-to-end.
- Deliver API-first orchestration (`BaselineSeedResource`) plus background runners to manage cleanup, seeding, validation, and observability of the baseline job.
- Ensure broker and trader user journeys operate solely on curated data, supported by Cypress, Cucumber, integration, and Gatling coverage mandated by the constitution, with structured logs and audit evidence for every critical flow.

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.4.5) backend, TypeScript 5.x / React 18 frontend (JHipster 8.11.0 scaffold).  
**Primary Dependencies**: Liquibase, MapStruct, Spring Data JPA, Spring Security, Spring WebSocket, React + Redux Toolkit, Cypress, Jest, Cucumber.  
**Storage**: H2 (dev/testing) and PostgreSQL 14+ (prod) with Liquibase-managed schema (`master.xml`).  
**Testing**: JUnit 5 + Spring Boot integration tests, Cucumber BDD features, Cypress E2E specs, Jest/RTL for UI, Gatling performance harness (available if needed).  
**Target Platform**: JHipster monolith ( Spring Boot service + React SPA ) deployable on Linux containers/VMs.  
**Project Type**: Web monolith (single repo containing backend + frontend).  
**Performance Goals**: Baseline seed completes <5 minutes (SC-001); broker desk readiness <60 seconds from login (SC-002); order execution remains under the platform baseline `<250 ms p95` target per spec NFR-006 (no new work in this feature beyond preventing regressions).
**Constraints**: Liquibase is the single source of truth for migrations; tests precede implementation (TDD non-negotiable); RBAC must enforce `EXCHANGE_OPERATOR`, `BROKER_ADMIN`, `TRADER`; seeding must be idempotent and profile-aware; destructive cleanup only occurs after prerequisite validation succeeds.  
**Scale/Scope**: MVP dataset (3 exchanges, 1 broker, 5+ cash instruments, derivative set, margin rules, user mappings) impacting order, risk, and UI flows.

## Constitution Check

- **I. Test-Driven Development (TDD)**: Tasks enforce test creation (integration, REST, Cypress, Cucumber) before implementation; plan preserves red-green-refactor cadence.
- **II. JHipster Conventions & Best Practices**: Uses Liquibase changelog includes, MapStruct DTOs, layered services/resources, generator-aligned project layout.
- **III. Role-Based Governance (RBAC)**: API endpoints require `EXCHANGE_OPERATOR`; validation runner confirms broker/trader roles and mappings.
- **VI. Domain-Driven Design (DDD)**: Seed logic scoped to domain services/runners; changelog maintains aggregate invariants; ubiquitous language maintained (`Exchange`, `MarginRule`, etc.).
- **VII. API-First Development**: `baseline-seed.openapi.yaml` defined prior to implementing `BaselineSeedResource`; code generation planned via `./mvnw generate-sources`.  
  _Gate Status_: **PASS** — no deviations from constitution requirements detected pre-design.

## Project Structure

### Documentation (feature)

```text
specs/001-seed-domain-data/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── baseline-seed.openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/com/rnexchange/
├── config/                  # Liquibase runners, constants
├── domain/                  # Existing aggregates (Exchange, Broker, etc.)
├── repository/              # Spring Data repositories
├── service/
│   ├── seed/                # New truncate/seed services & job registry
│   └── startup/             # Validation runner hooks
├── service/dto/             # DTO definitions (BaselineSeedJobDTO)
└── web/rest/                # REST resources (BaselineSeedResource)

src/main/resources/config/
├── application-dev.yml      # Liquibase context alignment (`baseline`)
├── application-prod.yml     # Prod profile updates
└── liquibase/
    ├── master.xml           # Includes `data/0001-seed-domain-data.xml`
    └── data/                # New deterministic seed changelog

src/test/java/com/rnexchange/
├── service/seed/            # Integration tests for cleanup + seed lifecycle
├── web/rest/                # REST contract/integration tests
├── service/                 # MarginService tests
└── cucumber/                # Baseline order flow feature glue

src/test/resources/com/rnexchange/cucumber/
└── baseline_seed.feature    # Trader journey scenario

src/test/javascript/cypress/e2e/
├── broker/broker-seed.cy.ts # Broker readiness SLA
└── trader/trader-seed.cy.ts # Trader order simulation
```

**Structure Decision**: Retain single JHipster monolith; add seed-specific services, runners, and changelog under existing modular folders while extending frontend Cypress suites for broker/trader validation.

## Phase 0 – Research Summary

- Verified Liquibase context strategy (`baseline`) and truncation runner approach, documenting decisions in `research.md`.
- Catalogued deterministic seed values (tick sizes, lot sizes, derivatives) and RBAC alignment per spec clarifications.
- Confirmed testing stack (Spring Boot IT, Cypress, Cucumber) and performance targets consistent with constitution mandates.

## Phase 1 – Design & Contracts Outputs

- Data relationships and validation rules captured in `data-model.md` to guide changelog ordering and integrity constraints.
- `contracts/baseline-seed.openapi.yaml` models asynchronous seed job endpoints gating code generation.
- `quickstart.md` documents end-to-end validation steps (admin, broker, trader) and troubleshooting aligned with acceptance tests.
- Agent context refreshed via `.specify/scripts/bash/update-agent-context.sh cursor-agent` to record new technologies/decisions.

## Phase 2 – Implementation Readiness

- Coordinate Phase 1/2 tasks from `tasks.md`: align Liquibase contexts (T001–T006), author deterministic changelog (T019), implement seed services/resources (T012–T021), and extend RBAC-aware validation runners (T020).
- Ensure UI/backend workstreams respect dependency graph: foundational contexts → seed services → structured logging/audit instrumentation → RBAC validation → broker/trader enhancements.
- Maintain TDD order: execute test tasks (T007–T011, T022–T023, T028–T030, T036, T038) before touching implementation counterparts, including performance verification via T040 once implementation stabilises.

## Phase 3 – User Story 1 Seed Account Details

- Reuse the constitution-mandated existing RBAC users so FR-003 stays aligned with the spec:
  - Confirm the current `BROKER_ADMIN` account is mapped to RN DEMO BROKING via the new seed changelog.
  - Ensure the existing `EXCHANGE_OPERATOR` account remains authoritative for seed orchestration and validation.
  - Associate the two pre-existing trader personas from the platform bootstrap with the seeded `TraderProfile` + `TradingAccount` rows (status ACTIVE, base currency INR, balance ₹1,000,000).
- Use the standard JHipster encrypted fixture values already present in prior changelogs where credentials must be referenced.
- Reference these established user identifiers in startup validation, integration tests, and Cypress/Cucumber flows to keep journeys reproducible across environments without introducing duplicate RBAC users.

### Prerequisite Validation Sequence (FR-010 Alignment)

- Create a lightweight `BaselinePrerequisiteValidator` invoked at startup to confirm RBAC users, timezones, and Liquibase contexts exist before any truncation logic executes.
- Ensure `BaselineSeedCleanupRunner` depends on the validator outcome; if validation fails, abort the job without touching database contents and surface the failure via structured logs/metrics.
- Update seed orchestration tests to cover both successful validation leading to cleanup and failure paths that skip destructive actions.

### Structured Logging & Failure Telemetry (NFR-003 & NFR-005 Alignment)

- Extend `BaselineSeedCleanupRunner`, `BaselineSeedService`, and `BaselineValidationRunner` logging to include actor metadata (`actorId`, `actorRole`) and domain context (`instrument`, `outcome`) in addition to the existing `phase`, `entityType`, `status`, `durationMs`, and `failureReason` fields.
- Ensure `BaselineSeedLoggingIT` and `TraderAuditLogIT` exercise cleanup, seeding, validation, and trader audit flows, covering both success and forced-failure paths, and asserting that each JSON payload carries the expanded field set.
- Reuse the JHipster structured logging pipeline (SLF4J + JSON layout/Micrometer); do not introduce ad-hoc logging utilities.
- Surface the telemetry via existing log/metrics sinks so operators can triage failed runs quickly.

### Verification Gates & Idempotency (NFR-003 & NFR-004 Alignment)

- Design deterministic verification gates inside `BaselineSeedService` that compute checksum/invariant assertions after each seeding phase (e.g., exchange, broker, instrument counts) and abort with a descriptive failure if expectations are violated or duplicates appear.
- Capture verification metadata in structured logs and duration metrics to make rerun behaviour observable.
- Implement dedicated integration tests that force duplicate/partial data scenarios to ensure the verification gates fail fast and leave the database unchanged.
- Document the verification strategy in `quickstart.md` to guide operators on interpreting checksum/invariant failures.

### Broker Readiness SLA & Telemetry (SC-002 Alignment)

- Update the `broker-seed.cy.ts` flow to capture the login-to-ready duration, persist the measurement as part of Cypress test artifacts (e.g., JSON fixture or console attachment), and assert that the metric remains ≤60 seconds.
- Document the persisted metric output location so CI retains the data for trend analysis and SLA enforcement.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
| --------- | ---------- | ------------------------------------ |
| _None_    | —          | —                                    |
