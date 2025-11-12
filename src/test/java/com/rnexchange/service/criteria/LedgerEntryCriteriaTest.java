package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class LedgerEntryCriteriaTest {

    @Test
    void newLedgerEntryCriteriaHasAllFiltersNullTest() {
        var ledgerEntryCriteria = new LedgerEntryCriteria();
        assertThat(ledgerEntryCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void ledgerEntryCriteriaFluentMethodsCreatesFiltersTest() {
        var ledgerEntryCriteria = new LedgerEntryCriteria();

        setAllFilters(ledgerEntryCriteria);

        assertThat(ledgerEntryCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void ledgerEntryCriteriaCopyCreatesNullFilterTest() {
        var ledgerEntryCriteria = new LedgerEntryCriteria();
        var copy = ledgerEntryCriteria.copy();

        assertThat(ledgerEntryCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(ledgerEntryCriteria)
        );
    }

    @Test
    void ledgerEntryCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var ledgerEntryCriteria = new LedgerEntryCriteria();
        setAllFilters(ledgerEntryCriteria);

        var copy = ledgerEntryCriteria.copy();

        assertThat(ledgerEntryCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(ledgerEntryCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var ledgerEntryCriteria = new LedgerEntryCriteria();

        assertThat(ledgerEntryCriteria).hasToString("LedgerEntryCriteria{}");
    }

    private static void setAllFilters(LedgerEntryCriteria ledgerEntryCriteria) {
        ledgerEntryCriteria.id();
        ledgerEntryCriteria.ts();
        ledgerEntryCriteria.type();
        ledgerEntryCriteria.amount();
        ledgerEntryCriteria.ccy();
        ledgerEntryCriteria.balanceAfter();
        ledgerEntryCriteria.reference();
        ledgerEntryCriteria.remarks();
        ledgerEntryCriteria.tradingAccountId();
        ledgerEntryCriteria.distinct();
    }

    private static Condition<LedgerEntryCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTs()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getAmount()) &&
                condition.apply(criteria.getCcy()) &&
                condition.apply(criteria.getBalanceAfter()) &&
                condition.apply(criteria.getReference()) &&
                condition.apply(criteria.getRemarks()) &&
                condition.apply(criteria.getTradingAccountId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<LedgerEntryCriteria> copyFiltersAre(LedgerEntryCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTs(), copy.getTs()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getAmount(), copy.getAmount()) &&
                condition.apply(criteria.getCcy(), copy.getCcy()) &&
                condition.apply(criteria.getBalanceAfter(), copy.getBalanceAfter()) &&
                condition.apply(criteria.getReference(), copy.getReference()) &&
                condition.apply(criteria.getRemarks(), copy.getRemarks()) &&
                condition.apply(criteria.getTradingAccountId(), copy.getTradingAccountId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
