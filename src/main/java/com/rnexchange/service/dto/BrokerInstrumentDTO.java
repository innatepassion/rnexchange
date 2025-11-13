package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import java.io.Serializable;
import java.math.BigDecimal;

public class BrokerInstrumentDTO implements Serializable {

    private String symbol;
    private String name;
    private String exchangeCode;
    private AssetClass assetClass;
    private BigDecimal tickSize;
    private Long lotSize;
    private Currency currency;

    public BrokerInstrumentDTO() {}

    public BrokerInstrumentDTO(
        String symbol,
        String name,
        String exchangeCode,
        AssetClass assetClass,
        BigDecimal tickSize,
        Long lotSize,
        Currency currency
    ) {
        this.symbol = symbol;
        this.name = name;
        this.exchangeCode = exchangeCode;
        this.assetClass = assetClass;
        this.tickSize = tickSize;
        this.lotSize = lotSize;
        this.currency = currency;
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

    public String getExchangeCode() {
        return exchangeCode;
    }

    public void setExchangeCode(String exchangeCode) {
        this.exchangeCode = exchangeCode;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(AssetClass assetClass) {
        this.assetClass = assetClass;
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
}
