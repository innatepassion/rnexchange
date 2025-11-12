package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rnexchange.domain.enumeration.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A LedgerEntry.
 */
@Entity
@Table(name = "ledger_entry")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LedgerEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "ts", nullable = false)
    private Instant ts;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ccy", nullable = false)
    private Currency ccy;

    @Column(name = "balance_after", precision = 21, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "reference")
    private String reference;

    @Column(name = "remarks")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "broker", "trader" }, allowSetters = true)
    private TradingAccount tradingAccount;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public LedgerEntry id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTs() {
        return this.ts;
    }

    public LedgerEntry ts(Instant ts) {
        this.setTs(ts);
        return this;
    }

    public void setTs(Instant ts) {
        this.ts = ts;
    }

    public String getType() {
        return this.type;
    }

    public LedgerEntry type(String type) {
        this.setType(type);
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public LedgerEntry amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCcy() {
        return this.ccy;
    }

    public LedgerEntry ccy(Currency ccy) {
        this.setCcy(ccy);
        return this;
    }

    public void setCcy(Currency ccy) {
        this.ccy = ccy;
    }

    public BigDecimal getBalanceAfter() {
        return this.balanceAfter;
    }

    public LedgerEntry balanceAfter(BigDecimal balanceAfter) {
        this.setBalanceAfter(balanceAfter);
        return this;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getReference() {
        return this.reference;
    }

    public LedgerEntry reference(String reference) {
        this.setReference(reference);
        return this;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public LedgerEntry remarks(String remarks) {
        this.setRemarks(remarks);
        return this;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public TradingAccount getTradingAccount() {
        return this.tradingAccount;
    }

    public void setTradingAccount(TradingAccount tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    public LedgerEntry tradingAccount(TradingAccount tradingAccount) {
        this.setTradingAccount(tradingAccount);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LedgerEntry)) {
            return false;
        }
        return getId() != null && getId().equals(((LedgerEntry) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LedgerEntry{" +
            "id=" + getId() +
            ", ts='" + getTs() + "'" +
            ", type='" + getType() + "'" +
            ", amount=" + getAmount() +
            ", ccy='" + getCcy() + "'" +
            ", balanceAfter=" + getBalanceAfter() +
            ", reference='" + getReference() + "'" +
            ", remarks='" + getRemarks() + "'" +
            "}";
    }
}
