package com.rnexchange.service.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO representing an account summary with balance, buying power, and risk flags.
 * M6 User Story 2 (T028): Exposes negative/at-risk balance flags for broker and trader UIs.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AccountSummaryDTO implements Serializable {

    private Long tradingAccountId;
    private BigDecimal balance;
    private BigDecimal buyingPower;
    private Boolean isNegative;
    private Boolean isAtRisk;
    private String riskReason;

    public AccountSummaryDTO() {}

    public AccountSummaryDTO(Long tradingAccountId, BigDecimal balance, BigDecimal buyingPower) {
        this.tradingAccountId = tradingAccountId;
        this.balance = balance;
        this.buyingPower = buyingPower;
        this.isNegative = balance != null && balance.signum() < 0;
        this.isAtRisk = this.isNegative; // At risk if negative; can be extended with margin rules
        this.riskReason = this.isNegative ? "Account balance is negative" : null;
    }

    public Long getTradingAccountId() {
        return tradingAccountId;
    }

    public void setTradingAccountId(Long tradingAccountId) {
        this.tradingAccountId = tradingAccountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        // Recalculate flags when balance changes
        this.isNegative = balance != null && balance.signum() < 0;
        this.isAtRisk = this.isNegative;
        this.riskReason = this.isNegative ? "Account balance is negative" : null;
    }

    public BigDecimal getBuyingPower() {
        return buyingPower;
    }

    public void setBuyingPower(BigDecimal buyingPower) {
        this.buyingPower = buyingPower;
    }

    /**
     * M6 User Story 2: Flag indicating if account balance is negative.
     */
    public Boolean getIsNegative() {
        return isNegative;
    }

    public void setIsNegative(Boolean isNegative) {
        this.isNegative = isNegative;
    }

    /**
     * M6 User Story 2: Flag indicating if account is at risk (negative or limit-breaching).
     */
    public Boolean getIsAtRisk() {
        return isAtRisk;
    }

    public void setIsAtRisk(Boolean isAtRisk) {
        this.isAtRisk = isAtRisk;
    }

    /**
     * M6 User Story 2: Reason for at-risk status (e.g., "Account balance is negative").
     */
    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountSummaryDTO)) {
            return false;
        }
        AccountSummaryDTO that = (AccountSummaryDTO) o;
        return Objects.equals(tradingAccountId, that.tradingAccountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradingAccountId);
    }

    @Override
    public String toString() {
        return (
            "AccountSummaryDTO{" +
            "tradingAccountId=" +
            tradingAccountId +
            ", balance=" +
            balance +
            ", buyingPower=" +
            buyingPower +
            ", isNegative=" +
            isNegative +
            ", isAtRisk=" +
            isAtRisk +
            ", riskReason='" +
            riskReason +
            '\'' +
            '}'
        );
    }
}
