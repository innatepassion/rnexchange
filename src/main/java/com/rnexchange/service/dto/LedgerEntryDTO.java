package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.domain.enumeration.LedgerEntryType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.LedgerEntry} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LedgerEntryDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant createdAt;

    @NotNull
    private LedgerEntryType type;

    @NotNull
    private BigDecimal amount;

    private BigDecimal fee;

    @NotNull
    private Currency ccy;

    private BigDecimal balanceAfter;

    private String description;

    private String reference;

    private String remarks;

    private TradingAccountDTO tradingAccount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public LedgerEntryType getType() {
        return type;
    }

    public void setType(LedgerEntryType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public Currency getCcy() {
        return ccy;
    }

    public void setCcy(Currency ccy) {
        this.ccy = ccy;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public TradingAccountDTO getTradingAccount() {
        return tradingAccount;
    }

    public void setTradingAccount(TradingAccountDTO tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LedgerEntryDTO)) {
            return false;
        }

        LedgerEntryDTO ledgerEntryDTO = (LedgerEntryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, ledgerEntryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LedgerEntryDTO{" +
            "id=" + getId() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", type='" + getType() + "'" +
            ", amount=" + getAmount() +
            ", fee=" + getFee() +
            ", ccy='" + getCcy() + "'" +
            ", balanceAfter=" + getBalanceAfter() +
            ", description='" + getDescription() + "'" +
            ", reference='" + getReference() + "'" +
            ", remarks='" + getRemarks() + "'" +
            ", tradingAccount=" + getTradingAccount() +
            "}";
    }
}
