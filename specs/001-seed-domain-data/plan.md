# Implementation Plan: Seed Domain Trading Data Baseline

**Branch**: `001-seed-domain-data` | **Date**: 2025-11-13 | **Spec**: `/specs/001-seed-domain-data/spec.md`
**Input**: Feature specification from `/specs/001-seed-domain-data/spec.md`

**Note**: This plan is generated via `/speckit.plan`. Follow sections in order; later phases depend on earlier outputs.

## Summary

Create an idempotent Liquibase changelog that wipes legacy demo content and seeds production-like trading data (exchanges, broker, instruments, contracts, holidays, margin rules, user/account ties) so admin, broker, and trader roles can immediately exercise MVP workflows. Implementation hinges on aligning JDL entities with realistic seed rows, validating role mappings, and ensuring automated reset scripts cover both H2 (dev) and PostgreSQL (staging/prod) profiles.

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 17 (Spring Boot 3.4.5), TypeScript 5.x (React 18)  
**Primary Dependencies**: JHipster 8.11.0 stack (Spring Boot, Spring Security, Liquibase, MapStruct), Liquibase XML changelogs, JPA/Hibernate  
**Storage**: Liquibase-managed relational DB (H2 file DB for dev, PostgreSQL 15 for docker/prod)  
**Testing**: JUnit 5 + Spring Boot integration tests with `@EmbeddedSQL` (Liquibase idempotency + record assertion), Cucumber (Gherkin acceptance), Cypress E2E  
**Target Platform**: JHipster monolith (backend REST + React frontend) deployed on Linux containers  
**Project Type**: Web monolith with shared backend/frontend repo  
**Performance Goals**: Seed execution under 5 minutes on dev hardware; startup verification flows in <1 minute for broker desk per SC-002  
**Constraints**: Must follow TDD workflow, enforce RBAC mappings, use Liquibase-only migrations, run pre-seed truncation routine to wipe faker/demo datasets before applying new baseline changelog  
**Scale/Scope**: MVP baseline (3 exchanges, ~15 instruments/contracts, <=5 user accounts) with room for future expansion

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

- **I. TDD (Non-Negotiable)**: PASS — Plan schedules contract + integration tests before Liquibase seeding code merges; quickstart will mandate failing tests that assert seed presence/absence.
- **II. JHipster Conventions**: PASS — Strategy relies on JDL-aligned entities, Liquibase XML changelog under `config/liquibase/data`, and MapStruct-generated DTOs; no custom ORM shortcuts planned.
- **III. RBAC**: PASS — Seed data maps broker/trader roles explicitly and calls out verification tests for `@PreAuthorize` scopes.
- **VI. DDD**: PASS — Seed records align with existing aggregates (Exchange, Broker, TraderProfile, TradingAccount, MarginRule); ubiquitous language preserved.
- **VII. API-First**: N/A — No new endpoints anticipated; if discovery in Phase 1 reveals API adjustments, OpenAPI update will precede code.
- **Guardrails**: No violations detected; continue to Phase 0 once clarifications below are resolved in `research.md`.
- **Post-Phase-1 Review**: PASS — `research.md`, `data-model.md`, `baseline-seed.openapi.yaml`, and `quickstart.md` align with TDD + Liquibase rules; API-first obligations satisfied via new OpenAPI contract stub.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
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
src/
├── main/
│   ├── java/com/rnexchange/...        # Spring Boot application, domain/services/resources
│   └── resources/
│       ├── config/liquibase/          # Migrations + seed changelogs (new 0001-seed-domain-data.xml)
│       ├── application-*.yml
│       └── swagger/api.yml
└── test/
    ├── java/com/rnexchange/...        # JUnit + Cucumber tests (add seed verification)
    └── resources/                     # Gherkin features, test Liquibase configs

src/main/webapp/
├── app/                               # React entities/modules (ensure seeded data reflected)
└── content/i18n/                      # Add glossary/tooltips if needed in quickstart
```

**Structure Decision**: Continue using the existing JHipster monolith layout; feature work touches Liquibase data under `src/main/resources/config/liquibase`, backend integration tests under `src/test/java`, and documentation under `specs/001-seed-domain-data/`.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation                  | Why Needed         | Simpler Alternative Rejected Because |
| -------------------------- | ------------------ | ------------------------------------ |
| [e.g., 4th project]        | [current need]     | [why 3 projects insufficient]        |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient]  |
