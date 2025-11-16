package com.rnexchange.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BrokerSettlementSummary implements Serializable {

    private LocalDate refDate;
    private Long brokerId;
    private String brokerCode;
    private Integer accountsProcessed;
    private Integer positionsProcessed;
    private BigDecimal totalNetPnl;
    private String summaryUrl;

    public LocalDate getRefDate() {
        return refDate;
    }

    public void setRefDate(LocalDate refDate) {
        this.refDate = refDate;
    }

    public Long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }

    public String getBrokerCode() {
        return brokerCode;
    }

    public void setBrokerCode(String brokerCode) {
        this.brokerCode = brokerCode;
    }

    public Integer getAccountsProcessed() {
        return accountsProcessed;
    }

    public void setAccountsProcessed(Integer accountsProcessed) {
        this.accountsProcessed = accountsProcessed;
    }

    public Integer getPositionsProcessed() {
        return positionsProcessed;
    }

    public void setPositionsProcessed(Integer positionsProcessed) {
        this.positionsProcessed = positionsProcessed;
    }

    public BigDecimal getTotalNetPnl() {
        return totalNetPnl;
    }

    public void setTotalNetPnl(BigDecimal totalNetPnl) {
        this.totalNetPnl = totalNetPnl;
    }

    public String getSummaryUrl() {
        return summaryUrl;
    }

    public void setSummaryUrl(String summaryUrl) {
        this.summaryUrl = summaryUrl;
    }
}
