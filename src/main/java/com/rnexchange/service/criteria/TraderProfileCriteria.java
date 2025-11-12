package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.KycStatus;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.TraderProfile} entity. This class is used
 * in {@link com.rnexchange.web.rest.TraderProfileResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /trader-profiles?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TraderProfileCriteria implements Serializable, Criteria {

    /**
     * Class for filtering KycStatus
     */
    public static class KycStatusFilter extends Filter<KycStatus> {

        public KycStatusFilter() {}

        public KycStatusFilter(KycStatusFilter filter) {
            super(filter);
        }

        @Override
        public KycStatusFilter copy() {
            return new KycStatusFilter(this);
        }
    }

    /**
     * Class for filtering AccountStatus
     */
    public static class AccountStatusFilter extends Filter<AccountStatus> {

        public AccountStatusFilter() {}

        public AccountStatusFilter(AccountStatusFilter filter) {
            super(filter);
        }

        @Override
        public AccountStatusFilter copy() {
            return new AccountStatusFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter displayName;

    private StringFilter email;

    private StringFilter mobile;

    private KycStatusFilter kycStatus;

    private AccountStatusFilter status;

    private LongFilter userId;

    private Boolean distinct;

    public TraderProfileCriteria() {}

    public TraderProfileCriteria(TraderProfileCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.displayName = other.optionalDisplayName().map(StringFilter::copy).orElse(null);
        this.email = other.optionalEmail().map(StringFilter::copy).orElse(null);
        this.mobile = other.optionalMobile().map(StringFilter::copy).orElse(null);
        this.kycStatus = other.optionalKycStatus().map(KycStatusFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(AccountStatusFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public TraderProfileCriteria copy() {
        return new TraderProfileCriteria(this);
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

    public StringFilter getDisplayName() {
        return displayName;
    }

    public Optional<StringFilter> optionalDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public StringFilter displayName() {
        if (displayName == null) {
            setDisplayName(new StringFilter());
        }
        return displayName;
    }

    public void setDisplayName(StringFilter displayName) {
        this.displayName = displayName;
    }

    public StringFilter getEmail() {
        return email;
    }

    public Optional<StringFilter> optionalEmail() {
        return Optional.ofNullable(email);
    }

    public StringFilter email() {
        if (email == null) {
            setEmail(new StringFilter());
        }
        return email;
    }

    public void setEmail(StringFilter email) {
        this.email = email;
    }

    public StringFilter getMobile() {
        return mobile;
    }

    public Optional<StringFilter> optionalMobile() {
        return Optional.ofNullable(mobile);
    }

    public StringFilter mobile() {
        if (mobile == null) {
            setMobile(new StringFilter());
        }
        return mobile;
    }

    public void setMobile(StringFilter mobile) {
        this.mobile = mobile;
    }

    public KycStatusFilter getKycStatus() {
        return kycStatus;
    }

    public Optional<KycStatusFilter> optionalKycStatus() {
        return Optional.ofNullable(kycStatus);
    }

    public KycStatusFilter kycStatus() {
        if (kycStatus == null) {
            setKycStatus(new KycStatusFilter());
        }
        return kycStatus;
    }

    public void setKycStatus(KycStatusFilter kycStatus) {
        this.kycStatus = kycStatus;
    }

    public AccountStatusFilter getStatus() {
        return status;
    }

    public Optional<AccountStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public AccountStatusFilter status() {
        if (status == null) {
            setStatus(new AccountStatusFilter());
        }
        return status;
    }

    public void setStatus(AccountStatusFilter status) {
        this.status = status;
    }

    public LongFilter getUserId() {
        return userId;
    }

    public Optional<LongFilter> optionalUserId() {
        return Optional.ofNullable(userId);
    }

    public LongFilter userId() {
        if (userId == null) {
            setUserId(new LongFilter());
        }
        return userId;
    }

    public void setUserId(LongFilter userId) {
        this.userId = userId;
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
        final TraderProfileCriteria that = (TraderProfileCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(displayName, that.displayName) &&
            Objects.equals(email, that.email) &&
            Objects.equals(mobile, that.mobile) &&
            Objects.equals(kycStatus, that.kycStatus) &&
            Objects.equals(status, that.status) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, email, mobile, kycStatus, status, userId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TraderProfileCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalDisplayName().map(f -> "displayName=" + f + ", ").orElse("") +
            optionalEmail().map(f -> "email=" + f + ", ").orElse("") +
            optionalMobile().map(f -> "mobile=" + f + ", ").orElse("") +
            optionalKycStatus().map(f -> "kycStatus=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
