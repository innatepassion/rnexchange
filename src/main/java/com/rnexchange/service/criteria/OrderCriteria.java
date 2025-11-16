package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.OrderSide;
import com.rnexchange.domain.enumeration.OrderStatus;
import com.rnexchange.domain.enumeration.OrderType;
import com.rnexchange.domain.enumeration.Tif;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.Order} entity. This class is used
 * in {@link com.rnexchange.web.rest.OrderResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /orders?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OrderCriteria implements Serializable, Criteria {

    /**
     * Class for filtering OrderSide
     */
    public static class OrderSideFilter extends Filter<OrderSide> {

        public OrderSideFilter() {}

        public OrderSideFilter(OrderSideFilter filter) {
            super(filter);
        }

        @Override
        public OrderSideFilter copy() {
            return new OrderSideFilter(this);
        }
    }

    /**
     * Class for filtering OrderType
     */
    public static class OrderTypeFilter extends Filter<OrderType> {

        public OrderTypeFilter() {}

        public OrderTypeFilter(OrderTypeFilter filter) {
            super(filter);
        }

        @Override
        public OrderTypeFilter copy() {
            return new OrderTypeFilter(this);
        }
    }

    /**
     * Class for filtering Tif
     */
    public static class TifFilter extends Filter<Tif> {

        public TifFilter() {}

        public TifFilter(TifFilter filter) {
            super(filter);
        }

        @Override
        public TifFilter copy() {
            return new TifFilter(this);
        }
    }

    /**
     * Class for filtering OrderStatus
     */
    public static class OrderStatusFilter extends Filter<OrderStatus> {

        public OrderStatusFilter() {}

        public OrderStatusFilter(OrderStatusFilter filter) {
            super(filter);
        }

        @Override
        public OrderStatusFilter copy() {
            return new OrderStatusFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private OrderSideFilter side;

    private OrderTypeFilter type;

    private BigDecimalFilter qty;

    private BigDecimalFilter limitPx;

    private BigDecimalFilter stopPx;

    private TifFilter tif;

    private OrderStatusFilter status;

    private StringFilter rejectionReason;

    private StringFilter venue;

    private InstantFilter createdAt;

    private InstantFilter updatedAt;

    private LongFilter tradingAccountId;

    private LongFilter instrumentId;

    private Boolean distinct;

    public OrderCriteria() {}

    public OrderCriteria(OrderCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.side = other.optionalSide().map(OrderSideFilter::copy).orElse(null);
        this.type = other.optionalType().map(OrderTypeFilter::copy).orElse(null);
        this.qty = other.optionalQty().map(BigDecimalFilter::copy).orElse(null);
        this.limitPx = other.optionalLimitPx().map(BigDecimalFilter::copy).orElse(null);
        this.stopPx = other.optionalStopPx().map(BigDecimalFilter::copy).orElse(null);
        this.tif = other.optionalTif().map(TifFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(OrderStatusFilter::copy).orElse(null);
        this.rejectionReason = other.optionalRejectionReason().map(StringFilter::copy).orElse(null);
        this.venue = other.optionalVenue().map(StringFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.updatedAt = other.optionalUpdatedAt().map(InstantFilter::copy).orElse(null);
        this.tradingAccountId = other.optionalTradingAccountId().map(LongFilter::copy).orElse(null);
        this.instrumentId = other.optionalInstrumentId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public OrderCriteria copy() {
        return new OrderCriteria(this);
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

    public OrderSideFilter getSide() {
        return side;
    }

    public Optional<OrderSideFilter> optionalSide() {
        return Optional.ofNullable(side);
    }

    public OrderSideFilter side() {
        if (side == null) {
            setSide(new OrderSideFilter());
        }
        return side;
    }

    public void setSide(OrderSideFilter side) {
        this.side = side;
    }

    public OrderTypeFilter getType() {
        return type;
    }

    public Optional<OrderTypeFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public OrderTypeFilter type() {
        if (type == null) {
            setType(new OrderTypeFilter());
        }
        return type;
    }

    public void setType(OrderTypeFilter type) {
        this.type = type;
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

    public BigDecimalFilter getLimitPx() {
        return limitPx;
    }

    public Optional<BigDecimalFilter> optionalLimitPx() {
        return Optional.ofNullable(limitPx);
    }

    public BigDecimalFilter limitPx() {
        if (limitPx == null) {
            setLimitPx(new BigDecimalFilter());
        }
        return limitPx;
    }

    public void setLimitPx(BigDecimalFilter limitPx) {
        this.limitPx = limitPx;
    }

    public BigDecimalFilter getStopPx() {
        return stopPx;
    }

    public Optional<BigDecimalFilter> optionalStopPx() {
        return Optional.ofNullable(stopPx);
    }

    public BigDecimalFilter stopPx() {
        if (stopPx == null) {
            setStopPx(new BigDecimalFilter());
        }
        return stopPx;
    }

    public void setStopPx(BigDecimalFilter stopPx) {
        this.stopPx = stopPx;
    }

    public TifFilter getTif() {
        return tif;
    }

    public Optional<TifFilter> optionalTif() {
        return Optional.ofNullable(tif);
    }

    public TifFilter tif() {
        if (tif == null) {
            setTif(new TifFilter());
        }
        return tif;
    }

    public void setTif(TifFilter tif) {
        this.tif = tif;
    }

    public OrderStatusFilter getStatus() {
        return status;
    }

    public Optional<OrderStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public OrderStatusFilter status() {
        if (status == null) {
            setStatus(new OrderStatusFilter());
        }
        return status;
    }

    public void setStatus(OrderStatusFilter status) {
        this.status = status;
    }

    public StringFilter getRejectionReason() {
        return rejectionReason;
    }

    public Optional<StringFilter> optionalRejectionReason() {
        return Optional.ofNullable(rejectionReason);
    }

    public StringFilter rejectionReason() {
        if (rejectionReason == null) {
            setRejectionReason(new StringFilter());
        }
        return rejectionReason;
    }

    public void setRejectionReason(StringFilter rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public StringFilter getVenue() {
        return venue;
    }

    public Optional<StringFilter> optionalVenue() {
        return Optional.ofNullable(venue);
    }

    public StringFilter venue() {
        if (venue == null) {
            setVenue(new StringFilter());
        }
        return venue;
    }

    public void setVenue(StringFilter venue) {
        this.venue = venue;
    }

    public InstantFilter getCreatedAt() {
        return createdAt;
    }

    public Optional<InstantFilter> optionalCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public InstantFilter createdAt() {
        if (createdAt == null) {
            setCreatedAt(new InstantFilter());
        }
        return createdAt;
    }

    public void setCreatedAt(InstantFilter createdAt) {
        this.createdAt = createdAt;
    }

    public InstantFilter getUpdatedAt() {
        return updatedAt;
    }

    public Optional<InstantFilter> optionalUpdatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    public InstantFilter updatedAt() {
        if (updatedAt == null) {
            setUpdatedAt(new InstantFilter());
        }
        return updatedAt;
    }

    public void setUpdatedAt(InstantFilter updatedAt) {
        this.updatedAt = updatedAt;
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
        final OrderCriteria that = (OrderCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(side, that.side) &&
            Objects.equals(type, that.type) &&
            Objects.equals(qty, that.qty) &&
            Objects.equals(limitPx, that.limitPx) &&
            Objects.equals(stopPx, that.stopPx) &&
            Objects.equals(tif, that.tif) &&
            Objects.equals(status, that.status) &&
            Objects.equals(rejectionReason, that.rejectionReason) &&
            Objects.equals(venue, that.venue) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(tradingAccountId, that.tradingAccountId) &&
            Objects.equals(instrumentId, that.instrumentId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            side,
            type,
            qty,
            limitPx,
            stopPx,
            tif,
            status,
            rejectionReason,
            venue,
            createdAt,
            updatedAt,
            tradingAccountId,
            instrumentId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OrderCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalSide().map(f -> "side=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalQty().map(f -> "qty=" + f + ", ").orElse("") +
            optionalLimitPx().map(f -> "limitPx=" + f + ", ").orElse("") +
            optionalStopPx().map(f -> "stopPx=" + f + ", ").orElse("") +
            optionalTif().map(f -> "tif=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalRejectionReason().map(f -> "rejectionReason=" + f + ", ").orElse("") +
            optionalVenue().map(f -> "venue=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalUpdatedAt().map(f -> "updatedAt=" + f + ", ").orElse("") +
            optionalTradingAccountId().map(f -> "tradingAccountId=" + f + ", ").orElse("") +
            optionalInstrumentId().map(f -> "instrumentId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
