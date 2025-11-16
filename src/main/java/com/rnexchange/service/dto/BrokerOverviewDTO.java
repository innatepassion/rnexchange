package com.rnexchange.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Phase 2 (T009): Overview metrics for Broker Dashboard.
 */
public class BrokerOverviewDTO implements Serializable {

    private long activeTraderCount;
    private BigDecimal totalCash;
    private BigDecimal totalEquityExposure;
    private Instant generatedAt;

    public long getActiveTraderCount() {
        return activeTraderCount;
    }

    public void setActiveTraderCount(long activeTraderCount) {
        this.activeTraderCount = activeTraderCount;
    }

    public BigDecimal getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(BigDecimal totalCash) {
        this.totalCash = totalCash;
    }

    public BigDecimal getTotalEquityExposure() {
        return totalEquityExposure;
    }

    public void setTotalEquityExposure(BigDecimal totalEquityExposure) {
        this.totalEquityExposure = totalEquityExposure;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
