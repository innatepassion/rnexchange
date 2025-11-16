package com.rnexchange.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Phase 2 (T009): Lightweight summary for broker clients list/details.
 */
public class BrokerTraderSummaryDTO implements Serializable {

    private Long traderId;
    private String displayName;
    private Long tradingAccountId;
    private BigDecimal cashBalance;
    private BigDecimal unrealizedPnl;
    private BigDecimal utilizationPct;
    private boolean stalePriceFlag;

    public Long getTraderId() {
        return traderId;
    }

    public void setTraderId(Long traderId) {
        this.traderId = traderId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getTradingAccountId() {
        return tradingAccountId;
    }

    public void setTradingAccountId(Long tradingAccountId) {
        this.tradingAccountId = tradingAccountId;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public BigDecimal getUnrealizedPnl() {
        return unrealizedPnl;
    }

    public void setUnrealizedPnl(BigDecimal unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    public BigDecimal getUtilizationPct() {
        return utilizationPct;
    }

    public void setUtilizationPct(BigDecimal utilizationPct) {
        this.utilizationPct = utilizationPct;
    }

    public boolean isStalePriceFlag() {
        return stalePriceFlag;
    }

    public void setStalePriceFlag(boolean stalePriceFlag) {
        this.stalePriceFlag = stalePriceFlag;
    }
}
