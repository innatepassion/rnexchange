package com.rnexchange.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.DailySettlementPrice} entity. This class is used
 * in {@link com.rnexchange.web.rest.DailySettlementPriceResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /daily-settlement-prices?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DailySettlementPriceCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LocalDateFilter refDate;

    private StringFilter instrumentSymbol;

    private BigDecimalFilter settlePrice;

    private LongFilter instrumentId;

    private Boolean distinct;

    public DailySettlementPriceCriteria() {}

    public DailySettlementPriceCriteria(DailySettlementPriceCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.refDate = other.optionalRefDate().map(LocalDateFilter::copy).orElse(null);
        this.instrumentSymbol = other.optionalInstrumentSymbol().map(StringFilter::copy).orElse(null);
        this.settlePrice = other.optionalSettlePrice().map(BigDecimalFilter::copy).orElse(null);
        this.instrumentId = other.optionalInstrumentId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public DailySettlementPriceCriteria copy() {
        return new DailySettlementPriceCriteria(this);
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

    public LocalDateFilter getRefDate() {
        return refDate;
    }

    public Optional<LocalDateFilter> optionalRefDate() {
        return Optional.ofNullable(refDate);
    }

    public LocalDateFilter refDate() {
        if (refDate == null) {
            setRefDate(new LocalDateFilter());
        }
        return refDate;
    }

    public void setRefDate(LocalDateFilter refDate) {
        this.refDate = refDate;
    }

    public StringFilter getInstrumentSymbol() {
        return instrumentSymbol;
    }

    public Optional<StringFilter> optionalInstrumentSymbol() {
        return Optional.ofNullable(instrumentSymbol);
    }

    public StringFilter instrumentSymbol() {
        if (instrumentSymbol == null) {
            setInstrumentSymbol(new StringFilter());
        }
        return instrumentSymbol;
    }

    public void setInstrumentSymbol(StringFilter instrumentSymbol) {
        this.instrumentSymbol = instrumentSymbol;
    }

    public BigDecimalFilter getSettlePrice() {
        return settlePrice;
    }

    public Optional<BigDecimalFilter> optionalSettlePrice() {
        return Optional.ofNullable(settlePrice);
    }

    public BigDecimalFilter settlePrice() {
        if (settlePrice == null) {
            setSettlePrice(new BigDecimalFilter());
        }
        return settlePrice;
    }

    public void setSettlePrice(BigDecimalFilter settlePrice) {
        this.settlePrice = settlePrice;
    }

    public LongFilter getInstrumentId() {
        return instrumentId;
    }

    public Optional<LongFilter> optionalInstrumentId() {
        return Optional.ofNullable(instrumentId);
    }

    public LongFilter instrumentId() {
        if (instrumentId == null) {
            setInstrumentId(new LongFilter());
        }
        return instrumentId;
    }

    public void setInstrumentId(LongFilter instrumentId) {
        this.instrumentId = instrumentId;
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
        final DailySettlementPriceCriteria that = (DailySettlementPriceCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(refDate, that.refDate) &&
            Objects.equals(instrumentSymbol, that.instrumentSymbol) &&
            Objects.equals(settlePrice, that.settlePrice) &&
            Objects.equals(instrumentId, that.instrumentId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, refDate, instrumentSymbol, settlePrice, instrumentId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DailySettlementPriceCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalRefDate().map(f -> "refDate=" + f + ", ").orElse("") +
            optionalInstrumentSymbol().map(f -> "instrumentSymbol=" + f + ", ").orElse("") +
            optionalSettlePrice().map(f -> "settlePrice=" + f + ", ").orElse("") +
            optionalInstrumentId().map(f -> "instrumentId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
