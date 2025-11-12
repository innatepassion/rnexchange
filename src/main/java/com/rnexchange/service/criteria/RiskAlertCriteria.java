package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.AlertType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.RiskAlert} entity. This class is used
 * in {@link com.rnexchange.web.rest.RiskAlertResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /risk-alerts?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RiskAlertCriteria implements Serializable, Criteria {

    /**
     * Class for filtering AlertType
     */
    public static class AlertTypeFilter extends Filter<AlertType> {

        public AlertTypeFilter() {}

        public AlertTypeFilter(AlertTypeFilter filter) {
            super(filter);
        }

        @Override
        public AlertTypeFilter copy() {
            return new AlertTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private AlertTypeFilter alertType;

    private StringFilter description;

    private InstantFilter createdAt;

    private LongFilter tradingAccountId;

    private LongFilter traderId;

    private Boolean distinct;

    public RiskAlertCriteria() {}

    public RiskAlertCriteria(RiskAlertCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.alertType = other.optionalAlertType().map(AlertTypeFilter::copy).orElse(null);
        this.description = other.optionalDescription().map(StringFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.tradingAccountId = other.optionalTradingAccountId().map(LongFilter::copy).orElse(null);
        this.traderId = other.optionalTraderId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public RiskAlertCriteria copy() {
        return new RiskAlertCriteria(this);
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

    public AlertTypeFilter getAlertType() {
        return alertType;
    }

    public Optional<AlertTypeFilter> optionalAlertType() {
        return Optional.ofNullable(alertType);
    }

    public AlertTypeFilter alertType() {
        if (alertType == null) {
            setAlertType(new AlertTypeFilter());
        }
        return alertType;
    }

    public void setAlertType(AlertTypeFilter alertType) {
        this.alertType = alertType;
    }

    public StringFilter getDescription() {
        return description;
    }

    public Optional<StringFilter> optionalDescription() {
        return Optional.ofNullable(description);
    }

    public StringFilter description() {
        if (description == null) {
            setDescription(new StringFilter());
        }
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
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

    public LongFilter getTraderId() {
        return traderId;
    }

    public Optional<LongFilter> optionalTraderId() {
        return Optional.ofNullable(traderId);
    }

    public LongFilter traderId() {
        if (traderId == null) {
            setTraderId(new LongFilter());
        }
        return traderId;
    }

    public void setTraderId(LongFilter traderId) {
        this.traderId = traderId;
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
        final RiskAlertCriteria that = (RiskAlertCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(alertType, that.alertType) &&
            Objects.equals(description, that.description) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(tradingAccountId, that.tradingAccountId) &&
            Objects.equals(traderId, that.traderId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, alertType, description, createdAt, tradingAccountId, traderId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RiskAlertCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalAlertType().map(f -> "alertType=" + f + ", ").orElse("") +
            optionalDescription().map(f -> "description=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalTradingAccountId().map(f -> "tradingAccountId=" + f + ", ").orElse("") +
            optionalTraderId().map(f -> "traderId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
