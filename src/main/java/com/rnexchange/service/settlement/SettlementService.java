package com.rnexchange.service.settlement;

import java.time.LocalDate;

/**
 * Domain service for running end-of-day (EOD) settlement batches.
 */
public interface SettlementService {
    /**
     * Run the end-of-day settlement for the given trade date.
     *
     * @param tradeDate the trade date to settle (YYYY-MM-DD)
     */
    void runEod(LocalDate tradeDate);
}
