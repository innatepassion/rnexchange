package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.SettlementKind;
import com.rnexchange.domain.enumeration.SettlementStatus;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.SettlementBatch} entity. This class is used
 * in {@link com.rnexchange.web.rest.SettlementBatchResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /settlement-batches?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SettlementBatchCriteria implements Serializable, Criteria {

    /**
     * Class for filtering SettlementKind
     */
    public static class SettlementKindFilter extends Filter<SettlementKind> {

        public SettlementKindFilter() {}

        public SettlementKindFilter(SettlementKindFilter filter) {
            super(filter);
        }

        @Override
        public SettlementKindFilter copy() {
            return new SettlementKindFilter(this);
        }
    }

    /**
     * Class for filtering SettlementStatus
     */
    public static class SettlementStatusFilter extends Filter<SettlementStatus> {

        public SettlementStatusFilter() {}

        public SettlementStatusFilter(SettlementStatusFilter filter) {
            super(filter);
        }

        @Override
        public SettlementStatusFilter copy() {
            return new SettlementStatusFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LocalDateFilter refDate;

    private SettlementKindFilter kind;

    private SettlementStatusFilter status;

    private StringFilter remarks;

    private LongFilter exchangeId;

    private Boolean distinct;

    public SettlementBatchCriteria() {}

    public SettlementBatchCriteria(SettlementBatchCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.refDate = other.optionalRefDate().map(LocalDateFilter::copy).orElse(null);
        this.kind = other.optionalKind().map(SettlementKindFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(SettlementStatusFilter::copy).orElse(null);
        this.remarks = other.optionalRemarks().map(StringFilter::copy).orElse(null);
        this.exchangeId = other.optionalExchangeId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public SettlementBatchCriteria copy() {
        return new SettlementBatchCriteria(this);
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

    public SettlementKindFilter getKind() {
        return kind;
    }

    public Optional<SettlementKindFilter> optionalKind() {
        return Optional.ofNullable(kind);
    }

    public SettlementKindFilter kind() {
        if (kind == null) {
            setKind(new SettlementKindFilter());
        }
        return kind;
    }

    public void setKind(SettlementKindFilter kind) {
        this.kind = kind;
    }

    public SettlementStatusFilter getStatus() {
        return status;
    }

    public Optional<SettlementStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public SettlementStatusFilter status() {
        if (status == null) {
            setStatus(new SettlementStatusFilter());
        }
        return status;
    }

    public void setStatus(SettlementStatusFilter status) {
        this.status = status;
    }

    public StringFilter getRemarks() {
        return remarks;
    }

    public Optional<StringFilter> optionalRemarks() {
        return Optional.ofNullable(remarks);
    }

    public StringFilter remarks() {
        if (remarks == null) {
            setRemarks(new StringFilter());
        }
        return remarks;
    }

    public void setRemarks(StringFilter remarks) {
        this.remarks = remarks;
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
        final SettlementBatchCriteria that = (SettlementBatchCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(refDate, that.refDate) &&
            Objects.equals(kind, that.kind) &&
            Objects.equals(status, that.status) &&
            Objects.equals(remarks, that.remarks) &&
            Objects.equals(exchangeId, that.exchangeId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, refDate, kind, status, remarks, exchangeId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SettlementBatchCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalRefDate().map(f -> "refDate=" + f + ", ").orElse("") +
            optionalKind().map(f -> "kind=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalRemarks().map(f -> "remarks=" + f + ", ").orElse("") +
            optionalExchangeId().map(f -> "exchangeId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
