package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ExecutionCriteriaTest {

    @Test
    void newExecutionCriteriaHasAllFiltersNullTest() {
        var executionCriteria = new ExecutionCriteria();
        assertThat(executionCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void executionCriteriaFluentMethodsCreatesFiltersTest() {
        var executionCriteria = new ExecutionCriteria();

        setAllFilters(executionCriteria);

        assertThat(executionCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void executionCriteriaCopyCreatesNullFilterTest() {
        var executionCriteria = new ExecutionCriteria();
        var copy = executionCriteria.copy();

        assertThat(executionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(executionCriteria)
        );
    }

    @Test
    void executionCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var executionCriteria = new ExecutionCriteria();
        setAllFilters(executionCriteria);

        var copy = executionCriteria.copy();

        assertThat(executionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(executionCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var executionCriteria = new ExecutionCriteria();

        assertThat(executionCriteria).hasToString("ExecutionCriteria{}");
    }

    private static void setAllFilters(ExecutionCriteria executionCriteria) {
        executionCriteria.id();
        executionCriteria.execTs();
        executionCriteria.px();
        executionCriteria.qty();
        executionCriteria.liquidity();
        executionCriteria.fee();
        executionCriteria.orderId();
        executionCriteria.distinct();
    }

    private static Condition<ExecutionCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getExecTs()) &&
                condition.apply(criteria.getPx()) &&
                condition.apply(criteria.getQty()) &&
                condition.apply(criteria.getLiquidity()) &&
                condition.apply(criteria.getFee()) &&
                condition.apply(criteria.getOrderId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ExecutionCriteria> copyFiltersAre(ExecutionCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getExecTs(), copy.getExecTs()) &&
                condition.apply(criteria.getPx(), copy.getPx()) &&
                condition.apply(criteria.getQty(), copy.getQty()) &&
                condition.apply(criteria.getLiquidity(), copy.getLiquidity()) &&
                condition.apply(criteria.getFee(), copy.getFee()) &&
                condition.apply(criteria.getOrderId(), copy.getOrderId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
