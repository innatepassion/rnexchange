package com.rnexchange.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.Lot} entity. This class is used
 * in {@link com.rnexchange.web.rest.LotResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /lots?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LotCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter openTs;

    private BigDecimalFilter openPx;

    private BigDecimalFilter qtyOpen;

    private BigDecimalFilter qtyClosed;

    private StringFilter method;

    private LongFilter positionId;

    private Boolean distinct;

    public LotCriteria() {}

    public LotCriteria(LotCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.openTs = other.optionalOpenTs().map(InstantFilter::copy).orElse(null);
        this.openPx = other.optionalOpenPx().map(BigDecimalFilter::copy).orElse(null);
        this.qtyOpen = other.optionalQtyOpen().map(BigDecimalFilter::copy).orElse(null);
        this.qtyClosed = other.optionalQtyClosed().map(BigDecimalFilter::copy).orElse(null);
        this.method = other.optionalMethod().map(StringFilter::copy).orElse(null);
        this.positionId = other.optionalPositionId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public LotCriteria copy() {
        return new LotCriteria(this);
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

    public InstantFilter getOpenTs() {
        return openTs;
    }

    public Optional<InstantFilter> optionalOpenTs() {
        return Optional.ofNullable(openTs);
    }

    public InstantFilter openTs() {
        if (openTs == null) {
            setOpenTs(new InstantFilter());
        }
        return openTs;
    }

    public void setOpenTs(InstantFilter openTs) {
        this.openTs = openTs;
    }

    public BigDecimalFilter getOpenPx() {
        return openPx;
    }

    public Optional<BigDecimalFilter> optionalOpenPx() {
        return Optional.ofNullable(openPx);
    }

    public BigDecimalFilter openPx() {
        if (openPx == null) {
            setOpenPx(new BigDecimalFilter());
        }
        return openPx;
    }

    public void setOpenPx(BigDecimalFilter openPx) {
        this.openPx = openPx;
    }

    public BigDecimalFilter getQtyOpen() {
        return qtyOpen;
    }

    public Optional<BigDecimalFilter> optionalQtyOpen() {
        return Optional.ofNullable(qtyOpen);
    }

    public BigDecimalFilter qtyOpen() {
        if (qtyOpen == null) {
            setQtyOpen(new BigDecimalFilter());
        }
        return qtyOpen;
    }

    public void setQtyOpen(BigDecimalFilter qtyOpen) {
        this.qtyOpen = qtyOpen;
    }

    public BigDecimalFilter getQtyClosed() {
        return qtyClosed;
    }

    public Optional<BigDecimalFilter> optionalQtyClosed() {
        return Optional.ofNullable(qtyClosed);
    }

    public BigDecimalFilter qtyClosed() {
        if (qtyClosed == null) {
            setQtyClosed(new BigDecimalFilter());
        }
        return qtyClosed;
    }

    public void setQtyClosed(BigDecimalFilter qtyClosed) {
        this.qtyClosed = qtyClosed;
    }

    public StringFilter getMethod() {
        return method;
    }

    public Optional<StringFilter> optionalMethod() {
        return Optional.ofNullable(method);
    }

    public StringFilter method() {
        if (method == null) {
            setMethod(new StringFilter());
        }
        return method;
    }

    public void setMethod(StringFilter method) {
        this.method = method;
    }

    public LongFilter getPositionId() {
        return positionId;
    }

    public Optional<LongFilter> optionalPositionId() {
        return Optional.ofNullable(positionId);
    }

    public LongFilter positionId() {
        if (positionId == null) {
            setPositionId(new LongFilter());
        }
        return positionId;
    }

    public void setPositionId(LongFilter positionId) {
        this.positionId = positionId;
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
        final LotCriteria that = (LotCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(openTs, that.openTs) &&
            Objects.equals(openPx, that.openPx) &&
            Objects.equals(qtyOpen, that.qtyOpen) &&
            Objects.equals(qtyClosed, that.qtyClosed) &&
            Objects.equals(method, that.method) &&
            Objects.equals(positionId, that.positionId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, openTs, openPx, qtyOpen, qtyClosed, method, positionId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LotCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalOpenTs().map(f -> "openTs=" + f + ", ").orElse("") +
            optionalOpenPx().map(f -> "openPx=" + f + ", ").orElse("") +
            optionalQtyOpen().map(f -> "qtyOpen=" + f + ", ").orElse("") +
            optionalQtyClosed().map(f -> "qtyClosed=" + f + ", ").orElse("") +
            optionalMethod().map(f -> "method=" + f + ", ").orElse("") +
            optionalPositionId().map(f -> "positionId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
