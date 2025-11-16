# Implementation Plan: [FEATURE]

**Branch**: `004-broker-backoffice` | **Date**: 2025-11-16 | **Spec**: `/home/explorer/Development/rnexchange/specs/004-broker-backoffice/spec.md`
**Input**: Feature specification from `/specs/004-broker-backoffice/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Implement M3 Broker Back Office to provide broker-scoped visibility (dashboard and clients list), simple funds journals (credit/debit with idempotency), and lightweight risk snapshots ranked by utilization. Technical approach aligns with JHipster 8 stack: API-first with OpenAPI updates under `src/main/resources/swagger/api.yml`, Spring Boot resource/service layers with RBAC scoping by broker, Liquibase migrations only if new persistence is required (journal/ledger reuse preferred), and React UI additions within existing JHipster structure. Testing is TDD-first: contract tests for new endpoints, integration tests for broker scoping, and UI/Cypress for critical flows.

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 21 (Spring Boot via JHipster 8), TypeScript/React 18  
**Primary Dependencies**: Spring Boot, Spring Security, MapStruct, Liquibase, JPA/Hibernate, Redux Toolkit, Cypress/Jest, Gatling  
**Storage**: PostgreSQL via Liquibase-managed schema; reuse existing M2 entities (TraderProfile, TradingAccount, Position, LedgerEntry)  
**Testing**: JUnit 5 (backend), Contract tests from OpenAPI, Jest/RTL (frontend), Cypress (E2E), Gatling (perf)  
**Target Platform**: Linux server (containerized via Jib/Docker Compose provided)  
**Project Type**: Web application (backend + frontend in single JHipster repo)  
**Performance Goals**: Broker dashboard perceived load ≤ ~2s for a few hundred traders; backend p95 <250ms per API; WebSocket price freshness ≤1 min for exposure inputs  
**Constraints**: TDD (tests before impl), API-first (OpenAPI → generate), strict RBAC scoping by broker, Liquibase only for schema changes  
**Scale/Scope**: Milestone M3; typical broker has 10s–100s traders; no cross-broker access

Unknowns flagged for Phase 0 research (resolved; see `research.md` and `spec.md` Clarifications):

- ε constant for utilization clamp → ε = 1.0 (currency units)
- Unrealized P&L source and price freshness → reuse M2 valuation; exclude prices older than 60s
- Idempotency strategy → required `Idempotency-Key` header; persist tokens; 6h retention
- Journal entry mapping and audit → `JOURNAL_CREDIT`/`JOURNAL_DEBIT`; include audit fields and idempotency token reference

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

- TDD enforced: Plan includes contract/integration/UI tests before impl — PASS
- JHipster conventions: Use JDL for new entities if required; Liquibase; MapStruct; layered architecture — PASS
- RBAC: Endpoints guarded with `@PreAuthorize('hasRole(\"BROKER_ADMIN\")')` and broker scoping at service/repo — PASS
- API-First: Update `src/main/resources/swagger/api.yml` prior to code and run codegen — PASS
- Real-Time: Price freshness requirement acknowledged; reuse existing WebSocket/price sources — PASS
- DDD: Reuse domain entities; new domain logic in services — PASS

No violations anticipated. If idempotency requires a new table, it will be introduced via JDL/Liquibase with tests.

Post-Phase 1 re-check: Research resolved unknowns; contracts/data-model align with API-first, RBAC, and TDD — PASS.

## Project Structure

### Documentation (this feature)

```text
specs/004-broker-backoffice/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
src/main/java/com/rnexchange/                 # JHipster backend
├── domain/                                   # Existing entities (reuse)
├── repository/
├── service/
│   ├── broker/                               # New M3 broker services
│   └── dto/
└── web/rest/
    └── broker/                               # New REST resources (broker-scoped)

src/main/resources/
├── config/
├── swagger/
│   └── api.yml                               # Update with new broker endpoints
└── db/changelog/                             # Liquibase if needed

src/main/webapp/app/
├── modules/broker/                           # Broker Dashboard, Clients, Journal UI
└── shared/

src/test/java/com/rnexchange/
├── contract/                                 # OpenAPI/contract tests
├── integration/                              # RBAC & broker-scoping tests
└── unit/

src/test/javascript/cypress/                  # UI tests for broker flows
```

**Structure Decision**: Single JHipster monorepo (backend + frontend). New broker-specific REST resources and services live under `web/rest/broker` and `service/broker`. Frontend feature under `app/modules/broker`. Contracts defined in `src/main/resources/swagger/api.yml` per API-first policy.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation                  | Why Needed         | Simpler Alternative Rejected Because |
| -------------------------- | ------------------ | ------------------------------------ |
| [e.g., 4th project]        | [current need]     | [why 3 projects insufficient]        |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient]  |
