package com.rnexchange.service.settlement;

import com.rnexchange.service.dto.BrokerSettlementSummary;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for aggregating broker-level settlement summaries.
 */
public interface BrokerSettlementService {
    /**
     * Get broker-level settlement summaries for the authenticated broker admin.
     *
     * @param fromDate optional start date (inclusive)
     * @param toDate optional end date (inclusive)
     * @return list of broker settlement summaries
     */
    List<BrokerSettlementSummary> getBrokerSettlements(LocalDate fromDate, LocalDate toDate);

    /**
     * Get HTML summary for a specific broker and date.
     *
     * @param brokerId the broker ID
     * @param refDate the reference date
     * @return HTML content
     */
    String getBrokerSummaryHtml(Long brokerId, LocalDate refDate);
}
