package com.rnexchange.service.criteria;

import com.rnexchange.domain.enumeration.Currency;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.rnexchange.domain.LedgerEntry} entity. This class is used
 * in {@link com.rnexchange.web.rest.LedgerEntryResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /ledger-entries?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LedgerEntryCriteria implements Serializable, Criteria {

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

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter ts;

    private StringFilter type;

    private BigDecimalFilter amount;

    private CurrencyFilter ccy;

    private BigDecimalFilter balanceAfter;

    private StringFilter reference;

    private StringFilter remarks;

    private LongFilter tradingAccountId;

    private Boolean distinct;

    public LedgerEntryCriteria() {}

    public LedgerEntryCriteria(LedgerEntryCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.ts = other.optionalTs().map(InstantFilter::copy).orElse(null);
        this.type = other.optionalType().map(StringFilter::copy).orElse(null);
        this.amount = other.optionalAmount().map(BigDecimalFilter::copy).orElse(null);
        this.ccy = other.optionalCcy().map(CurrencyFilter::copy).orElse(null);
        this.balanceAfter = other.optionalBalanceAfter().map(BigDecimalFilter::copy).orElse(null);
        this.reference = other.optionalReference().map(StringFilter::copy).orElse(null);
        this.remarks = other.optionalRemarks().map(StringFilter::copy).orElse(null);
        this.tradingAccountId = other.optionalTradingAccountId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public LedgerEntryCriteria copy() {
        return new LedgerEntryCriteria(this);
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

    public InstantFilter getTs() {
        return ts;
    }

    public Optional<InstantFilter> optionalTs() {
        return Optional.ofNullable(ts);
    }

    public InstantFilter ts() {
        if (ts == null) {
            setTs(new InstantFilter());
        }
        return ts;
    }

    public void setTs(InstantFilter ts) {
        this.ts = ts;
    }

    public StringFilter getType() {
        return type;
    }

    public Optional<StringFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public StringFilter type() {
        if (type == null) {
            setType(new StringFilter());
        }
        return type;
    }

    public void setType(StringFilter type) {
        this.type = type;
    }

    public BigDecimalFilter getAmount() {
        return amount;
    }

    public Optional<BigDecimalFilter> optionalAmount() {
        return Optional.ofNullable(amount);
    }

    public BigDecimalFilter amount() {
        if (amount == null) {
            setAmount(new BigDecimalFilter());
        }
        return amount;
    }

    public void setAmount(BigDecimalFilter amount) {
        this.amount = amount;
    }

    public CurrencyFilter getCcy() {
        return ccy;
    }

    public Optional<CurrencyFilter> optionalCcy() {
        return Optional.ofNullable(ccy);
    }

    public CurrencyFilter ccy() {
        if (ccy == null) {
            setCcy(new CurrencyFilter());
        }
        return ccy;
    }

    public void setCcy(CurrencyFilter ccy) {
        this.ccy = ccy;
    }

    public BigDecimalFilter getBalanceAfter() {
        return balanceAfter;
    }

    public Optional<BigDecimalFilter> optionalBalanceAfter() {
        return Optional.ofNullable(balanceAfter);
    }

    public BigDecimalFilter balanceAfter() {
        if (balanceAfter == null) {
            setBalanceAfter(new BigDecimalFilter());
        }
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimalFilter balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public StringFilter getReference() {
        return reference;
    }

    public Optional<StringFilter> optionalReference() {
        return Optional.ofNullable(reference);
    }

    public StringFilter reference() {
        if (reference == null) {
            setReference(new StringFilter());
        }
        return reference;
    }

    public void setReference(StringFilter reference) {
        this.reference = reference;
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
        final LedgerEntryCriteria that = (LedgerEntryCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(ts, that.ts) &&
            Objects.equals(type, that.type) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(ccy, that.ccy) &&
            Objects.equals(balanceAfter, that.balanceAfter) &&
            Objects.equals(reference, that.reference) &&
            Objects.equals(remarks, that.remarks) &&
            Objects.equals(tradingAccountId, that.tradingAccountId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ts, type, amount, ccy, balanceAfter, reference, remarks, tradingAccountId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LedgerEntryCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTs().map(f -> "ts=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalAmount().map(f -> "amount=" + f + ", ").orElse("") +
            optionalCcy().map(f -> "ccy=" + f + ", ").orElse("") +
            optionalBalanceAfter().map(f -> "balanceAfter=" + f + ", ").orElse("") +
            optionalReference().map(f -> "reference=" + f + ", ").orElse("") +
            optionalRemarks().map(f -> "remarks=" + f + ", ").orElse("") +
            optionalTradingAccountId().map(f -> "tradingAccountId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
