package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.ExchangeStatus;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.Exchange} entity. This class is used
 * in {@link com.rnexchange.web.rest.ExchangeResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /exchanges?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExchangeCriteria implements Serializable, Criteria {

    /**
     * Class for filtering ExchangeStatus
     */
    public static class ExchangeStatusFilter extends Filter<ExchangeStatus> {

        public ExchangeStatusFilter() {}

        public ExchangeStatusFilter(ExchangeStatusFilter filter) {
            super(filter);
        }

        @Override
        public ExchangeStatusFilter copy() {
            return new ExchangeStatusFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter code;

    private StringFilter name;

    private StringFilter timezone;

    private ExchangeStatusFilter status;

    private Boolean distinct;

    public ExchangeCriteria() {}

    public ExchangeCriteria(ExchangeCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.code = other.optionalCode().map(StringFilter::copy).orElse(null);
        this.name = other.optionalName().map(StringFilter::copy).orElse(null);
        this.timezone = other.optionalTimezone().map(StringFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(ExchangeStatusFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ExchangeCriteria copy() {
        return new ExchangeCriteria(this);
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

    public StringFilter getCode() {
        return code;
    }

    public Optional<StringFilter> optionalCode() {
        return Optional.ofNullable(code);
    }

    public StringFilter code() {
        if (code == null) {
            setCode(new StringFilter());
        }
        return code;
    }

    public void setCode(StringFilter code) {
        this.code = code;
    }

    public StringFilter getName() {
        return name;
    }

    public Optional<StringFilter> optionalName() {
        return Optional.ofNullable(name);
    }

    public StringFilter name() {
        if (name == null) {
            setName(new StringFilter());
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getTimezone() {
        return timezone;
    }

    public Optional<StringFilter> optionalTimezone() {
        return Optional.ofNullable(timezone);
    }

    public StringFilter timezone() {
        if (timezone == null) {
            setTimezone(new StringFilter());
        }
        return timezone;
    }

    public void setTimezone(StringFilter timezone) {
        this.timezone = timezone;
    }

    public ExchangeStatusFilter getStatus() {
        return status;
    }

    public Optional<ExchangeStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public ExchangeStatusFilter status() {
        if (status == null) {
            setStatus(new ExchangeStatusFilter());
        }
        return status;
    }

    public void setStatus(ExchangeStatusFilter status) {
        this.status = status;
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
        final ExchangeCriteria that = (ExchangeCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(code, that.code) &&
            Objects.equals(name, that.name) &&
            Objects.equals(timezone, that.timezone) &&
            Objects.equals(status, that.status) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name, timezone, status, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExchangeCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalCode().map(f -> "code=" + f + ", ").orElse("") +
            optionalName().map(f -> "name=" + f + ", ").orElse("") +
            optionalTimezone().map(f -> "timezone=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
