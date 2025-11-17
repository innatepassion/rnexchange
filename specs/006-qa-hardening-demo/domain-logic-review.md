# Domain Logic Review - M6 Phase 8 (T060)

**Date**: 2025-01-17  
**Purpose**: Review and identify areas for refactoring in cross-cutting domain logic for trades, ledger, and statements.

## Summary

A review of the trading, ledger, and settlement service layers was conducted to identify opportunities for clarity improvements and duplication reduction. The codebase follows good separation of concerns, but several areas were identified for potential improvement.

## Areas Reviewed

### 1. Trading Services (`src/main/java/com/rnexchange/service/trading/`)

**Current State**:

- `TradingService.java` - Core trading operations
- `BuyingPowerService.java` - Buying power calculations
- `TraderAuditStructuredLogger.java` - Audit logging

**Observations**:

- ✅ Good separation: Trading logic is isolated from ledger/account updates
- ✅ Buying power calculation is properly extracted to a dedicated service
- ⚠️ **Potential improvement**: Order validation logic is scattered between `OrderService` and `TradingService`
- ⚠️ **Potential improvement**: Position update logic could be centralized

**Recommendations**:

- Consider extracting order validation into a dedicated `OrderValidationService`
- Centralize position update logic in `PositionService` to avoid duplication

### 2. Ledger Services (`src/main/java/com/rnexchange/service/`)

**Current State**:

- `LedgerEntryService.java` - CRUD operations for ledger entries
- `LedgerEntryQueryService.java` - Query operations
- `TradingAccountService.java` - Account balance management

**Observations**:

- ✅ M6 enhancements properly handle negative balances (FR-004)
- ✅ Balance updates are correctly scoped to journal entries
- ⚠️ **Potential improvement**: Balance calculation logic appears in multiple places:
  - `LedgerEntryService.save()` updates balance
  - `TradingAccountService` may also update balance
  - `BrokerJournalService` updates balance
- ⚠️ **Potential improvement**: Running balance calculation could be extracted to a shared utility

**Recommendations**:

- Extract balance update logic to a shared `BalanceUpdateService` or utility
- Ensure all balance updates go through a single code path for consistency
- Consider using domain events for balance updates to decouple services

### 3. Settlement Services (`src/main/java/com/rnexchange/service/settlement/`)

**Current State**:

- `SettlementService.java` / `SettlementServiceImpl.java` - EOD processing
- `StatementService.java` - Statement generation
- `BrokerSettlementService.java` - Broker-specific settlement operations

**Observations**:

- ✅ EOD idempotency is properly implemented (FR-005)
- ✅ Statement generation is separated from EOD processing
- ⚠️ **Potential improvement**: MTM (Mark-to-Market) calculation logic may be duplicated:
  - EOD settlement calculates MTM
  - Position service may also calculate MTM
- ⚠️ **Potential improvement**: Statement reconciliation logic could be extracted

**Recommendations**:

- Extract MTM calculation to a shared `MarkToMarketService`
- Create a `StatementReconciliationService` for balance/P&L reconciliation logic
- Consider using a strategy pattern for different statement types (trader vs broker)

## Duplication Analysis

### Identified Duplications

1. **Balance Update Logic**

   - Found in: `LedgerEntryService`, `BrokerJournalService`, potentially `TradingAccountService`
   - **Action**: Extract to shared service or utility method

2. **Position Update Logic**

   - Found in: `TradingService`, `PositionService`, potentially `SettlementService`
   - **Action**: Centralize in `PositionService` with clear interfaces

3. **MTM Calculation**

   - Found in: `SettlementService`, potentially `PositionService`
   - **Action**: Extract to dedicated `MarkToMarketService`

4. **Account Balance Queries**
   - Found in: Multiple services query `TradingAccountRepository` directly
   - **Action**: Use `TradingAccountService` as the single source of truth

## Clarity Improvements

### Documentation

- ✅ Services have good JavaDoc comments
- ⚠️ **Improvement**: Add more examples in JavaDoc for complex methods
- ⚠️ **Improvement**: Document the flow of balance updates across services

### Method Naming

- ✅ Methods follow clear naming conventions
- ⚠️ **Improvement**: Some methods could be more descriptive (e.g., `save()` vs `saveAndUpdateBalance()`)

### Error Handling

- ✅ Exceptions are properly typed
- ⚠️ **Improvement**: Consider creating domain-specific exceptions for clearer error messages

## Priority Recommendations

### High Priority

1. Extract balance update logic to prevent inconsistencies
2. Centralize position update logic
3. Extract MTM calculation to shared service

### Medium Priority

4. Create domain events for balance/position updates
5. Improve documentation with flow diagrams
6. Add integration tests for cross-service interactions

### Low Priority

7. Refactor method names for clarity
8. Create domain-specific exceptions
9. Add more JavaDoc examples

## Conclusion

The domain logic is well-structured with good separation of concerns. The main areas for improvement are:

- Reducing duplication in balance and position update logic
- Extracting shared calculations (MTM, running balance)
- Improving documentation of cross-service interactions

These improvements can be implemented incrementally without breaking existing functionality.
