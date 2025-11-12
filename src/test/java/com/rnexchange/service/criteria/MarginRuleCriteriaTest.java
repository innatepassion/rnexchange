package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class MarginRuleCriteriaTest {

    @Test
    void newMarginRuleCriteriaHasAllFiltersNullTest() {
        var marginRuleCriteria = new MarginRuleCriteria();
        assertThat(marginRuleCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void marginRuleCriteriaFluentMethodsCreatesFiltersTest() {
        var marginRuleCriteria = new MarginRuleCriteria();

        setAllFilters(marginRuleCriteria);

        assertThat(marginRuleCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void marginRuleCriteriaCopyCreatesNullFilterTest() {
        var marginRuleCriteria = new MarginRuleCriteria();
        var copy = marginRuleCriteria.copy();

        assertThat(marginRuleCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(marginRuleCriteria)
        );
    }

    @Test
    void marginRuleCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var marginRuleCriteria = new MarginRuleCriteria();
        setAllFilters(marginRuleCriteria);

        var copy = marginRuleCriteria.copy();

        assertThat(marginRuleCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(marginRuleCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var marginRuleCriteria = new MarginRuleCriteria();

        assertThat(marginRuleCriteria).hasToString("MarginRuleCriteria{}");
    }

    private static void setAllFilters(MarginRuleCriteria marginRuleCriteria) {
        marginRuleCriteria.id();
        marginRuleCriteria.scope();
        marginRuleCriteria.initialPct();
        marginRuleCriteria.maintPct();
        marginRuleCriteria.exchangeId();
        marginRuleCriteria.distinct();
    }

    private static Condition<MarginRuleCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getScope()) &&
                condition.apply(criteria.getInitialPct()) &&
                condition.apply(criteria.getMaintPct()) &&
                condition.apply(criteria.getExchangeId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<MarginRuleCriteria> copyFiltersAre(MarginRuleCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getScope(), copy.getScope()) &&
                condition.apply(criteria.getInitialPct(), copy.getInitialPct()) &&
                condition.apply(criteria.getMaintPct(), copy.getMaintPct()) &&
                condition.apply(criteria.getExchangeId(), copy.getExchangeId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
