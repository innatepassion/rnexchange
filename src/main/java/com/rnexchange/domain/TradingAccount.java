package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.AccountType;
import com.rnexchange.domain.enumeration.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A TradingAccount.
 */
@Entity
@Table(name = "trading_account")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TradingAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AccountType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "base_ccy", nullable = false)
    private Currency baseCcy;

    @NotNull
    @Column(name = "balance", precision = 21, scale = 2, nullable = false)
    private BigDecimal balance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "exchange" }, allowSetters = true)
    private Broker broker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private TraderProfile trader;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public TradingAccount id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountType getType() {
        return this.type;
    }

    public TradingAccount type(AccountType type) {
        this.setType(type);
        return this;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public Currency getBaseCcy() {
        return this.baseCcy;
    }

    public TradingAccount baseCcy(Currency baseCcy) {
        this.setBaseCcy(baseCcy);
        return this;
    }

    public void setBaseCcy(Currency baseCcy) {
        this.baseCcy = baseCcy;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public TradingAccount balance(BigDecimal balance) {
        this.setBalance(balance);
        return this;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return this.status;
    }

    public TradingAccount status(AccountStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Broker getBroker() {
        return this.broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public TradingAccount broker(Broker broker) {
        this.setBroker(broker);
        return this;
    }

    public TraderProfile getTrader() {
        return this.trader;
    }

    public void setTrader(TraderProfile traderProfile) {
        this.trader = traderProfile;
    }

    public TradingAccount trader(TraderProfile traderProfile) {
        this.setTrader(traderProfile);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TradingAccount)) {
            return false;
        }
        return getId() != null && getId().equals(((TradingAccount) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TradingAccount{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", baseCcy='" + getBaseCcy() + "'" +
            ", balance=" + getBalance() +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
