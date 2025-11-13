# Research: Seed Domain Trading Data Baseline

## Decision Log

### Decision: Prefer targeted truncation runner over schema drop for baseline reset

- **Rationale**: Preserves Liquibase checksum history, respects JHipster-managed tables, and enables idempotent reseeding across dev/staging without destructive migrations.
- **Alternatives considered**:
  - Full database drop — rejected because Liquibase metadata tables and Spring Batch artifacts must persist.
  - Ad-hoc SQL delete scripts — rejected due to maintenance overhead and risk of missing future entities.

### Decision: Isolate curated data under a dedicated `baseline` Liquibase context

- **Rationale**: Allows profile-driven execution (dev/test vs prod), keeps faker data disabled, and simplifies Spring profile configuration for tests.
- **Alternatives considered**:
  - Reusing `dev` context — rejected; conflicts with existing faker seeds.
  - SQL import scripts outside Liquibase — rejected; breaks change tracking and rollback guarantees.

### Decision: Encode deterministic seed values for instruments, derivatives, and margin rules

- **Rationale**: Enables reproducible assertions in integration/Cypress tests, aligns with constitution TDD mandates, and prevents subjective “realistic” interpretations.
- **Alternatives considered**:
  - Relative tolerance ranges — rejected; complicates deterministic test expectations.
  - Runtime-configurable values — rejected for MVP to avoid additional configuration surface area.

### Decision: Execute Liquibase idempotency integration tests using embedded PostgreSQL/H2

- **Rationale**: Mirrors production startup (truncate → migrate → rerun), surfaces checksum errors early, and reuses existing `@EmbeddedSQL` infrastructure.
- **Alternatives considered**:
  - Unit tests around individual services — insufficient because seeding is Liquibase-driven.
  - Manual QA verification — slow and non-repeatable.
  - Liquibase `RollbackTest` CLI — adds CI overhead without verifying entity relationships.

### Decision: Enforce SLA metrics through Cypress and Cucumber for broker/trader journeys

- **Rationale**: Measures end-to-end readiness (SC-002) in the UI stack, captures login-to-ready timings automatically, and validates trader order lifecycle against seeded data.
- **Alternatives considered**:
  - Backend stopwatch assertions only — rejected; misses front-end rendering latency.
  - Manual smoke tests — rejected; violates constitution requirement for automated validation.

### Decision: Seed RBAC roles using constitution-defined authorities (`EXCHANGE_OPERATOR`, `BROKER_ADMIN`, `TRADER`)

- **Rationale**: Keeps terminology consistent across spec, tasks, and implementation while satisfying RBAC principle and preventing ambiguity around legacy “admin” naming.
- **Alternatives considered**:
  - Reuse generic `ROLE_ADMIN` mappings — rejected; conflicts with constitution Principle III and acceptance criteria.
