# Implementation Plan: Mock Market Data & Market Watch

**Branch**: `002-mock-market-data` | **Date**: 2025-11-13 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-mock-market-data/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

This feature implements a full end-to-end simulated market data pipeline for NSE, BSE, and MCX exchanges. The backend MockMarketDataService maintains in-memory OHLC state for all active instruments, applies bounded random-walk price updates at 500-1000ms intervals, and broadcasts quotes and bars over Spring WebSocket/STOMP. Exchange operators can control the feed via REST endpoints, while traders view live prices in a Market Watch screen that subscribes to WebSocket topics and manages watchlists. The implementation respects market holidays, enforces trading sessions, and provides clear visual indicators for simulated data and connection status.

## Technical Context

**Language/Version**: Java 21 (backend), TypeScript 5.x (frontend)  
**Primary Dependencies**: Spring Boot 3.x, Spring WebSocket/STOMP, React 18, Redux Toolkit, MapStruct, Liquibase  
**Storage**: PostgreSQL (existing instrument, holiday, watchlist tables)  
**Testing**: JUnit 5 + Spring Test (backend), Jest + React Testing Library (frontend), Cypress (E2E)  
**Target Platform**: Linux server (backend), modern browsers (Chrome, Firefox, Safari)
**Project Type**: Web application (JHipster 8.x monolithic architecture)  
**Performance Goals**: 10,000 tick updates/sec broadcast capacity, <250ms order placement latency (p95), support 1,000+ concurrent traders  
**Constraints**: <2s status update latency after operator actions, <3s watchlist modification feedback, zero tick generation for holiday exchanges  
**Scale/Scope**: 3 exchanges (NSE, BSE, MCX), ~500-1000 instruments per exchange, 5-10 watchlists per trader, 1-minute bar aggregation

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

| Principle                       | Status  | Evidence / Plan                                                                                                                                                                                                                                                                                                                        |
| ------------------------------- | ------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **I. TDD**                      | ✅ PASS | Feature spec includes Given-When-Then acceptance scenarios for all 3 user stories. Will write contract tests for REST endpoints (start/stop/status, watchlist CRUD) and integration tests for WebSocket subscription flows before implementation. Target: 90%+ backend coverage, 80%+ frontend coverage.                               |
| **II. JHipster Conventions**    | ✅ PASS | Will follow JHipster 8.x layered architecture (Entity → Repository → Service → Resource). OpenAPI spec will define REST endpoints first, then implement delegates. Liquibase migrations for any schema changes. MapStruct DTOs for quote/bar serialization. React components follow Redux Toolkit patterns.                            |
| **III. RBAC**                   | ✅ PASS | Exchange operator endpoints (`/api/marketdata/mock/*`) require `EXCHANGE_OPERATOR` role via `@PreAuthorize`. Trader watchlist endpoints (`/api/watchlists/*`) require `TRADER` role and scope queries by user ID. WebSocket topics validate JWT and enforce role-based access (traders can only subscribe to their watchlist symbols). |
| **IV. Real-Time Architecture**  | ✅ PASS | Core feature requirement. Implements Spring WebSocket/STOMP with topics `/topic/quotes/{symbol}` and `/topic/bars/{symbol}`. JWT validation in handshake. Client reconnection with exponential backoff. Mock generator configurable (frequency 500-1000ms, volatility 1% per tick).                                                    |
| **V. Educational Transparency** | ✅ PASS | Market Watch displays persistent "SIMULATED FEED" badge. WebSocket status indicator shows connection state. Console panel shows "Running/Stopped" feed status. All UI elements clarify this is mock/training data.                                                                                                                     |
| **VI. DDD**                     | ✅ PASS | Domain services: `MockMarketDataService` (price generation), `WatchlistService` (membership management). Value objects: `QuoteDTO`, `BarDTO`. Domain events can be added later (e.g., `FeedStartedEvent`). Business logic (random walk, OHLC aggregation, holiday checks) resides in services, not controllers.                        |
| **VII. API-First**              | ✅ PASS | Will define OpenAPI spec for operator control endpoints (`POST /api/marketdata/mock/start`, `POST /api/marketdata/mock/stop`, `GET /api/marketdata/mock/status`) and watchlist CRUD endpoints before implementation. Run `./mvnw generate-sources` to generate interfaces, then implement delegates.                                   |

**Gate Result**: ✅ **APPROVED** — All principles satisfied. Feature aligns with M1 milestone (Market Data Mock). No constitution violations requiring justification.

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

```text
src/main/
├── java/com/rnexchange/
│   ├── domain/
│   │   ├── Instrument.java (existing)
│   │   ├── MarketHoliday.java (existing)
│   │   └── Watchlist.java (existing)
│   ├── repository/
│   │   ├── InstrumentRepository.java (existing)
│   │   ├── MarketHolidayRepository.java (existing)
│   │   └── WatchlistRepository.java (existing)
│   ├── service/
│   │   ├── marketdata/
│   │   │   ├── MockMarketDataService.java (NEW)
│   │   │   ├── PriceGenerator.java (NEW)
│   │   │   └── BarAggregator.java (NEW)
│   │   ├── dto/
│   │   │   ├── QuoteDTO.java (NEW)
│   │   │   ├── BarDTO.java (NEW)
│   │   │   └── FeedStatusDTO.java (NEW)
│   │   └── WatchlistService.java (existing, extend)
│   ├── web/
│   │   ├── rest/
│   │   │   ├── MarketDataResource.java (NEW)
│   │   │   └── WatchlistResource.java (existing, extend)
│   │   └── websocket/
│   │       ├── MarketDataWebSocketHandler.java (NEW)
│   │       └── WebSocketConfig.java (existing, extend)
│   └── config/
│       └── SchedulingConfig.java (existing, extend)
├── resources/
│   ├── swagger/
│   │   └── api.yml (extend with mock endpoints)
│   └── config/
│       └── liquibase/changelog/ (migrations if needed)
└── webapp/
    └── app/
        ├── modules/
        │   ├── market-watch/ (NEW)
        │   │   ├── market-watch.tsx
        │   │   ├── market-watch.reducer.ts
        │   │   ├── websocket-service.ts
        │   │   └── watchlist-selector.tsx
        │   └── exchange-console/ (existing, extend)
        │       └── market-data-panel.tsx (NEW)
        └── shared/
            ├── model/
            │   ├── quote.model.ts (NEW)
            │   └── bar.model.ts (NEW)
            └── websocket/
                └── stomp-client.ts (NEW)

src/test/
├── java/com/rnexchange/
│   ├── service/marketdata/
│   │   ├── MockMarketDataServiceIT.java (NEW)
│   │   └── PriceGeneratorTest.java (NEW)
│   └── web/rest/
│       └── MarketDataResourceIT.java (NEW)
└── javascript/spec/
    └── app/modules/market-watch/
        └── market-watch.spec.tsx (NEW)
```

**Structure Decision**: JHipster 8.x monolithic architecture. Backend follows layered pattern (Domain → Repository → Service → Resource). Frontend uses Redux Toolkit with feature-based modules. WebSocket configuration extends existing Spring WebSocket setup. Tests mirror source structure (contract tests for REST, integration tests for services, component tests for React).

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

_No violations detected. This section intentionally left empty._

---

## Constitution Check (Post-Design Re-evaluation)

_Re-evaluated after Phase 1 design completion (research.md, data-model.md, contracts/)_

| Principle                       | Status  | Post-Design Evidence                                                                                                                                                                                                                                                                                                                                                      |
| ------------------------------- | ------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **I. TDD**                      | ✅ PASS | Contract tests defined in quickstart.md for all REST endpoints. Integration test patterns documented for WebSocket subscriptions. Unit test examples provided for PriceGenerator and InstrumentState. Test-first approach maintained throughout design phase.                                                                                                             |
| **II. JHipster Conventions**    | ✅ PASS | OpenAPI specification created following JHipster API-first pattern (mock-market-data.openapi.yaml). DTOs designed as Java records per modern JHipster conventions. Liquibase migrations not needed (existing M0 schema sufficient). WebSocket config extends JHipster's existing Spring WebSocket setup.                                                                  |
| **III. RBAC**                   | ✅ PASS | All REST endpoints include security annotations in OpenAPI spec (bearerAuth). Role enforcement documented: EXCHANGE_OPERATOR for feed control, TRADER for watchlist management. WebSocket JWT validation specified in websocket-topics.md contract. Ownership checks implemented in watchlist endpoint design.                                                            |
| **IV. Real-Time Architecture**  | ✅ PASS | Complete WebSocket/STOMP topic design documented in websocket-topics.md. Subscription patterns defined (`/topic/quotes/{symbol}`, `/topic/bars/{symbol}`). Reconnection logic with exponential backoff specified. Client-side React hook pattern provided for declarative subscription management. Performance optimizations documented (batch broadcasting, throttling). |
| **V. Educational Transparency** | ✅ PASS | UI mockups in quickstart.md show "SIMULATED FEED" badge. Connection status indicators documented. Exchange console status display includes clear "Running/Stopped/Holiday" states. All user-facing components emphasize training/educational context.                                                                                                                     |
| **VI. DDD**                     | ✅ PASS | Domain services clearly separated (MockMarketDataService for business logic, PriceGenerator for random walk algorithm). Value objects defined as immutable records (QuoteDTO, BarDTO). InstrumentState modeled as aggregate with OHLC invariants. Business rules (holiday checking, bounds enforcement) encapsulated in service layer, not controllers.                   |
| **VII. API-First**              | ✅ PASS | OpenAPI 3.0 specification completed before implementation (contracts/mock-market-data.openapi.yaml). All endpoints include request/response schemas, error scenarios, and examples. Code generation workflow documented (./mvnw generate-sources). Delegate pattern follows JHipster conventions.                                                                         |

**Gate Result**: ✅ **APPROVED** — All principles satisfied post-design. Design artifacts (research, data-model, contracts, quickstart) align with constitution. No violations introduced during planning phase.

**Design Quality**: High confidence in implementation feasibility. All technical unknowns resolved in research.md. Data model leverages existing M0 entities (no schema migrations needed). API contracts comprehensive with error handling. Performance optimizations identified and documented.

---

## Phase 2 Readiness

✅ **READY FOR PHASE 2** — All planning artifacts complete:

- ✅ `plan.md` — This document (summary, technical context, constitution check, structure)
- ✅ `research.md` — 8 research sections covering WebSocket, random walk, OHLC, holidays, subscriptions, performance, operator control, watchlist management
- ✅ `data-model.md` — Entities, DTOs, state models, validation rules, relationships
- ✅ `contracts/mock-market-data.openapi.yaml` — REST API specification (5 endpoints, schemas, examples)
- ✅ `contracts/websocket-topics.md` — WebSocket protocol documentation (STOMP handshake, topic patterns, message formats)
- ✅ `quickstart.md` — Implementation guide (TDD workflow, setup instructions, testing procedures)
- ✅ Agent context updated — `.cursor/rules/specify-rules.mdc` includes new tech stack

**Next Command**: Run `/speckit.tasks` to generate `tasks.md` with granular implementation checklist.
