package com.rnexchange.service.settlement.event;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain event published when an EOD settlement batch completes successfully.
 */
public class SettlementCompletedEvent {

    private final Long batchId;
    private final LocalDate tradeDate;
    private final int accountsProcessed;
    private final int positionsProcessed;
    private final BigDecimal netPnl;

    public SettlementCompletedEvent(Long batchId, LocalDate tradeDate, int accountsProcessed, int positionsProcessed, BigDecimal netPnl) {
        this.batchId = batchId;
        this.tradeDate = tradeDate;
        this.accountsProcessed = accountsProcessed;
        this.positionsProcessed = positionsProcessed;
        this.netPnl = netPnl;
    }

    public Long getBatchId() {
        return batchId;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public int getAccountsProcessed() {
        return accountsProcessed;
    }

    public int getPositionsProcessed() {
        return positionsProcessed;
    }

    public BigDecimal getNetPnl() {
        return netPnl;
    }
}
