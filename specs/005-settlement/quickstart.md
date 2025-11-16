# Quickstart — M5 Settlement & Reporting

## Overview

M5 adds a realistic end-of-day (EOD) settlement and reporting loop:

- Exchange Operator: trigger EOD for a trade date, see batch status and counts.
- Trader: view daily statements with opening/closing balances and EOD MTM P&L.
- Broker Admin: view per-day broker-level settlement summaries and drill into client statements.

All prices come from internal mock data and `DailySettlementPrice`; no Kite/external feeds.

## Prerequisites

- Branch: `005-settlement`
- API contracts: `specs/005-settlement/contracts/settlement.openapi.yaml`
- Constitution: `.specify/memory/constitution.md`

## Develop (API-first & TDD)

1. Merge the settlement contracts into `src/main/resources/swagger/api.yml`:
   - Add `/api/settlements/eod`, `/api/settlements`, `/api/statements`, `/api/statements/{statementId}/html`, `/api/broker/settlements`.
2. Generate sources:

   ```bash
   ./mvnw generate-sources
   ```

3. Write failing tests:

   - **Contract tests** for new endpoints under `src/test/java/com/rnexchange/contract/`.
   - **Integration tests** for EOD batch behavior, ledger/position updates, and RBAC under `src/test/java/com/rnexchange/integration/`.
   - **UI tests** (Jest + Cypress) for:
     - Exchange “Settlement” tab (list + Run EOD + re-run).
     - Broker “Settlements/Reports” tab.
     - Trader “Statements” screen.

4. Implement:

   - Settlement service under `src/main/java/com/rnexchange/service/settlement/` (`runEod(tradeDate)`).
   - REST resources under `src/main/java/com/rnexchange/web/rest/settlement/`.
   - React modules:
     - Exchange Operator: `src/main/webapp/app/modules/exchange/settlement/`.
     - Broker Admin: `src/main/webapp/app/modules/broker/settlements/`.
     - Trader: `src/main/webapp/app/modules/trader/statements/`.

5. Make tests pass (red → green → refactor).

## Endpoints (summary)

- POST `/api/settlements/eod?date=YYYY-MM-DD` — EXCHANGE_OPERATOR only, trigger EOD batch.
- GET `/api/settlements?from=YYYY-MM-DD&to=YYYY-MM-DD` — EXCHANGE_OPERATOR only, list batches.
- GET `/api/statements?from=&to=` — TRADER only, list own statements.
- GET `/api/statements/{statementId}/html` — TRADER only, view HTML statement.
- GET `/api/broker/settlements?from=&to=` — BROKER_ADMIN only, list broker settlement days + summary links.

## EOD Rules (high level)

- MTM per position: `qty × (settlePrice − avgCost)` using `DailySettlementPrice` or last 1-minute bar close for the trade date.
- Net MTM per account posted as one ledger entry (`EOD_MTM_CREDIT` or `EOD_MTM_DEBIT`).
- `Position.lastPx`, `unrealizedPnl`, and `realizedPnl` updated as the official EOD snapshot.
- `SettlementBatch` row created with `refDate`, `kind = EOD`, `status` transitions `CREATED → PROCESSED` or failure.
- Re-runs for the same date replace prior EOD MTM entries and report links so results are deterministic.

## UI Notes

- Exchange Operator:
  - Add “Settlement” tab under the Exchange console.
  - Show table of batches (date, status, accountsProcessed, positionsProcessed, netPnl).
  - Provide “Run EOD for Today” and “Re-run” actions.
- Broker Admin:
  - Add “Settlements/Reports” tab.
  - Show per-day rows with broker-level summary link and an action to browse client statements.
- Trader:
  - Add “Statements” screen listing available dates and account labels with “View” link opening HTML in new tab.

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
