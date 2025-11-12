package com.rnexchange.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.Execution} entity. This class is used
 * in {@link com.rnexchange.web.rest.ExecutionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /executions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExecutionCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter execTs;

    private BigDecimalFilter px;

    private BigDecimalFilter qty;

    private StringFilter liquidity;

    private BigDecimalFilter fee;

    private LongFilter orderId;

    private Boolean distinct;

    public ExecutionCriteria() {}

    public ExecutionCriteria(ExecutionCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.execTs = other.optionalExecTs().map(InstantFilter::copy).orElse(null);
        this.px = other.optionalPx().map(BigDecimalFilter::copy).orElse(null);
        this.qty = other.optionalQty().map(BigDecimalFilter::copy).orElse(null);
        this.liquidity = other.optionalLiquidity().map(StringFilter::copy).orElse(null);
        this.fee = other.optionalFee().map(BigDecimalFilter::copy).orElse(null);
        this.orderId = other.optionalOrderId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ExecutionCriteria copy() {
        return new ExecutionCriteria(this);
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

    public InstantFilter getExecTs() {
        return execTs;
    }

    public Optional<InstantFilter> optionalExecTs() {
        return Optional.ofNullable(execTs);
    }

    public InstantFilter execTs() {
        if (execTs == null) {
            setExecTs(new InstantFilter());
        }
        return execTs;
    }

    public void setExecTs(InstantFilter execTs) {
        this.execTs = execTs;
    }

    public BigDecimalFilter getPx() {
        return px;
    }

    public Optional<BigDecimalFilter> optionalPx() {
        return Optional.ofNullable(px);
    }

    public BigDecimalFilter px() {
        if (px == null) {
            setPx(new BigDecimalFilter());
        }
        return px;
    }

    public void setPx(BigDecimalFilter px) {
        this.px = px;
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

    public StringFilter getLiquidity() {
        return liquidity;
    }

    public Optional<StringFilter> optionalLiquidity() {
        return Optional.ofNullable(liquidity);
    }

    public StringFilter liquidity() {
        if (liquidity == null) {
            setLiquidity(new StringFilter());
        }
        return liquidity;
    }

    public void setLiquidity(StringFilter liquidity) {
        this.liquidity = liquidity;
    }

    public BigDecimalFilter getFee() {
        return fee;
    }

    public Optional<BigDecimalFilter> optionalFee() {
        return Optional.ofNullable(fee);
    }

    public BigDecimalFilter fee() {
        if (fee == null) {
            setFee(new BigDecimalFilter());
        }
        return fee;
    }

    public void setFee(BigDecimalFilter fee) {
        this.fee = fee;
    }

    public LongFilter getOrderId() {
        return orderId;
    }

    public Optional<LongFilter> optionalOrderId() {
        return Optional.ofNullable(orderId);
    }

    public LongFilter orderId() {
        if (orderId == null) {
            setOrderId(new LongFilter());
        }
        return orderId;
    }

    public void setOrderId(LongFilter orderId) {
        this.orderId = orderId;
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
        final ExecutionCriteria that = (ExecutionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(execTs, that.execTs) &&
            Objects.equals(px, that.px) &&
            Objects.equals(qty, that.qty) &&
            Objects.equals(liquidity, that.liquidity) &&
            Objects.equals(fee, that.fee) &&
            Objects.equals(orderId, that.orderId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, execTs, px, qty, liquidity, fee, orderId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExecutionCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalExecTs().map(f -> "execTs=" + f + ", ").orElse("") +
            optionalPx().map(f -> "px=" + f + ", ").orElse("") +
            optionalQty().map(f -> "qty=" + f + ", ").orElse("") +
            optionalLiquidity().map(f -> "liquidity=" + f + ", ").orElse("") +
            optionalFee().map(f -> "fee=" + f + ", ").orElse("") +
            optionalOrderId().map(f -> "orderId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
