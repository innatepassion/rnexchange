package com.rnexchange.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StatementSummary implements Serializable {

    private Long id;
    private LocalDate refDate;
    private Long tradingAccountId;
    private String tradingAccountLabel;
    private BigDecimal openingBalance;
    private BigDecimal netCashFlows;
    private BigDecimal eodMtmPnl;
    private BigDecimal closingBalance;
    private String htmlUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRefDate() {
        return refDate;
    }

    public void setRefDate(LocalDate refDate) {
        this.refDate = refDate;
    }

    public Long getTradingAccountId() {
        return tradingAccountId;
    }

    public void setTradingAccountId(Long tradingAccountId) {
        this.tradingAccountId = tradingAccountId;
    }

    public String getTradingAccountLabel() {
        return tradingAccountLabel;
    }

    public void setTradingAccountLabel(String tradingAccountLabel) {
        this.tradingAccountLabel = tradingAccountLabel;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getNetCashFlows() {
        return netCashFlows;
    }

    public void setNetCashFlows(BigDecimal netCashFlows) {
        this.netCashFlows = netCashFlows;
    }

    public BigDecimal getEodMtmPnl() {
        return eodMtmPnl;
    }

    public void setEodMtmPnl(BigDecimal eodMtmPnl) {
        this.eodMtmPnl = eodMtmPnl;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
}
