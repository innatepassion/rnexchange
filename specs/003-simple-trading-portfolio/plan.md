# Implementation Plan: Simple Trading & Portfolio (M2)

**Branch**: `003-simple-trading-portfolio` | **Date**: 2025-11-15 | **Spec**: `specs/003-simple-trading-portfolio/spec.md`  
**Input**: Feature specification from `/specs/003-simple-trading-portfolio/spec.md`

**Note**: Plan is intentionally minimal and pragmatic for M2; it reuses existing JHipster patterns and avoids new infrastructure or abstractions.

## Summary

Implement a simple, cash-only trading loop so a Trader can place Market or Limit BUY/SELL orders on supported instruments, have them immediately matched using mock market prices, and see resulting positions and cash ledger updates, while Broker Admins can view activity by broker.  
The technical approach is to extend the existing JHipster Spring Boot + React stack with a minimal `TradingService` and `MatchingService`, a few new entities (Order, Execution, Position, LedgerEntry) wired into existing repositories, REST endpoints for orders and portfolio views, and lightweight WebSocket notifications that trigger client-side refetches.

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 17 (Spring Boot JHipster backend), TypeScript/React frontend  
**Primary Dependencies**: JHipster 8.x stack (Spring Boot, Spring Data JPA, Spring WebSocket/STOMP, React + Redux Toolkit)  
**Storage**: PostgreSQL via JPA entities and Liquibase migrations (reusing existing DB setup)  
**Testing**: JUnit 5 + Spring test for backend (unit/integration), Jest + React Testing Library + Cypress for frontend, Gatling for load (reused as needed)  
**Target Platform**: Linux server backend, browser-based web frontend  
**Project Type**: Web application with shared monorepo (Spring Boot backend under `src/main/java`, React frontend under `src/main/webapp`)  
**Performance Goals**: Reuse constitution defaults; for this feature, ensure order placement and portfolio updates meet the constitution performance targets (e.g., p95 order placement latency \<250 ms) and SC-004’s 2-second UI reflection target, without requiring special tuning beyond standard implementation care.  
**Constraints**: Must adhere to RNExchange constitution (TDD, API-first, RBAC, WebSocket rules); avoid introducing new architectural layers or patterns beyond existing JHipster conventions.  
**Scale/Scope**: Single-broker, single-exchange training environment with up to a few hundred concurrent Traders exercising the simple cash-only flow; no special scaling work for this milestone.

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

- **TDD (I)**: We will add contract tests for the new REST endpoints (`/api/orders`, `/api/trading-accounts/{id}/positions`, `/api/trading-accounts/{id}/ledger-entries`) and write integration/unit tests for `TradingService` and `MatchingService` before implementing full logic.
- **JHipster Conventions (II)**: New entities will be modeled via JDL and Liquibase, exposed via generated REST resources and services where appropriate, and integrated into the existing `api.yml` OpenAPI definition.
- **RBAC (III)**: REST endpoints and WebSocket topics will enforce role checks (`TRADER` for own accounts; `BROKER_ADMIN` for broker-scoped views) and respect existing scoping rules.
- **Real-Time Architecture (IV)**: Order and execution events will publish to existing WebSocket infrastructure using topics `/topic/orders/{tradingAccountId}` and `/topic/executions/{tradingAccountId}`, intentionally scoping by trading account ID as a concrete specialization of the constitution’s `/topic/orders/{userId}` example, and allowing the frontend to refetch lightweight lists.
- **Educational Transparency (V)**: Error messages and validation failures (e.g., insufficient funds, inactive instrument) will be human-readable and oriented to learning.
- **DDD (VI)**: Trading logic (validation, matching, position and cash updates) will live in domain services (`TradingService`, `MatchingService`) and operate on domain entities (Order, Execution, Position, LedgerEntry) rather than in controllers or repositories.
- **API-First (VII)**: We will update the OpenAPI spec for the new endpoints first, generate server stubs, and then implement the delegates.

_Gate status_: **PASS** — all planned work aligns with constitution; no exceptions or complexity justifications required.

## Project Structure

### Documentation (this feature)

```text
specs/003-simple-trading-portfolio/
├── plan.md              # This file (/speckit.plan output)
├── research.md          # Phase 0 output (/speckit.plan)
├── data-model.md        # Phase 1 output (/speckit.plan)
├── quickstart.md        # Phase 1 output (/speckit.plan)
├── contracts/           # Phase 1 output (/speckit.plan)
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/rnexchange/...
│   │   ├── domain/            # Entities: TradingAccount, Instrument, Order, Execution, Position, LedgerEntry
│   │   ├── repository/        # Spring Data repositories
│   │   ├── service/           # TradingService, MatchingService and related domain services
│   │   └── web/rest/          # REST resources for orders, positions, ledger entries
│   └── webapp/app/            # React components and Redux slices
│       ├── modules/market-watch/   # Existing market watch; add order ticket drawer
│       └── modules/trader/         # New views: Orders & Trades, Portfolio & Cash
└── test/
    ├── java/com/rnexchange/...     # JUnit tests (unit + integration)
    └── javascript/cypress/...      # Cypress E2E tests for core trading flows
```

**Structure Decision**: Reuse the existing JHipster monolith layout with minimal additions: new domain entities, services, and REST resources in the backend, plus a small number of React components/tables on the frontend; no new top-level projects or cross-cutting frameworks are introduced.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
| --------- | ---------- | ------------------------------------ |
| (none)    |            |                                      |
