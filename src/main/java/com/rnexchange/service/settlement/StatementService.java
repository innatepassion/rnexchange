package com.rnexchange.service.settlement;

import com.rnexchange.domain.TradingAccount;
import com.rnexchange.service.dto.StatementSummary;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for assembling per-account daily statements and related views.
 */
public interface StatementService {
    /**
     * Get all statements for the authenticated trader.
     *
     * @param fromDate optional start date (inclusive)
     * @param toDate optional end date (inclusive)
     * @return list of statement summaries
     */
    List<StatementSummary> getStatementsForTrader(LocalDate fromDate, LocalDate toDate);

    /**
     * Get HTML statement for a specific statement ID.
     *
     * @param statementId the statement identifier
     * @return HTML content
     */
    String getStatementHtml(Long statementId);

    /**
     * Build a statement summary for a specific account and date.
     * Used by BrokerSettlementService to aggregate broker-level summaries.
     *
     * @param account the trading account
     * @param refDate the reference date
     * @return statement summary, or null if no data exists for that date
     */
    StatementSummary buildStatementSummary(TradingAccount account, LocalDate refDate);
}
