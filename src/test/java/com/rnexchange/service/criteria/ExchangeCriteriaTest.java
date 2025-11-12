package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ExchangeCriteriaTest {

    @Test
    void newExchangeCriteriaHasAllFiltersNullTest() {
        var exchangeCriteria = new ExchangeCriteria();
        assertThat(exchangeCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void exchangeCriteriaFluentMethodsCreatesFiltersTest() {
        var exchangeCriteria = new ExchangeCriteria();

        setAllFilters(exchangeCriteria);

        assertThat(exchangeCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void exchangeCriteriaCopyCreatesNullFilterTest() {
        var exchangeCriteria = new ExchangeCriteria();
        var copy = exchangeCriteria.copy();

        assertThat(exchangeCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(exchangeCriteria)
        );
    }

    @Test
    void exchangeCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var exchangeCriteria = new ExchangeCriteria();
        setAllFilters(exchangeCriteria);

        var copy = exchangeCriteria.copy();

        assertThat(exchangeCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(exchangeCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var exchangeCriteria = new ExchangeCriteria();

        assertThat(exchangeCriteria).hasToString("ExchangeCriteria{}");
    }

    private static void setAllFilters(ExchangeCriteria exchangeCriteria) {
        exchangeCriteria.id();
        exchangeCriteria.code();
        exchangeCriteria.name();
        exchangeCriteria.timezone();
        exchangeCriteria.status();
        exchangeCriteria.distinct();
    }

    private static Condition<ExchangeCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getCode()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getTimezone()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ExchangeCriteria> copyFiltersAre(ExchangeCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getCode(), copy.getCode()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getTimezone(), copy.getTimezone()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
