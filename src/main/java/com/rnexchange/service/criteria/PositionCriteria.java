package com.rnexchange.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.Position} entity. This class is used
 * in {@link com.rnexchange.web.rest.PositionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /positions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PositionCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private BigDecimalFilter qty;

    private BigDecimalFilter avgCost;

    private BigDecimalFilter lastPx;

    private BigDecimalFilter unrealizedPnl;

    private BigDecimalFilter realizedPnl;

    private LongFilter tradingAccountId;

    private LongFilter instrumentId;

    private Boolean distinct;

    public PositionCriteria() {}

    public PositionCriteria(PositionCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.qty = other.optionalQty().map(BigDecimalFilter::copy).orElse(null);
        this.avgCost = other.optionalAvgCost().map(BigDecimalFilter::copy).orElse(null);
        this.lastPx = other.optionalLastPx().map(BigDecimalFilter::copy).orElse(null);
        this.unrealizedPnl = other.optionalUnrealizedPnl().map(BigDecimalFilter::copy).orElse(null);
        this.realizedPnl = other.optionalRealizedPnl().map(BigDecimalFilter::copy).orElse(null);
        this.tradingAccountId = other.optionalTradingAccountId().map(LongFilter::copy).orElse(null);
        this.instrumentId = other.optionalInstrumentId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public PositionCriteria copy() {
        return new PositionCriteria(this);
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

    public BigDecimalFilter getQty() {
        return qty;
    }

    public Optional<BigDecimalFilter> optionalQty() {
        return Optional.ofNullable(qty);
    }

    public BigDecimalFilter qty() {
        if (qty == null) {
            setQty(new BigDecimalFilter());
        }
        return qty;
    }

    public void setQty(BigDecimalFilter qty) {
        this.qty = qty;
    }

    public BigDecimalFilter getAvgCost() {
        return avgCost;
    }

    public Optional<BigDecimalFilter> optionalAvgCost() {
        return Optional.ofNullable(avgCost);
    }

    public BigDecimalFilter avgCost() {
        if (avgCost == null) {
            setAvgCost(new BigDecimalFilter());
        }
        return avgCost;
    }

    public void setAvgCost(BigDecimalFilter avgCost) {
        this.avgCost = avgCost;
    }

    public BigDecimalFilter getLastPx() {
        return lastPx;
    }

    public Optional<BigDecimalFilter> optionalLastPx() {
        return Optional.ofNullable(lastPx);
    }

    public BigDecimalFilter lastPx() {
        if (lastPx == null) {
            setLastPx(new BigDecimalFilter());
        }
        return lastPx;
    }

    public void setLastPx(BigDecimalFilter lastPx) {
        this.lastPx = lastPx;
    }

    public BigDecimalFilter getUnrealizedPnl() {
        return unrealizedPnl;
    }

    public Optional<BigDecimalFilter> optionalUnrealizedPnl() {
        return Optional.ofNullable(unrealizedPnl);
    }

    public BigDecimalFilter unrealizedPnl() {
        if (unrealizedPnl == null) {
            setUnrealizedPnl(new BigDecimalFilter());
        }
        return unrealizedPnl;
    }

    public void setUnrealizedPnl(BigDecimalFilter unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    public BigDecimalFilter getRealizedPnl() {
        return realizedPnl;
    }

    public Optional<BigDecimalFilter> optionalRealizedPnl() {
        return Optional.ofNullable(realizedPnl);
    }

    public BigDecimalFilter realizedPnl() {
        if (realizedPnl == null) {
            setRealizedPnl(new BigDecimalFilter());
        }
        return realizedPnl;
    }

    public void setRealizedPnl(BigDecimalFilter realizedPnl) {
        this.realizedPnl = realizedPnl;
    }

    public LongFilter getTradingAccountId() {
        return tradingAccountId;
    }

    public Optional<LongFilter> optionalTradingAccountId() {
        return Optional.ofNullable(tradingAccountId);
    }

    public LongFilter tradingAccountId() {
        if (tradingAccountId == null) {
            setTradingAccountId(new LongFilter());
        }
        return tradingAccountId;
    }

    public void setTradingAccountId(LongFilter tradingAccountId) {
        this.tradingAccountId = tradingAccountId;
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
        final PositionCriteria that = (PositionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(qty, that.qty) &&
            Objects.equals(avgCost, that.avgCost) &&
            Objects.equals(lastPx, that.lastPx) &&
            Objects.equals(unrealizedPnl, that.unrealizedPnl) &&
            Objects.equals(realizedPnl, that.realizedPnl) &&
            Objects.equals(tradingAccountId, that.tradingAccountId) &&
            Objects.equals(instrumentId, that.instrumentId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, qty, avgCost, lastPx, unrealizedPnl, realizedPnl, tradingAccountId, instrumentId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PositionCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalQty().map(f -> "qty=" + f + ", ").orElse("") +
            optionalAvgCost().map(f -> "avgCost=" + f + ", ").orElse("") +
            optionalLastPx().map(f -> "lastPx=" + f + ", ").orElse("") +
            optionalUnrealizedPnl().map(f -> "unrealizedPnl=" + f + ", ").orElse("") +
            optionalRealizedPnl().map(f -> "realizedPnl=" + f + ", ").orElse("") +
            optionalTradingAccountId().map(f -> "tradingAccountId=" + f + ", ").orElse("") +
            optionalInstrumentId().map(f -> "instrumentId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
