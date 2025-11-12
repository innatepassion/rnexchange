package com.rnexchange.domain;

import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A Instrument.
 */
@Entity
@Table(name = "instrument")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Instrument implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "name")
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_class", nullable = false)
    private AssetClass assetClass;

    @NotNull
    @Column(name = "exchange_code", nullable = false)
    private String exchangeCode;

    @NotNull
    @Column(name = "tick_size", precision = 21, scale = 2, nullable = false)
    private BigDecimal tickSize;

    @NotNull
    @Column(name = "lot_size", nullable = false)
    private Long lotSize;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;

    @NotNull
    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Exchange exchange;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Instrument id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public Instrument symbol(String symbol) {
        this.setSymbol(symbol);
        return this;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return this.name;
    }

    public Instrument name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetClass getAssetClass() {
        return this.assetClass;
    }

    public Instrument assetClass(AssetClass assetClass) {
        this.setAssetClass(assetClass);
        return this;
    }

    public void setAssetClass(AssetClass assetClass) {
        this.assetClass = assetClass;
    }

    public String getExchangeCode() {
        return this.exchangeCode;
    }

    public Instrument exchangeCode(String exchangeCode) {
        this.setExchangeCode(exchangeCode);
        return this;
    }

    public void setExchangeCode(String exchangeCode) {
        this.exchangeCode = exchangeCode;
    }

    public BigDecimal getTickSize() {
        return this.tickSize;
    }

    public Instrument tickSize(BigDecimal tickSize) {
        this.setTickSize(tickSize);
        return this;
    }

    public void setTickSize(BigDecimal tickSize) {
        this.tickSize = tickSize;
    }

    public Long getLotSize() {
        return this.lotSize;
    }

    public Instrument lotSize(Long lotSize) {
        this.setLotSize(lotSize);
        return this;
    }

    public void setLotSize(Long lotSize) {
        this.lotSize = lotSize;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public Instrument currency(Currency currency) {
        this.setCurrency(currency);
        return this;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return this.status;
    }

    public Instrument status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Exchange getExchange() {
        return this.exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public Instrument exchange(Exchange exchange) {
        this.setExchange(exchange);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Instrument)) {
            return false;
        }
        return getId() != null && getId().equals(((Instrument) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Instrument{" +
            "id=" + getId() +
            ", symbol='" + getSymbol() + "'" +
            ", name='" + getName() + "'" +
            ", assetClass='" + getAssetClass() + "'" +
            ", exchangeCode='" + getExchangeCode() + "'" +
            ", tickSize=" + getTickSize() +
            ", lotSize=" + getLotSize() +
            ", currency='" + getCurrency() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
