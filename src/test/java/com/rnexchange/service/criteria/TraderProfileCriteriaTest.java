package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class TraderProfileCriteriaTest {

    @Test
    void newTraderProfileCriteriaHasAllFiltersNullTest() {
        var traderProfileCriteria = new TraderProfileCriteria();
        assertThat(traderProfileCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void traderProfileCriteriaFluentMethodsCreatesFiltersTest() {
        var traderProfileCriteria = new TraderProfileCriteria();

        setAllFilters(traderProfileCriteria);

        assertThat(traderProfileCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void traderProfileCriteriaCopyCreatesNullFilterTest() {
        var traderProfileCriteria = new TraderProfileCriteria();
        var copy = traderProfileCriteria.copy();

        assertThat(traderProfileCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(traderProfileCriteria)
        );
    }

    @Test
    void traderProfileCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var traderProfileCriteria = new TraderProfileCriteria();
        setAllFilters(traderProfileCriteria);

        var copy = traderProfileCriteria.copy();

        assertThat(traderProfileCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(traderProfileCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var traderProfileCriteria = new TraderProfileCriteria();

        assertThat(traderProfileCriteria).hasToString("TraderProfileCriteria{}");
    }

    private static void setAllFilters(TraderProfileCriteria traderProfileCriteria) {
        traderProfileCriteria.id();
        traderProfileCriteria.displayName();
        traderProfileCriteria.email();
        traderProfileCriteria.mobile();
        traderProfileCriteria.kycStatus();
        traderProfileCriteria.status();
        traderProfileCriteria.userId();
        traderProfileCriteria.distinct();
    }

    private static Condition<TraderProfileCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getDisplayName()) &&
                condition.apply(criteria.getEmail()) &&
                condition.apply(criteria.getMobile()) &&
                condition.apply(criteria.getKycStatus()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<TraderProfileCriteria> copyFiltersAre(
        TraderProfileCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getDisplayName(), copy.getDisplayName()) &&
                condition.apply(criteria.getEmail(), copy.getEmail()) &&
                condition.apply(criteria.getMobile(), copy.getMobile()) &&
                condition.apply(criteria.getKycStatus(), copy.getKycStatus()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
