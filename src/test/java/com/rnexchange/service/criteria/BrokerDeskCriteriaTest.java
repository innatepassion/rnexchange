package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class BrokerDeskCriteriaTest {

    @Test
    void newBrokerDeskCriteriaHasAllFiltersNullTest() {
        var brokerDeskCriteria = new BrokerDeskCriteria();
        assertThat(brokerDeskCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void brokerDeskCriteriaFluentMethodsCreatesFiltersTest() {
        var brokerDeskCriteria = new BrokerDeskCriteria();

        setAllFilters(brokerDeskCriteria);

        assertThat(brokerDeskCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void brokerDeskCriteriaCopyCreatesNullFilterTest() {
        var brokerDeskCriteria = new BrokerDeskCriteria();
        var copy = brokerDeskCriteria.copy();

        assertThat(brokerDeskCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(brokerDeskCriteria)
        );
    }

    @Test
    void brokerDeskCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var brokerDeskCriteria = new BrokerDeskCriteria();
        setAllFilters(brokerDeskCriteria);

        var copy = brokerDeskCriteria.copy();

        assertThat(brokerDeskCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(brokerDeskCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var brokerDeskCriteria = new BrokerDeskCriteria();

        assertThat(brokerDeskCriteria).hasToString("BrokerDeskCriteria{}");
    }

    private static void setAllFilters(BrokerDeskCriteria brokerDeskCriteria) {
        brokerDeskCriteria.id();
        brokerDeskCriteria.name();
        brokerDeskCriteria.userId();
        brokerDeskCriteria.brokerId();
        brokerDeskCriteria.distinct();
    }

    private static Condition<BrokerDeskCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getBrokerId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<BrokerDeskCriteria> copyFiltersAre(BrokerDeskCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getBrokerId(), copy.getBrokerId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
