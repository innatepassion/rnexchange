package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "exchange_volatility_override",
    uniqueConstraints = { @UniqueConstraint(name = "ux_exchange_volatility_override", columnNames = { "exchange_code", "asset_class" }) }
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExchangeVolatilityOverride implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "exchange_code", nullable = false, length = 10)
    private String exchangeCode;

    @NotBlank
    @Column(name = "asset_class", nullable = false, length = 30)
    private String assetClass;

    @NotNull
    @DecimalMin("0.00001")
    @Column(name = "volatility_pct", precision = 6, scale = 5, nullable = false)
    private BigDecimal volatilityPct;

    @NotNull
    @Column(name = "last_modified", nullable = false)
    private Instant lastModified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_code", referencedColumnName = "code", insertable = false, updatable = false)
    @JsonIgnoreProperties(value = { "brokerDesks", "exchangeOperators", "instruments", "contracts" }, allowSetters = true)
    private Exchange exchange;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return id;
    }

    public ExchangeVolatilityOverride id(Long id) {
        this.id = id;
        return this;
    }

    public String getExchangeCode() {
        return exchangeCode;
    }

    public ExchangeVolatilityOverride exchangeCode(String exchangeCode) {
        this.exchangeCode = exchangeCode;
        return this;
    }

    public void setExchangeCode(String exchangeCode) {
        this.exchangeCode = exchangeCode;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public ExchangeVolatilityOverride assetClass(String assetClass) {
        this.assetClass = assetClass;
        return this;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }

    public BigDecimal getVolatilityPct() {
        return volatilityPct;
    }

    public ExchangeVolatilityOverride volatilityPct(BigDecimal volatilityPct) {
        this.volatilityPct = volatilityPct;
        return this;
    }

    public void setVolatilityPct(BigDecimal volatilityPct) {
        this.volatilityPct = volatilityPct;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public ExchangeVolatilityOverride lastModified(Instant lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExchangeVolatilityOverride)) {
            return false;
        }
        return getId() != null && getId().equals(((ExchangeVolatilityOverride) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "ExchangeVolatilityOverride{" +
            "id=" +
            getId() +
            ", exchangeCode='" +
            getExchangeCode() +
            '\'' +
            ", assetClass='" +
            getAssetClass() +
            '\'' +
            ", volatilityPct=" +
            getVolatilityPct() +
            ", lastModified=" +
            getLastModified() +
            '}'
        );
    }
}
