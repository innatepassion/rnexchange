# Research — M5 Settlement & Reporting

## Unknowns and Decisions

### 1) Granularity of EOD MTM ledger entries

- Decision: Post a single net variation P&L ledger entry per trading account per EOD run, using distinct ledger types (for example, `EOD_MTM_CREDIT` and `EOD_MTM_DEBIT`) to indicate direction.
- Rationale: Keeps ledgers readable and reconciliable while still allowing the statement and reports layer to break out per-position P&L using position snapshots; avoids creating many small ledger rows per instrument.
- Alternatives considered: One ledger entry per position/instrument (too noisy for accounts with many symbols), embedding MTM only in position fields with no ledger entry (harder to reconcile cash/equity changes day to day).

### 2) Fallback price source when DailySettlementPrice is missing

- Decision: For each active instrument on the trade date, first try `DailySettlementPrice(refDate, instrument)`; if absent, derive the settlement price from the last available 1-minute bar close for that instrument on that date in the mock market data store; if neither exists, treat it as a hard error and fail the batch.
- Rationale: Respects the spec’s preference for daily settlement prices but keeps EOD self-contained and internal by falling back to existing mock bars; failing the batch when no internal prices exist prevents silently inconsistent P&L.
- Alternatives considered: Skipping such instruments and marking them as “unpriced” (would produce partial and misleading batch results), reusing previous day’s settlement price (would blur day boundaries and complicate reconciliation).

### 3) Re-running EOD for a given date

- Decision: Allow multiple `SettlementBatch` rows for the same `refDate` and `kind = EOD`, but treat the latest successful one as authoritative and make the EOD job idempotent per date by logically replacing prior EOD MTM ledger entries and reports for that date (e.g., by identifying prior EOD entries by type and date and superseding or reversing them).
- Rationale: Matches the spec’s “Re-run batch for this date” behavior while preserving an audit trail of prior runs; ensuring that only one set of EOD MTM entries is active per date keeps ledgers and statements deterministic.
- Alternatives considered: Hard-disallowing re-runs (would make data correction impossible), allowing multiple active EOD MTM entries for a date (would double-count P&L and break reconciliation).

### 4) Storage model for statements and broker summaries

- Decision: Store a `ReportLink`-style record per generated statement or broker summary (e.g., fields for trade date, report type, tradingAccountId or brokerId, and a stable relative URL) rather than persisting raw HTML blobs or file-system paths; the URL will route to a server-side view that renders HTML on demand from ledger/position data at that date.
- Rationale: Keeps persistence simple and avoids filesystem complexity, while still giving the UI a stable link to show in “Statements” and “Settlements/Reports” lists; avoids duplicating report content if underlying data needs to be corrected and batch re-run.
- Alternatives considered: Writing static HTML files to disk and storing filesystem paths (adds infra concerns and deployment complexity), storing full HTML in the database (bloats the DB and makes evolution harder).

### 5) Scope of EOD batch workload and performance envelope

- Decision: Target EOD batch workloads of up to ~10,000 open positions across all brokers and traders per run, processed by a single in-app batch service that streams positions and posts net MTM entries per account, aiming to complete within 5 minutes as per the constitution’s EOD performance guidance.
- Rationale: Aligns with RNExchange performance targets while keeping the implementation simple (no separate batch worker or distributed job framework) and sufficient for the simulator’s educational scale.
- Alternatives considered: Introducing a dedicated job scheduler/distributed batch framework (overkill for this milestone), limiting batch size to smaller subsets by broker (would complicate the “single EOD for the day” mental model).

## Best Practices Recap

- Use API-first workflow: define settlement and reporting endpoints in this feature’s OpenAPI contracts and merge them into `src/main/resources/swagger/api.yml` before generating delegates.
- Enforce RBAC rigorously: `EXCHANGE_OPERATOR` only for batch run/list, `BROKER_ADMIN` scoped to their broker’s summaries, and `TRADER` scoped to their own statements.
- Keep settlement domain logic in dedicated services under `service/settlement` and avoid complex calculations in controllers.
- Log each EOD batch with batch id, refDate, counts (positions, accounts), totals (net MTM), and failure reasons to support investigation.

## Result

All relevant design decisions for M5 Settlement & Reporting are documented above and feed into the Phase 1 data model, contracts, and quickstart artifacts. No outstanding NEEDS CLARIFICATION items remain for this feature.
