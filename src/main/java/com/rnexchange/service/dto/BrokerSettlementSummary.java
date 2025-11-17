package com.rnexchange.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BrokerSettlementSummary implements Serializable {

    private LocalDate refDate;
    private Long brokerId;
    private String brokerName;
    private Integer totalClientCount;
    private BigDecimal totalOpeningBalance;
    private BigDecimal totalClosingBalance;
    private BigDecimal totalEodMtmPnl;
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

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public Integer getTotalClientCount() {
        return totalClientCount;
    }

    public void setTotalClientCount(Integer totalClientCount) {
        this.totalClientCount = totalClientCount;
    }

    public BigDecimal getTotalOpeningBalance() {
        return totalOpeningBalance;
    }

    public void setTotalOpeningBalance(BigDecimal totalOpeningBalance) {
        this.totalOpeningBalance = totalOpeningBalance;
    }

    public BigDecimal getTotalClosingBalance() {
        return totalClosingBalance;
    }

    public void setTotalClosingBalance(BigDecimal totalClosingBalance) {
        this.totalClosingBalance = totalClosingBalance;
    }

    public BigDecimal getTotalEodMtmPnl() {
        return totalEodMtmPnl;
    }

    public void setTotalEodMtmPnl(BigDecimal totalEodMtmPnl) {
        this.totalEodMtmPnl = totalEodMtmPnl;
    }

    public String getSummaryUrl() {
        return summaryUrl;
    }

    public void setSummaryUrl(String summaryUrl) {
        this.summaryUrl = summaryUrl;
    }
}
