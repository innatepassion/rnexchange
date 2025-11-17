package com.rnexchange.service.settlement.event;

import java.time.LocalDate;

/**
 * Domain event published when an EOD settlement batch fails.
 */
public class SettlementFailedEvent {

    private final Long batchId;
    private final LocalDate tradeDate;
    private final String errorMessage;

    public SettlementFailedEvent(Long batchId, LocalDate tradeDate, String errorMessage) {
        this.batchId = batchId;
        this.tradeDate = tradeDate;
        this.errorMessage = errorMessage;
    }

    public Long getBatchId() {
        return batchId;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
