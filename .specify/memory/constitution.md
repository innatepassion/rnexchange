<!--
SYNC IMPACT REPORT
==================
Version Change: [TEMPLATE] → 1.0.0
Created: 2025-11-12

This is the initial constitution ratification for RNExchange.

Principles Defined:
- I. Test-Driven Development (TDD) - NON-NEGOTIABLE
- II. JHipster Conventions & Best Practices
- III. Role-Based Governance (RBAC)
- IV. Real-Time Architecture
- V. Educational Transparency
- VI. Domain-Driven Design
- VII. API-First Development

Templates Status:
✅ plan-template.md - Reviewed, compatible (Constitution Check section present)
✅ spec-template.md - Reviewed, compatible (user stories & requirements align)
✅ tasks-template.md - Reviewed, compatible (TDD test-first emphasis present)

Follow-up TODOs: None
-->

# RNExchange Constitution

## Core Principles

### I. Test-Driven Development (TDD) — NON-NEGOTIABLE

**Rules**:

- Tests MUST be written before implementation code (Red-Green-Refactor cycle strictly enforced)
- All feature specifications MUST include acceptance scenarios in Given-When-Then format
- Contract tests MUST be created for all API endpoints before implementation
- Integration tests MUST validate user journeys end-to-end
- Unit tests MUST achieve minimum 90% backend coverage, 80% frontend coverage
- Tests MUST fail first, then pass after implementation
- No feature is considered complete until all tests pass in CI

**Rationale**: RNExchange is a financial simulation platform where accuracy, reliability, and correctness are paramount. TDD ensures that every feature behaves as specified and prevents regression in critical trading, settlement, and risk management logic.

---

### II. JHipster Conventions & Best Practices

**Rules**:

- All code generation MUST use JHipster 8.x generators and blueprints
- Entity definitions MUST be declared in JDL (JHipster Domain Language)
- Database migrations MUST use Liquibase exclusively
- DTOs MUST be generated via MapStruct; no manual mapping
- Spring Boot services MUST follow JHipster layered architecture: Entity → Repository → Service → Resource (REST)
- React components MUST follow JHipster's Redux/toolkit structure
- OpenAPI specifications MUST be maintained in `src/main/resources/swagger/api.yml`
- API-first development: update OpenAPI spec, run `./mvnw generate-sources`, then implement delegate classes

**Rationale**: JHipster provides a production-ready, opinionated stack that ensures consistency, maintainability, and rapid development. Deviating from conventions introduces technical debt and reduces team velocity.

---

### III. Role-Based Governance (RBAC)

**Rules**:

- Three roles MUST be enforced at all layers: `TRADER`, `BROKER_ADMIN`, `EXCHANGE_OPERATOR`
- Every REST endpoint MUST declare `@PreAuthorize` or equivalent role guards
- Database queries MUST scope results by user's organization (broker/exchange) where applicable
- WebSocket subscriptions MUST validate JWT and enforce role-based topic access
- UI components MUST conditionally render based on user role
- Audit logs MUST capture role, user ID, and action for all state-changing operations

**Role Hierarchy**:

- **TRADER**: Can view/manage only own account, orders, positions, ledger
- **BROKER_ADMIN**: Can view/manage all traders under their broker; cannot access other brokers
- **EXCHANGE_OPERATOR**: Full system authority; can view/manage all brokers, traders, and system settings

**Rationale**: RNExchange simulates a hierarchical brokerage ecosystem. Strict RBAC prevents unauthorized access, data leakage, and privilege escalation—critical for educational trust and realistic governance modeling.

---

### IV. Real-Time Architecture

**Rules**:

- Market data (ticks, OHLC) MUST be broadcast via Spring WebSocket (STOMP)
- Order status updates MUST push to traders via WebSocket topics (`/topic/orders/{userId}`)
- Portfolio MTM updates MUST refresh in real-time (2–5 second intervals)
- WebSocket handshake MUST validate JWT tokens
- Client reconnection logic MUST be implemented with exponential backoff
- Mock market data generator MUST be configurable (frequency, volatility, latency)
- Kite API integration MUST support failover to mock feed on disconnection (post-MVP)

**Rationale**: Real-time feedback is essential for a trading simulator to feel authentic. WebSocket architecture provides low-latency, bidirectional communication for market data and order updates, replicating live market conditions.

---

### V. Educational Transparency

**Rules**:

- All market data displays MUST show "SIMULATED" or "DELAYED" badges prominently
- UI MUST include tooltips explaining training/educational context
- Terms of Use MUST clarify no real money is involved
- Reports and statements MUST include disclaimer: "This is a simulated environment"
- Error messages MUST be human-readable and educational (e.g., "Invalid price – must be multiple of ₹0.05")
- Documentation MUST emphasize learning objectives

**Rationale**: RNExchange is a learning and experimentation platform. Transparency prevents confusion, builds trust, and reinforces the educational mission. Users must never mistake simulated trading for real financial transactions.

---

### VI. Domain-Driven Design (DDD)

**Rules**:

- Core domain entities MUST be modeled in JDL: `Exchange`, `Broker`, `TraderProfile`, `TradingAccount`, `Instrument`, `Contract`, `Order`, `Execution`, `Position`, `Lot`, `LedgerEntry`, `SettlementBatch`, `MarginRule`, `RiskAlert`
- Business logic MUST reside in domain services, not controllers or repositories
- Aggregates MUST enforce invariants (e.g., margin checks before order placement)
- Value objects MUST be immutable (e.g., `Money`, `Price`, `Quantity`)
- Domain events MUST be published for significant state changes (e.g., `OrderFilledEvent`, `SettlementCompletedEvent`)
- Ubiquitous language MUST be used consistently across code, tests, and documentation (e.g., "MTM" = Mark-to-Market, "EOD" = End-of-Day)

**Rationale**: RNExchange models a complex financial domain with intricate workflows (trading, risk, settlement). DDD ensures the codebase reflects the real-world domain, making it easier to understand, extend, and maintain.

---

### VII. API-First Development

**Rules**:

- All REST APIs MUST be defined in OpenAPI 3.0 (`src/main/resources/swagger/api.yml`) before implementation
- API changes MUST update the OpenAPI spec first
- Code generation MUST be triggered via `./mvnw generate-sources`
- Generated delegate interfaces MUST be implemented by `@Service` classes
- API documentation MUST be auto-published and accessible via Swagger UI
- Breaking changes MUST increment API version (e.g., `/api/v1` → `/api/v2`)

**Rationale**: API-first development ensures frontend and backend teams can work in parallel, contracts are explicit, and API consumers have clear, versioned documentation. This is critical for a multi-role platform with distinct UIs.

---

## Technical Standards

### Code Quality

- Linting: ESLint (frontend), Checkstyle (backend) MUST pass with no errors
- Formatting: Prettier (frontend), Spring Java Format (backend) MUST be applied pre-commit
- Static Analysis: SonarQube MUST show zero P1 issues; code smells MUST be addressed
- Code Reviews: All PRs MUST be reviewed by at least one team member before merge

### Performance Targets

- Order placement latency: <250 ms (p95)
- WebSocket tick broadcast: 10,000 updates/sec peak capacity
- Concurrent users: 1,000+ traders without degradation
- EOD settlement: Complete for 10,000 positions within 5 minutes

### Security

- Authentication: JWT with short-lived tokens (15 min access, 7 day refresh)
- Authorization: Role-based access enforced at service layer
- HTTPS/TLS: Mandatory in all non-development environments
- Secrets Management: Kite API keys and DB credentials MUST be externalized (environment variables, Vault)
- Audit Logging: All admin/broker/trader actions MUST be logged with user ID, role, timestamp, action

---

## Development Workflow

### Feature Development Process

1. **Specification**: Create feature spec in `/specs/[###-feature-name]/spec.md` (user stories, acceptance criteria)
2. **Planning**: Generate implementation plan via `/speckit.plan` (research, data model, contracts, tasks)
3. **TDD Cycle**:
   - Write failing tests (contract, integration, unit)
   - Implement minimum code to pass tests
   - Refactor for clarity and maintainability
4. **Review**: Submit PR with tests, implementation, and updated docs
5. **CI/CD**: All tests MUST pass; SonarQube MUST be clean; deploy to staging
6. **Validation**: Product owner validates against acceptance criteria

### Testing Strategy

- **Contract Tests**: API endpoint validation (request/response schemas)
- **Integration Tests**: End-to-end user journeys (Cucumber/Jest)
- **Unit Tests**: Service and utility logic (JUnit5/Jest)
- **Performance Tests**: Load testing with Gatling (1,000 concurrent users)
- **UI Tests**: Critical paths with Cypress (login, order placement, portfolio view)

### Branching & Commits

- Feature branches: `[###-feature-name]` (e.g., `001-market-watch`, `002-order-ticket`)
- Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`
- Commit frequency: After each task or logical unit (small, atomic commits)
- No force-push to `main`; use rebase for clean history

---

## Milestone Alignment

RNExchange follows a six-milestone roadmap (M0–M6):

- **M0 — Foundations**: JHipster scaffold, JWT auth, Liquibase, CI/CD
- **M1 — Market Data Mock**: WebSocket ticks, 1-min bars, watchlists
- **M2 — Trading Core**: Order matching, positions, P&L, ledger, margin checks
- **M3 — Broker Portal**: Client management, funds journal, risk monitor, EOD per broker
- **M4 — Exchange Console**: Broker lifecycle, holidays, settlement overrides
- **M5 — Kite Integration**: Real market data feed, live EOD, latency metrics
- **M6 — QA & Launch**: Regression, load testing, pilot rollout

All features MUST align with the active milestone; no cross-milestone dependencies without explicit approval.

---

## Governance

### Amendment Procedure

- Constitution changes MUST be proposed via PR to `.specify/memory/constitution.md`
- Changes MUST include rationale and impact analysis
- Approval requires consensus from tech lead and product owner
- Version MUST be incremented per semantic versioning:
  - **MAJOR**: Backward-incompatible principle removals or redefinitions
  - **MINOR**: New principle or materially expanded guidance
  - **PATCH**: Clarifications, wording, typo fixes

### Compliance & Enforcement

- All PRs MUST verify compliance with constitution principles
- Weekly reviews: Team discusses adherence and proposes improvements
- Constitution violations MUST be justified in `/specs/[feature]/plan.md` (Complexity Tracking section)
- Persistent violations trigger retrospective and principle refinement

### Runtime Guidance

- For day-to-day development best practices, consult `README.md` and JHipster documentation
- For agent-assisted development, this constitution provides the authoritative ruleset

---

**Version**: 1.0.0 | **Ratified**: 2025-11-12 | **Last Amended**: 2025-11-12
