package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.IntegrationStatus;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.ExchangeIntegration} entity. This class is used
 * in {@link com.rnexchange.web.rest.ExchangeIntegrationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /exchange-integrations?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExchangeIntegrationCriteria implements Serializable, Criteria {

    /**
     * Class for filtering IntegrationStatus
     */
    public static class IntegrationStatusFilter extends Filter<IntegrationStatus> {

        public IntegrationStatusFilter() {}

        public IntegrationStatusFilter(IntegrationStatusFilter filter) {
            super(filter);
        }

        @Override
        public IntegrationStatusFilter copy() {
            return new IntegrationStatusFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter provider;

    private StringFilter apiKey;

    private StringFilter apiSecret;

    private IntegrationStatusFilter status;

    private InstantFilter lastHeartbeat;

    private LongFilter exchangeId;

    private Boolean distinct;

    public ExchangeIntegrationCriteria() {}

    public ExchangeIntegrationCriteria(ExchangeIntegrationCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.provider = other.optionalProvider().map(StringFilter::copy).orElse(null);
        this.apiKey = other.optionalApiKey().map(StringFilter::copy).orElse(null);
        this.apiSecret = other.optionalApiSecret().map(StringFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(IntegrationStatusFilter::copy).orElse(null);
        this.lastHeartbeat = other.optionalLastHeartbeat().map(InstantFilter::copy).orElse(null);
        this.exchangeId = other.optionalExchangeId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ExchangeIntegrationCriteria copy() {
        return new ExchangeIntegrationCriteria(this);
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

    public StringFilter getProvider() {
        return provider;
    }

    public Optional<StringFilter> optionalProvider() {
        return Optional.ofNullable(provider);
    }

    public StringFilter provider() {
        if (provider == null) {
            setProvider(new StringFilter());
        }
        return provider;
    }

    public void setProvider(StringFilter provider) {
        this.provider = provider;
    }

    public StringFilter getApiKey() {
        return apiKey;
    }

    public Optional<StringFilter> optionalApiKey() {
        return Optional.ofNullable(apiKey);
    }

    public StringFilter apiKey() {
        if (apiKey == null) {
            setApiKey(new StringFilter());
        }
        return apiKey;
    }

    public void setApiKey(StringFilter apiKey) {
        this.apiKey = apiKey;
    }

    public StringFilter getApiSecret() {
        return apiSecret;
    }

    public Optional<StringFilter> optionalApiSecret() {
        return Optional.ofNullable(apiSecret);
    }

    public StringFilter apiSecret() {
        if (apiSecret == null) {
            setApiSecret(new StringFilter());
        }
        return apiSecret;
    }

    public void setApiSecret(StringFilter apiSecret) {
        this.apiSecret = apiSecret;
    }

    public IntegrationStatusFilter getStatus() {
        return status;
    }

    public Optional<IntegrationStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public IntegrationStatusFilter status() {
        if (status == null) {
            setStatus(new IntegrationStatusFilter());
        }
        return status;
    }

    public void setStatus(IntegrationStatusFilter status) {
        this.status = status;
    }

    public InstantFilter getLastHeartbeat() {
        return lastHeartbeat;
    }

    public Optional<InstantFilter> optionalLastHeartbeat() {
        return Optional.ofNullable(lastHeartbeat);
    }

    public InstantFilter lastHeartbeat() {
        if (lastHeartbeat == null) {
            setLastHeartbeat(new InstantFilter());
        }
        return lastHeartbeat;
    }

    public void setLastHeartbeat(InstantFilter lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
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
        final ExchangeIntegrationCriteria that = (ExchangeIntegrationCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(provider, that.provider) &&
            Objects.equals(apiKey, that.apiKey) &&
            Objects.equals(apiSecret, that.apiSecret) &&
            Objects.equals(status, that.status) &&
            Objects.equals(lastHeartbeat, that.lastHeartbeat) &&
            Objects.equals(exchangeId, that.exchangeId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, provider, apiKey, apiSecret, status, lastHeartbeat, exchangeId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExchangeIntegrationCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalProvider().map(f -> "provider=" + f + ", ").orElse("") +
            optionalApiKey().map(f -> "apiKey=" + f + ", ").orElse("") +
            optionalApiSecret().map(f -> "apiSecret=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalLastHeartbeat().map(f -> "lastHeartbeat=" + f + ", ").orElse("") +
            optionalExchangeId().map(f -> "exchangeId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
