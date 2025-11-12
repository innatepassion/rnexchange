package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.AccountType;
import com.rnexchange.domain.enumeration.Currency;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.TradingAccount} entity. This class is used
 * in {@link com.rnexchange.web.rest.TradingAccountResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /trading-accounts?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TradingAccountCriteria implements Serializable, Criteria {

    /**
     * Class for filtering AccountType
     */
    public static class AccountTypeFilter extends Filter<AccountType> {

        public AccountTypeFilter() {}

        public AccountTypeFilter(AccountTypeFilter filter) {
            super(filter);
        }

        @Override
        public AccountTypeFilter copy() {
            return new AccountTypeFilter(this);
        }
    }

    /**
     * Class for filtering Currency
     */
    public static class CurrencyFilter extends Filter<Currency> {

        public CurrencyFilter() {}

        public CurrencyFilter(CurrencyFilter filter) {
            super(filter);
        }

        @Override
        public CurrencyFilter copy() {
            return new CurrencyFilter(this);
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

    private AccountTypeFilter type;

    private CurrencyFilter baseCcy;

    private BigDecimalFilter balance;

    private AccountStatusFilter status;

    private LongFilter brokerId;

    private LongFilter traderId;

    private Boolean distinct;

    public TradingAccountCriteria() {}

    public TradingAccountCriteria(TradingAccountCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.type = other.optionalType().map(AccountTypeFilter::copy).orElse(null);
        this.baseCcy = other.optionalBaseCcy().map(CurrencyFilter::copy).orElse(null);
        this.balance = other.optionalBalance().map(BigDecimalFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(AccountStatusFilter::copy).orElse(null);
        this.brokerId = other.optionalBrokerId().map(LongFilter::copy).orElse(null);
        this.traderId = other.optionalTraderId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public TradingAccountCriteria copy() {
        return new TradingAccountCriteria(this);
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

    public AccountTypeFilter getType() {
        return type;
    }

    public Optional<AccountTypeFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public AccountTypeFilter type() {
        if (type == null) {
            setType(new AccountTypeFilter());
        }
        return type;
    }

    public void setType(AccountTypeFilter type) {
        this.type = type;
    }

    public CurrencyFilter getBaseCcy() {
        return baseCcy;
    }

    public Optional<CurrencyFilter> optionalBaseCcy() {
        return Optional.ofNullable(baseCcy);
    }

    public CurrencyFilter baseCcy() {
        if (baseCcy == null) {
            setBaseCcy(new CurrencyFilter());
        }
        return baseCcy;
    }

    public void setBaseCcy(CurrencyFilter baseCcy) {
        this.baseCcy = baseCcy;
    }

    public BigDecimalFilter getBalance() {
        return balance;
    }

    public Optional<BigDecimalFilter> optionalBalance() {
        return Optional.ofNullable(balance);
    }

    public BigDecimalFilter balance() {
        if (balance == null) {
            setBalance(new BigDecimalFilter());
        }
        return balance;
    }

    public void setBalance(BigDecimalFilter balance) {
        this.balance = balance;
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

    public LongFilter getBrokerId() {
        return brokerId;
    }

    public Optional<LongFilter> optionalBrokerId() {
        return Optional.ofNullable(brokerId);
    }

    public LongFilter brokerId() {
        if (brokerId == null) {
            setBrokerId(new LongFilter());
        }
        return brokerId;
    }

    public void setBrokerId(LongFilter brokerId) {
        this.brokerId = brokerId;
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
        final TradingAccountCriteria that = (TradingAccountCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(type, that.type) &&
            Objects.equals(baseCcy, that.baseCcy) &&
            Objects.equals(balance, that.balance) &&
            Objects.equals(status, that.status) &&
            Objects.equals(brokerId, that.brokerId) &&
            Objects.equals(traderId, that.traderId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, baseCcy, balance, status, brokerId, traderId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TradingAccountCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalBaseCcy().map(f -> "baseCcy=" + f + ", ").orElse("") +
            optionalBalance().map(f -> "balance=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalBrokerId().map(f -> "brokerId=" + f + ", ").orElse("") +
            optionalTraderId().map(f -> "traderId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
