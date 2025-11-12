package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.CorporateActionType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.CorporateAction} entity. This class is used
 * in {@link com.rnexchange.web.rest.CorporateActionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /corporate-actions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CorporateActionCriteria implements Serializable, Criteria {

    /**
     * Class for filtering CorporateActionType
     */
    public static class CorporateActionTypeFilter extends Filter<CorporateActionType> {

        public CorporateActionTypeFilter() {}

        public CorporateActionTypeFilter(CorporateActionTypeFilter filter) {
            super(filter);
        }

        @Override
        public CorporateActionTypeFilter copy() {
            return new CorporateActionTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private CorporateActionTypeFilter type;

    private StringFilter instrumentSymbol;

    private LocalDateFilter exDate;

    private LocalDateFilter payDate;

    private BigDecimalFilter ratio;

    private BigDecimalFilter cashAmount;

    private LongFilter instrumentId;

    private Boolean distinct;

    public CorporateActionCriteria() {}

    public CorporateActionCriteria(CorporateActionCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.type = other.optionalType().map(CorporateActionTypeFilter::copy).orElse(null);
        this.instrumentSymbol = other.optionalInstrumentSymbol().map(StringFilter::copy).orElse(null);
        this.exDate = other.optionalExDate().map(LocalDateFilter::copy).orElse(null);
        this.payDate = other.optionalPayDate().map(LocalDateFilter::copy).orElse(null);
        this.ratio = other.optionalRatio().map(BigDecimalFilter::copy).orElse(null);
        this.cashAmount = other.optionalCashAmount().map(BigDecimalFilter::copy).orElse(null);
        this.instrumentId = other.optionalInstrumentId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public CorporateActionCriteria copy() {
        return new CorporateActionCriteria(this);
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

    public CorporateActionTypeFilter getType() {
        return type;
    }

    public Optional<CorporateActionTypeFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public CorporateActionTypeFilter type() {
        if (type == null) {
            setType(new CorporateActionTypeFilter());
        }
        return type;
    }

    public void setType(CorporateActionTypeFilter type) {
        this.type = type;
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

    public LocalDateFilter getExDate() {
        return exDate;
    }

    public Optional<LocalDateFilter> optionalExDate() {
        return Optional.ofNullable(exDate);
    }

    public LocalDateFilter exDate() {
        if (exDate == null) {
            setExDate(new LocalDateFilter());
        }
        return exDate;
    }

    public void setExDate(LocalDateFilter exDate) {
        this.exDate = exDate;
    }

    public LocalDateFilter getPayDate() {
        return payDate;
    }

    public Optional<LocalDateFilter> optionalPayDate() {
        return Optional.ofNullable(payDate);
    }

    public LocalDateFilter payDate() {
        if (payDate == null) {
            setPayDate(new LocalDateFilter());
        }
        return payDate;
    }

    public void setPayDate(LocalDateFilter payDate) {
        this.payDate = payDate;
    }

    public BigDecimalFilter getRatio() {
        return ratio;
    }

    public Optional<BigDecimalFilter> optionalRatio() {
        return Optional.ofNullable(ratio);
    }

    public BigDecimalFilter ratio() {
        if (ratio == null) {
            setRatio(new BigDecimalFilter());
        }
        return ratio;
    }

    public void setRatio(BigDecimalFilter ratio) {
        this.ratio = ratio;
    }

    public BigDecimalFilter getCashAmount() {
        return cashAmount;
    }

    public Optional<BigDecimalFilter> optionalCashAmount() {
        return Optional.ofNullable(cashAmount);
    }

    public BigDecimalFilter cashAmount() {
        if (cashAmount == null) {
            setCashAmount(new BigDecimalFilter());
        }
        return cashAmount;
    }

    public void setCashAmount(BigDecimalFilter cashAmount) {
        this.cashAmount = cashAmount;
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
        final CorporateActionCriteria that = (CorporateActionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(type, that.type) &&
            Objects.equals(instrumentSymbol, that.instrumentSymbol) &&
            Objects.equals(exDate, that.exDate) &&
            Objects.equals(payDate, that.payDate) &&
            Objects.equals(ratio, that.ratio) &&
            Objects.equals(cashAmount, that.cashAmount) &&
            Objects.equals(instrumentId, that.instrumentId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, instrumentSymbol, exDate, payDate, ratio, cashAmount, instrumentId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CorporateActionCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalInstrumentSymbol().map(f -> "instrumentSymbol=" + f + ", ").orElse("") +
            optionalExDate().map(f -> "exDate=" + f + ", ").orElse("") +
            optionalPayDate().map(f -> "payDate=" + f + ", ").orElse("") +
            optionalRatio().map(f -> "ratio=" + f + ", ").orElse("") +
            optionalCashAmount().map(f -> "cashAmount=" + f + ", ").orElse("") +
            optionalInstrumentId().map(f -> "instrumentId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
