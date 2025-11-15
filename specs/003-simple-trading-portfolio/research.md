# Research: Simple Trading & Portfolio (M2)

**Feature**: `003-simple-trading-portfolio`  
**Date**: 2025-11-15

For this milestone the specification is already tightly scoped and aligned with the RNExchange constitution, so only light research and confirmation are required.

## Decisions

### 1. Keep architecture within existing JHipster monolith

- **Decision**: Implement trading logic inside the existing Spring Boot + React JHipster application, without introducing new microservices or separate deployable components.
- **Rationale**: Keeps complexity low for M2, simplifies data access to TradingAccount/Instrument, and fits JHipster conventions.
- **Alternatives considered**:
  - Separate trading microservice – rejected as over-engineering for a simple cash-only loop.
  - Event-sourced trading core – rejected as unnecessary for this educational MVP.

### 2. Use simple average-cost and cash ledger calculations

- **Decision**: Use straightforward average-cost calculations and a single cash ledger entry per execution as already described in the spec.
- **Rationale**: Sufficient to demonstrate the full loop and P&L behavior without full broker-grade accounting.
- **Alternatives considered**:
  - FIFO/LIFO inventory methods – rejected for now; would add complexity without additional learning value.
  - Multi-currency and complex fee models – deferred to later milestones.

### 3. Leverage existing WebSocket and RBAC setup

- **Decision**: Reuse the existing WebSocket/STOMP infrastructure and role-based access checks; emit minimal messages on `/topic/orders.{tradingAccountId}` and `/topic/executions.{tradingAccountId}` and let the frontend refetch data.
- **Rationale**: Aligns with constitution’s real-time architecture while minimizing custom client-side state or protocol.
- **Alternatives considered**:
  - Rich, stateful client-side order book and streaming MTM – rejected as over-engineering for M2.
  - Per-field streaming deltas – rejected; list refetch is simpler and good enough here.
