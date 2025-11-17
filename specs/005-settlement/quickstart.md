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

## EOD Usage Notes

### Running EOD Settlement

1. **Prerequisites**:

   - Ensure `DailySettlementPrice` records exist for all instruments with open positions on the target trade date
   - Verify that positions and trading accounts are properly configured
   - Ensure you are logged in as an `EXCHANGE_OPERATOR` user

2. **Running EOD**:

   ```bash
   # Via API
   curl -X POST "http://localhost:8080/api/settlements/eod?date=2025-01-15" \
     -H "Authorization: Bearer <token>"

   # Via UI
   # Navigate to Exchange > Settlement tab
   # Click "Run EOD for Today" or select a specific date
   ```

3. **Monitoring EOD Execution**:

   - Check batch status via GET `/api/settlements?from=YYYY-MM-DD&to=YYYY-MM-DD`
   - Review logs for correlation IDs (format: `[correlationId=...]`) to track request flow
   - Monitor `SettlementBatch.status` field: `CREATED` → `PROCESSED` or `FAILED`

4. **Re-running EOD**:

   - EOD can be re-run for the same date
   - Previous EOD MTM entries are automatically superseded (marked with "SUPERSEDED-" prefix)
   - Account balances are adjusted to reflect the new settlement results
   - Report links are regenerated for the latest batch

5. **Error Handling**:
   - If settlement price is missing for any instrument, the entire batch fails with status `FAILED`
   - No partial updates are applied on failure (atomic transaction)
   - Check batch `remarks` field for error details in JSON format
   - Review logs with correlation ID for detailed error trace

### Statement Access

- **Traders**: Access statements via `/api/statements` (filtered to own accounts only)
- **Broker Admins**: Access broker summaries via `/api/broker/settlements` (filtered to own broker only)
- Statements include simulated environment disclaimers in HTML output

### Performance Considerations

- EOD settlement processes all open positions in a single transaction
- For large datasets (10,000+ positions), expect processing time of up to 5 minutes
- Settlement runs synchronously; consider implementing async processing for production scale
- Use correlation IDs in logs to trace performance bottlenecks

## Documentation References

- **Specification**: `specs/005-settlement/spec.md` - Complete feature specification
- **Data Model**: `specs/005-settlement/data-model.md` - Entity relationships and validation rules
- **API Contracts**: `specs/005-settlement/contracts/settlement.openapi.yaml` - OpenAPI definitions
- **Implementation Plan**: `specs/005-settlement/plan.md` - Technical architecture and decisions
- **Tasks**: `specs/005-settlement/tasks.md` - Implementation task breakdown
- **Research**: `specs/005-settlement/research.md` - Technical research and constraints

## Related Features

- **Mock Market Data** (`specs/002-mock-market-data/`): Provides `DailySettlementPrice` data
- **Trading Portfolio** (`specs/003-simple-trading-portfolio/`): Provides `Position` and `LedgerEntry` entities
- **Broker Backoffice** (`specs/004-broker-backoffice/`): Provides broker and trader account management

## Troubleshooting

### Common Issues

1. **"No settlement price found" error**:

   - Ensure `DailySettlementPrice` records exist for the trade date
   - Check that instrument symbols match between positions and settlement prices
   - Verify instrument status is `active`

2. **RBAC access denied**:

   - Verify user has correct role (`EXCHANGE_OPERATOR`, `TRADER`, or `BROKER_ADMIN`)
   - Check that `BrokerDesk` is properly linked for broker admin users
   - Ensure `TraderProfile` is linked for trader users

3. **Statements not appearing**:

   - Verify EOD has been run for the target date
   - Check that `ReportLink` records exist for the account and date
   - Ensure user owns the trading account (for traders) or broker (for broker admins)

4. **Balance reconciliation issues**:
   - Review ledger entries for the date to verify EOD MTM entries
   - Check that opening balance + cash flows + EOD MTM = closing balance
   - Verify no duplicate EOD entries (check for "SUPERSEDED-" prefix)
