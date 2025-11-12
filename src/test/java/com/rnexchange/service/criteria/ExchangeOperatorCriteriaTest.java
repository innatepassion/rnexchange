package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ExchangeOperatorCriteriaTest {

    @Test
    void newExchangeOperatorCriteriaHasAllFiltersNullTest() {
        var exchangeOperatorCriteria = new ExchangeOperatorCriteria();
        assertThat(exchangeOperatorCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void exchangeOperatorCriteriaFluentMethodsCreatesFiltersTest() {
        var exchangeOperatorCriteria = new ExchangeOperatorCriteria();

        setAllFilters(exchangeOperatorCriteria);

        assertThat(exchangeOperatorCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void exchangeOperatorCriteriaCopyCreatesNullFilterTest() {
        var exchangeOperatorCriteria = new ExchangeOperatorCriteria();
        var copy = exchangeOperatorCriteria.copy();

        assertThat(exchangeOperatorCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(exchangeOperatorCriteria)
        );
    }

    @Test
    void exchangeOperatorCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var exchangeOperatorCriteria = new ExchangeOperatorCriteria();
        setAllFilters(exchangeOperatorCriteria);

        var copy = exchangeOperatorCriteria.copy();

        assertThat(exchangeOperatorCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(exchangeOperatorCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var exchangeOperatorCriteria = new ExchangeOperatorCriteria();

        assertThat(exchangeOperatorCriteria).hasToString("ExchangeOperatorCriteria{}");
    }

    private static void setAllFilters(ExchangeOperatorCriteria exchangeOperatorCriteria) {
        exchangeOperatorCriteria.id();
        exchangeOperatorCriteria.name();
        exchangeOperatorCriteria.userId();
        exchangeOperatorCriteria.exchangeId();
        exchangeOperatorCriteria.distinct();
    }

    private static Condition<ExchangeOperatorCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getExchangeId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ExchangeOperatorCriteria> copyFiltersAre(
        ExchangeOperatorCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getExchangeId(), copy.getExchangeId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
