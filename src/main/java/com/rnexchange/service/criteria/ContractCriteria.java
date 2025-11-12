package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.ContractType;
import com.rnexchange.domain.enumeration.OptionType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.Contract} entity. This class is used
 * in {@link com.rnexchange.web.rest.ContractResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /contracts?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ContractCriteria implements Serializable, Criteria {

    /**
     * Class for filtering ContractType
     */
    public static class ContractTypeFilter extends Filter<ContractType> {

        public ContractTypeFilter() {}

        public ContractTypeFilter(ContractTypeFilter filter) {
            super(filter);
        }

        @Override
        public ContractTypeFilter copy() {
            return new ContractTypeFilter(this);
        }
    }

    /**
     * Class for filtering OptionType
     */
    public static class OptionTypeFilter extends Filter<OptionType> {

        public OptionTypeFilter() {}

        public OptionTypeFilter(OptionTypeFilter filter) {
            super(filter);
        }

        @Override
        public OptionTypeFilter copy() {
            return new OptionTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter instrumentSymbol;

    private ContractTypeFilter contractType;

    private LocalDateFilter expiry;

    private BigDecimalFilter strike;

    private OptionTypeFilter optionType;

    private StringFilter segment;

    private LongFilter instrumentId;

    private Boolean distinct;

    public ContractCriteria() {}

    public ContractCriteria(ContractCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.instrumentSymbol = other.optionalInstrumentSymbol().map(StringFilter::copy).orElse(null);
        this.contractType = other.optionalContractType().map(ContractTypeFilter::copy).orElse(null);
        this.expiry = other.optionalExpiry().map(LocalDateFilter::copy).orElse(null);
        this.strike = other.optionalStrike().map(BigDecimalFilter::copy).orElse(null);
        this.optionType = other.optionalOptionType().map(OptionTypeFilter::copy).orElse(null);
        this.segment = other.optionalSegment().map(StringFilter::copy).orElse(null);
        this.instrumentId = other.optionalInstrumentId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ContractCriteria copy() {
        return new ContractCriteria(this);
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

    public ContractTypeFilter getContractType() {
        return contractType;
    }

    public Optional<ContractTypeFilter> optionalContractType() {
        return Optional.ofNullable(contractType);
    }

    public ContractTypeFilter contractType() {
        if (contractType == null) {
            setContractType(new ContractTypeFilter());
        }
        return contractType;
    }

    public void setContractType(ContractTypeFilter contractType) {
        this.contractType = contractType;
    }

    public LocalDateFilter getExpiry() {
        return expiry;
    }

    public Optional<LocalDateFilter> optionalExpiry() {
        return Optional.ofNullable(expiry);
    }

    public LocalDateFilter expiry() {
        if (expiry == null) {
            setExpiry(new LocalDateFilter());
        }
        return expiry;
    }

    public void setExpiry(LocalDateFilter expiry) {
        this.expiry = expiry;
    }

    public BigDecimalFilter getStrike() {
        return strike;
    }

    public Optional<BigDecimalFilter> optionalStrike() {
        return Optional.ofNullable(strike);
    }

    public BigDecimalFilter strike() {
        if (strike == null) {
            setStrike(new BigDecimalFilter());
        }
        return strike;
    }

    public void setStrike(BigDecimalFilter strike) {
        this.strike = strike;
    }

    public OptionTypeFilter getOptionType() {
        return optionType;
    }

    public Optional<OptionTypeFilter> optionalOptionType() {
        return Optional.ofNullable(optionType);
    }

    public OptionTypeFilter optionType() {
        if (optionType == null) {
            setOptionType(new OptionTypeFilter());
        }
        return optionType;
    }

    public void setOptionType(OptionTypeFilter optionType) {
        this.optionType = optionType;
    }

    public StringFilter getSegment() {
        return segment;
    }

    public Optional<StringFilter> optionalSegment() {
        return Optional.ofNullable(segment);
    }

    public StringFilter segment() {
        if (segment == null) {
            setSegment(new StringFilter());
        }
        return segment;
    }

    public void setSegment(StringFilter segment) {
        this.segment = segment;
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
        final ContractCriteria that = (ContractCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(instrumentSymbol, that.instrumentSymbol) &&
            Objects.equals(contractType, that.contractType) &&
            Objects.equals(expiry, that.expiry) &&
            Objects.equals(strike, that.strike) &&
            Objects.equals(optionType, that.optionType) &&
            Objects.equals(segment, that.segment) &&
            Objects.equals(instrumentId, that.instrumentId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, instrumentSymbol, contractType, expiry, strike, optionType, segment, instrumentId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ContractCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalInstrumentSymbol().map(f -> "instrumentSymbol=" + f + ", ").orElse("") +
            optionalContractType().map(f -> "contractType=" + f + ", ").orElse("") +
            optionalExpiry().map(f -> "expiry=" + f + ", ").orElse("") +
            optionalStrike().map(f -> "strike=" + f + ", ").orElse("") +
            optionalOptionType().map(f -> "optionType=" + f + ", ").orElse("") +
            optionalSegment().map(f -> "segment=" + f + ", ").orElse("") +
            optionalInstrumentId().map(f -> "instrumentId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
