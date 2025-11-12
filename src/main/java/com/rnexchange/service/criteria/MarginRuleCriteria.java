package com.rnexchange.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.MarginRule} entity. This class is used
 * in {@link com.rnexchange.web.rest.MarginRuleResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /margin-rules?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MarginRuleCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter scope;

    private BigDecimalFilter initialPct;

    private BigDecimalFilter maintPct;

    private LongFilter exchangeId;

    private Boolean distinct;

    public MarginRuleCriteria() {}

    public MarginRuleCriteria(MarginRuleCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.scope = other.optionalScope().map(StringFilter::copy).orElse(null);
        this.initialPct = other.optionalInitialPct().map(BigDecimalFilter::copy).orElse(null);
        this.maintPct = other.optionalMaintPct().map(BigDecimalFilter::copy).orElse(null);
        this.exchangeId = other.optionalExchangeId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public MarginRuleCriteria copy() {
        return new MarginRuleCriteria(this);
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

    public StringFilter getScope() {
        return scope;
    }

    public Optional<StringFilter> optionalScope() {
        return Optional.ofNullable(scope);
    }

    public StringFilter scope() {
        if (scope == null) {
            setScope(new StringFilter());
        }
        return scope;
    }

    public void setScope(StringFilter scope) {
        this.scope = scope;
    }

    public BigDecimalFilter getInitialPct() {
        return initialPct;
    }

    public Optional<BigDecimalFilter> optionalInitialPct() {
        return Optional.ofNullable(initialPct);
    }

    public BigDecimalFilter initialPct() {
        if (initialPct == null) {
            setInitialPct(new BigDecimalFilter());
        }
        return initialPct;
    }

    public void setInitialPct(BigDecimalFilter initialPct) {
        this.initialPct = initialPct;
    }

    public BigDecimalFilter getMaintPct() {
        return maintPct;
    }

    public Optional<BigDecimalFilter> optionalMaintPct() {
        return Optional.ofNullable(maintPct);
    }

    public BigDecimalFilter maintPct() {
        if (maintPct == null) {
            setMaintPct(new BigDecimalFilter());
        }
        return maintPct;
    }

    public void setMaintPct(BigDecimalFilter maintPct) {
        this.maintPct = maintPct;
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
        final MarginRuleCriteria that = (MarginRuleCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(scope, that.scope) &&
            Objects.equals(initialPct, that.initialPct) &&
            Objects.equals(maintPct, that.maintPct) &&
            Objects.equals(exchangeId, that.exchangeId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, scope, initialPct, maintPct, exchangeId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MarginRuleCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalScope().map(f -> "scope=" + f + ", ").orElse("") +
            optionalInitialPct().map(f -> "initialPct=" + f + ", ").orElse("") +
            optionalMaintPct().map(f -> "maintPct=" + f + ", ").orElse("") +
            optionalExchangeId().map(f -> "exchangeId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
