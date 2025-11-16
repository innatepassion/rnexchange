# Research — M3 Broker Back Office

## Unknowns and Decisions

### 1) ε constant for utilization clamp

- Decision: ε = 1.0 (currency units)
- Rationale: Simple, domain-agnostic floor to prevent divide-by-zero; avoids unstable ratios when equity ≈ 0.
- Alternatives considered: ε = 0.01 (too small, noise-sensitive), dynamic ε based on notional (adds complexity without clear benefit).

### 2) Price freshness enforcement for exposure (≤ 1 minute)

- Decision: For each instrument’s last price, require timestamp within 60 seconds of request time; otherwise treat price as stale and mark account’s exposure for that instrument as 0 with a “stale” indicator in snapshot diagnostics.
- Rationale: Meets requirement without blocking entire snapshot; avoids mixing stale prices with fresh ones while keeping UI responsive.
- Alternatives considered: Fail snapshot if any stale price (harsh UX), fetch on-demand from external feed (adds latency and coupling in M3).

### 3) Unrealized P&L source for equity = cash + UPL

- Decision: Reuse existing M2 position valuation service used by portfolio views; compute UPL from positions × (last price − cost basis) with the same rounding rules already shipped.
- Rationale: Consistency with M2; zero extra risk logic introduced in M3.
- Alternatives considered: Recompute bespoke UPL in M3 (duplication), cache-only approach (inconsistency risk).

### 4) Idempotency strategy for journals

- Decision: Require `Idempotency-Key` request header; persist tokens in a dedicated table `idempotency_token` with unique constraint `(broker_id, trading_account_id, token)` and reference to resulting ledger entry.
- Rationale: Decouples dedup from ledger schema; safer for retries and auditing without requiring schema changes to LedgerEntry.
- Alternatives considered: Add `idempotency_key` column to `ledger_entry` (schema coupling, migration risk), in-memory cache (not durable across nodes).

### 5) Journal entry types and audit mapping

- Decision: Introduce ledger types `JOURNAL_CREDIT` and `JOURNAL_DEBIT`; record timestamp, broker admin userId, trading account, direction, amount, free-text reason, and idempotency token reference.
- Rationale: Clear separation from trade/settlement entries; satisfies auditing and reconciliation needs.
- Alternatives considered: Overload existing deposit/withdrawal types (ambiguous), extend reason codes only (insufficient for analytics).

### 6) UI handling of zero positions and zero cash

- Decision: If exposure = 0 and equity ≤ ε, utilization = 0%; if exposure > 0 and equity ≈ 0, utilization approaches 100% but is clamped to 100%.
- Rationale: Matches spec language and avoids divide-by-zero; keeps ranking meaningful.
- Alternatives considered: Hide rows with zero equity (confusing to operators), custom tiers (adds complexity).

## Best Practices Recap

- JHipster 8 + API-first: update `src/main/resources/swagger/api.yml` first; generate sources; implement delegates/services.
- RBAC: `@PreAuthorize('hasRole(\"BROKER_ADMIN\")')` on resources and broker scoping at repository layer via joins/filters.
- Idempotency: Header-based token, persisted, at-most-once semantics; return original response for duplicates.
- Observability: Include correlation/idempotency IDs in logs; audit all journal actions.

## Result

All items initially marked NEEDS CLARIFICATION are resolved above and feed into Phase 1 artifacts.
