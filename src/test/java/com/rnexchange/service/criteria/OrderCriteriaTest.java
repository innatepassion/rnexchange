package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class OrderCriteriaTest {

    @Test
    void newOrderCriteriaHasAllFiltersNullTest() {
        var orderCriteria = new OrderCriteria();
        assertThat(orderCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void orderCriteriaFluentMethodsCreatesFiltersTest() {
        var orderCriteria = new OrderCriteria();

        setAllFilters(orderCriteria);

        assertThat(orderCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void orderCriteriaCopyCreatesNullFilterTest() {
        var orderCriteria = new OrderCriteria();
        var copy = orderCriteria.copy();

        assertThat(orderCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(orderCriteria)
        );
    }

    @Test
    void orderCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var orderCriteria = new OrderCriteria();
        setAllFilters(orderCriteria);

        var copy = orderCriteria.copy();

        assertThat(orderCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(orderCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var orderCriteria = new OrderCriteria();

        assertThat(orderCriteria).hasToString("OrderCriteria{}");
    }

    private static void setAllFilters(OrderCriteria orderCriteria) {
        orderCriteria.id();
        orderCriteria.side();
        orderCriteria.type();
        orderCriteria.qty();
        orderCriteria.limitPx();
        orderCriteria.stopPx();
        orderCriteria.tif();
        orderCriteria.status();
        orderCriteria.venue();
        orderCriteria.createdAt();
        orderCriteria.updatedAt();
        orderCriteria.tradingAccountId();
        orderCriteria.instrumentId();
        orderCriteria.distinct();
    }

    private static Condition<OrderCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getSide()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getQty()) &&
                condition.apply(criteria.getLimitPx()) &&
                condition.apply(criteria.getStopPx()) &&
                condition.apply(criteria.getTif()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getVenue()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt()) &&
                condition.apply(criteria.getTradingAccountId()) &&
                condition.apply(criteria.getInstrumentId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<OrderCriteria> copyFiltersAre(OrderCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getSide(), copy.getSide()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getQty(), copy.getQty()) &&
                condition.apply(criteria.getLimitPx(), copy.getLimitPx()) &&
                condition.apply(criteria.getStopPx(), copy.getStopPx()) &&
                condition.apply(criteria.getTif(), copy.getTif()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getVenue(), copy.getVenue()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt(), copy.getUpdatedAt()) &&
                condition.apply(criteria.getTradingAccountId(), copy.getTradingAccountId()) &&
                condition.apply(criteria.getInstrumentId(), copy.getInstrumentId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
