package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class RiskAlertCriteriaTest {

    @Test
    void newRiskAlertCriteriaHasAllFiltersNullTest() {
        var riskAlertCriteria = new RiskAlertCriteria();
        assertThat(riskAlertCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void riskAlertCriteriaFluentMethodsCreatesFiltersTest() {
        var riskAlertCriteria = new RiskAlertCriteria();

        setAllFilters(riskAlertCriteria);

        assertThat(riskAlertCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void riskAlertCriteriaCopyCreatesNullFilterTest() {
        var riskAlertCriteria = new RiskAlertCriteria();
        var copy = riskAlertCriteria.copy();

        assertThat(riskAlertCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(riskAlertCriteria)
        );
    }

    @Test
    void riskAlertCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var riskAlertCriteria = new RiskAlertCriteria();
        setAllFilters(riskAlertCriteria);

        var copy = riskAlertCriteria.copy();

        assertThat(riskAlertCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(riskAlertCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var riskAlertCriteria = new RiskAlertCriteria();

        assertThat(riskAlertCriteria).hasToString("RiskAlertCriteria{}");
    }

    private static void setAllFilters(RiskAlertCriteria riskAlertCriteria) {
        riskAlertCriteria.id();
        riskAlertCriteria.alertType();
        riskAlertCriteria.description();
        riskAlertCriteria.createdAt();
        riskAlertCriteria.tradingAccountId();
        riskAlertCriteria.traderId();
        riskAlertCriteria.distinct();
    }

    private static Condition<RiskAlertCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getAlertType()) &&
                condition.apply(criteria.getDescription()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getTradingAccountId()) &&
                condition.apply(criteria.getTraderId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<RiskAlertCriteria> copyFiltersAre(RiskAlertCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getAlertType(), copy.getAlertType()) &&
                condition.apply(criteria.getDescription(), copy.getDescription()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getTradingAccountId(), copy.getTradingAccountId()) &&
                condition.apply(criteria.getTraderId(), copy.getTraderId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
