package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.Instrument} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InstrumentDTO implements Serializable {

    private Long id;

    @NotNull
    private String symbol;

    private String name;

    @NotNull
    private AssetClass assetClass;

    @NotNull
    private String exchangeCode;

    @NotNull
    private BigDecimal tickSize;

    @NotNull
    private Long lotSize;

    @NotNull
    private Currency currency;

    @NotNull
    private String status;

    private ExchangeDTO exchange;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(AssetClass assetClass) {
        this.assetClass = assetClass;
    }

    public String getExchangeCode() {
        return exchangeCode;
    }

    public void setExchangeCode(String exchangeCode) {
        this.exchangeCode = exchangeCode;
    }

    public BigDecimal getTickSize() {
        return tickSize;
    }

    public void setTickSize(BigDecimal tickSize) {
        this.tickSize = tickSize;
    }

    public Long getLotSize() {
        return lotSize;
    }

    public void setLotSize(Long lotSize) {
        this.lotSize = lotSize;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ExchangeDTO getExchange() {
        return exchange;
    }

    public void setExchange(ExchangeDTO exchange) {
        this.exchange = exchange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InstrumentDTO)) {
            return false;
        }

        InstrumentDTO instrumentDTO = (InstrumentDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, instrumentDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InstrumentDTO{" +
            "id=" + getId() +
            ", symbol='" + getSymbol() + "'" +
            ", name='" + getName() + "'" +
            ", assetClass='" + getAssetClass() + "'" +
            ", exchangeCode='" + getExchangeCode() + "'" +
            ", tickSize=" + getTickSize() +
            ", lotSize=" + getLotSize() +
            ", currency='" + getCurrency() + "'" +
            ", status='" + getStatus() + "'" +
            ", exchange=" + getExchange() +
            "}";
    }
}
