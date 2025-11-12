package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.Instrument} entity. This class is used
 * in {@link com.rnexchange.web.rest.InstrumentResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /instruments?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InstrumentCriteria implements Serializable, Criteria {

    /**
     * Class for filtering AssetClass
     */
    public static class AssetClassFilter extends Filter<AssetClass> {

        public AssetClassFilter() {}

        public AssetClassFilter(AssetClassFilter filter) {
            super(filter);
        }

        @Override
        public AssetClassFilter copy() {
            return new AssetClassFilter(this);
        }
    }

    /**
     * Class for filtering Currency
     */
    public static class CurrencyFilter extends Filter<Currency> {

        public CurrencyFilter() {}

        public CurrencyFilter(CurrencyFilter filter) {
            super(filter);
        }

        @Override
        public CurrencyFilter copy() {
            return new CurrencyFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter symbol;

    private StringFilter name;

    private AssetClassFilter assetClass;

    private StringFilter exchangeCode;

    private BigDecimalFilter tickSize;

    private LongFilter lotSize;

    private CurrencyFilter currency;

    private StringFilter status;

    private LongFilter exchangeId;

    private Boolean distinct;

    public InstrumentCriteria() {}

    public InstrumentCriteria(InstrumentCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.symbol = other.optionalSymbol().map(StringFilter::copy).orElse(null);
        this.name = other.optionalName().map(StringFilter::copy).orElse(null);
        this.assetClass = other.optionalAssetClass().map(AssetClassFilter::copy).orElse(null);
        this.exchangeCode = other.optionalExchangeCode().map(StringFilter::copy).orElse(null);
        this.tickSize = other.optionalTickSize().map(BigDecimalFilter::copy).orElse(null);
        this.lotSize = other.optionalLotSize().map(LongFilter::copy).orElse(null);
        this.currency = other.optionalCurrency().map(CurrencyFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(StringFilter::copy).orElse(null);
        this.exchangeId = other.optionalExchangeId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public InstrumentCriteria copy() {
        return new InstrumentCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getSymbol() {
        return symbol;
    }

    public Optional<StringFilter> optionalSymbol() {
        return Optional.ofNullable(symbol);
    }

    public StringFilter symbol() {
        if (symbol == null) {
            setSymbol(new StringFilter());
        }
        return symbol;
    }

    public void setSymbol(StringFilter symbol) {
        this.symbol = symbol;
    }

    public StringFilter getName() {
        return name;
    }

    public Optional<StringFilter> optionalName() {
        return Optional.ofNullable(name);
    }

    public StringFilter name() {
        if (name == null) {
            setName(new StringFilter());
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public AssetClassFilter getAssetClass() {
        return assetClass;
    }

    public Optional<AssetClassFilter> optionalAssetClass() {
        return Optional.ofNullable(assetClass);
    }

    public AssetClassFilter assetClass() {
        if (assetClass == null) {
            setAssetClass(new AssetClassFilter());
        }
        return assetClass;
    }

    public void setAssetClass(AssetClassFilter assetClass) {
        this.assetClass = assetClass;
    }

    public StringFilter getExchangeCode() {
        return exchangeCode;
    }

    public Optional<StringFilter> optionalExchangeCode() {
        return Optional.ofNullable(exchangeCode);
    }

    public StringFilter exchangeCode() {
        if (exchangeCode == null) {
            setExchangeCode(new StringFilter());
        }
        return exchangeCode;
    }

    public void setExchangeCode(StringFilter exchangeCode) {
        this.exchangeCode = exchangeCode;
    }

    public BigDecimalFilter getTickSize() {
        return tickSize;
    }

    public Optional<BigDecimalFilter> optionalTickSize() {
        return Optional.ofNullable(tickSize);
    }

    public BigDecimalFilter tickSize() {
        if (tickSize == null) {
            setTickSize(new BigDecimalFilter());
        }
        return tickSize;
    }

    public void setTickSize(BigDecimalFilter tickSize) {
        this.tickSize = tickSize;
    }

    public LongFilter getLotSize() {
        return lotSize;
    }

    public Optional<LongFilter> optionalLotSize() {
        return Optional.ofNullable(lotSize);
    }

    public LongFilter lotSize() {
        if (lotSize == null) {
            setLotSize(new LongFilter());
        }
        return lotSize;
    }

    public void setLotSize(LongFilter lotSize) {
        this.lotSize = lotSize;
    }

    public CurrencyFilter getCurrency() {
        return currency;
    }

    public Optional<CurrencyFilter> optionalCurrency() {
        return Optional.ofNullable(currency);
    }

    public CurrencyFilter currency() {
        if (currency == null) {
            setCurrency(new CurrencyFilter());
        }
        return currency;
    }

    public void setCurrency(CurrencyFilter currency) {
        this.currency = currency;
    }

    public StringFilter getStatus() {
        return status;
    }

    public Optional<StringFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public StringFilter status() {
        if (status == null) {
            setStatus(new StringFilter());
        }
        return status;
    }

    public void setStatus(StringFilter status) {
        this.status = status;
    }

    public LongFilter getExchangeId() {
        return exchangeId;
    }

    public Optional<LongFilter> optionalExchangeId() {
        return Optional.ofNullable(exchangeId);
    }

    public LongFilter exchangeId() {
        if (exchangeId == null) {
            setExchangeId(new LongFilter());
        }
        return exchangeId;
    }

    public void setExchangeId(LongFilter exchangeId) {
        this.exchangeId = exchangeId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InstrumentCriteria that = (InstrumentCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(symbol, that.symbol) &&
            Objects.equals(name, that.name) &&
            Objects.equals(assetClass, that.assetClass) &&
            Objects.equals(exchangeCode, that.exchangeCode) &&
            Objects.equals(tickSize, that.tickSize) &&
            Objects.equals(lotSize, that.lotSize) &&
            Objects.equals(currency, that.currency) &&
            Objects.equals(status, that.status) &&
            Objects.equals(exchangeId, that.exchangeId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol, name, assetClass, exchangeCode, tickSize, lotSize, currency, status, exchangeId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InstrumentCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalSymbol().map(f -> "symbol=" + f + ", ").orElse("") +
            optionalName().map(f -> "name=" + f + ", ").orElse("") +
            optionalAssetClass().map(f -> "assetClass=" + f + ", ").orElse("") +
            optionalExchangeCode().map(f -> "exchangeCode=" + f + ", ").orElse("") +
            optionalTickSize().map(f -> "tickSize=" + f + ", ").orElse("") +
            optionalLotSize().map(f -> "lotSize=" + f + ", ").orElse("") +
            optionalCurrency().map(f -> "currency=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalExchangeId().map(f -> "exchangeId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
