package com.rnexchange.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.MarketHoliday} entity. This class is used
 * in {@link com.rnexchange.web.rest.MarketHolidayResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /market-holidays?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MarketHolidayCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LocalDateFilter tradeDate;

    private StringFilter reason;

    private BooleanFilter isHoliday;

    private LongFilter exchangeId;

    private Boolean distinct;

    public MarketHolidayCriteria() {}

    public MarketHolidayCriteria(MarketHolidayCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.tradeDate = other.optionalTradeDate().map(LocalDateFilter::copy).orElse(null);
        this.reason = other.optionalReason().map(StringFilter::copy).orElse(null);
        this.isHoliday = other.optionalIsHoliday().map(BooleanFilter::copy).orElse(null);
        this.exchangeId = other.optionalExchangeId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public MarketHolidayCriteria copy() {
        return new MarketHolidayCriteria(this);
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

    public LocalDateFilter getTradeDate() {
        return tradeDate;
    }

    public Optional<LocalDateFilter> optionalTradeDate() {
        return Optional.ofNullable(tradeDate);
    }

    public LocalDateFilter tradeDate() {
        if (tradeDate == null) {
            setTradeDate(new LocalDateFilter());
        }
        return tradeDate;
    }

    public void setTradeDate(LocalDateFilter tradeDate) {
        this.tradeDate = tradeDate;
    }

    public StringFilter getReason() {
        return reason;
    }

    public Optional<StringFilter> optionalReason() {
        return Optional.ofNullable(reason);
    }

    public StringFilter reason() {
        if (reason == null) {
            setReason(new StringFilter());
        }
        return reason;
    }

    public void setReason(StringFilter reason) {
        this.reason = reason;
    }

    public BooleanFilter getIsHoliday() {
        return isHoliday;
    }

    public Optional<BooleanFilter> optionalIsHoliday() {
        return Optional.ofNullable(isHoliday);
    }

    public BooleanFilter isHoliday() {
        if (isHoliday == null) {
            setIsHoliday(new BooleanFilter());
        }
        return isHoliday;
    }

    public void setIsHoliday(BooleanFilter isHoliday) {
        this.isHoliday = isHoliday;
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
        final MarketHolidayCriteria that = (MarketHolidayCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(tradeDate, that.tradeDate) &&
            Objects.equals(reason, that.reason) &&
            Objects.equals(isHoliday, that.isHoliday) &&
            Objects.equals(exchangeId, that.exchangeId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tradeDate, reason, isHoliday, exchangeId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MarketHolidayCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTradeDate().map(f -> "tradeDate=" + f + ", ").orElse("") +
            optionalReason().map(f -> "reason=" + f + ", ").orElse("") +
            optionalIsHoliday().map(f -> "isHoliday=" + f + ", ").orElse("") +
            optionalExchangeId().map(f -> "exchangeId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
