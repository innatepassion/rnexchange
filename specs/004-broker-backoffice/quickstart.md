# Quickstart — M3 Broker Back Office

## Overview

Broker-scoped views and actions for Broker Admins:

- Broker Dashboard: overview metrics + ranked utilization
- Clients: list traders, view details, submit funds journals

## Prerequisites

- Branch: `004-broker-backoffice`
- API contracts: `specs/004-broker-backoffice/contracts/broker-backoffice.openapi.yaml`
- Constitution: `.specify/memory/constitution.md`

## Develop (API-first & TDD)

1. Update `src/main/resources/swagger/api.yml` with broker endpoints from the contracts file.
2. Generate sources:
   ```bash
   ./mvnw generate-sources
   ```
3. Write failing contract and integration tests:
   - Backend: `src/test/java/.../contract` and `.../integration`
   - Ensure RBAC `BROKER_ADMIN` and broker scoping are enforced.
4. Implement delegates/services/repositories.
5. Make tests pass (red → green → refactor).

## Endpoints (summary)

- GET `/api/broker/overview`
- GET `/api/broker/traders?page={p}&size={s}`
- GET `/api/broker/traders/{traderId}`
- POST `/api/broker/traders/{tradingAccountId}/journal` (header `Idempotency-Key`)

## Journal Rules

- Direction: `credit` or `debit`, amount > 0, non-empty reason
- At-most-once per `(broker, account, Idempotency-Key)`
- Debits can make balance negative (M3)

## Utilization

- `equity = cash + UPL`
- `exposure = Σ |qty × last_price|`
- `utilization = clamp(exposure / max(equity, ε=1.0), 0, 1) × 100`
- Prices older than 60s marked stale and excluded

## UI

- Add module under `src/main/webapp/app/modules/broker/`
- Views: Dashboard, Clients list, Trader details + Journal drawer
- Role guard: `BROKER_ADMIN`

## Run

```bash
./mvnw
npm run start
```

Docker Compose (optional):

```bash
docker compose -f src/main/docker/app.yml up -d
```

## Tests

```bash
./mvnw test
npm run test
npm run cypress:open
```
