package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class PositionCriteriaTest {

    @Test
    void newPositionCriteriaHasAllFiltersNullTest() {
        var positionCriteria = new PositionCriteria();
        assertThat(positionCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void positionCriteriaFluentMethodsCreatesFiltersTest() {
        var positionCriteria = new PositionCriteria();

        setAllFilters(positionCriteria);

        assertThat(positionCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void positionCriteriaCopyCreatesNullFilterTest() {
        var positionCriteria = new PositionCriteria();
        var copy = positionCriteria.copy();

        assertThat(positionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(positionCriteria)
        );
    }

    @Test
    void positionCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var positionCriteria = new PositionCriteria();
        setAllFilters(positionCriteria);

        var copy = positionCriteria.copy();

        assertThat(positionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(positionCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var positionCriteria = new PositionCriteria();

        assertThat(positionCriteria).hasToString("PositionCriteria{}");
    }

    private static void setAllFilters(PositionCriteria positionCriteria) {
        positionCriteria.id();
        positionCriteria.qty();
        positionCriteria.avgCost();
        positionCriteria.lastPx();
        positionCriteria.unrealizedPnl();
        positionCriteria.realizedPnl();
        positionCriteria.tradingAccountId();
        positionCriteria.instrumentId();
        positionCriteria.distinct();
    }

    private static Condition<PositionCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getQty()) &&
                condition.apply(criteria.getAvgCost()) &&
                condition.apply(criteria.getLastPx()) &&
                condition.apply(criteria.getUnrealizedPnl()) &&
                condition.apply(criteria.getRealizedPnl()) &&
                condition.apply(criteria.getTradingAccountId()) &&
                condition.apply(criteria.getInstrumentId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<PositionCriteria> copyFiltersAre(PositionCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getQty(), copy.getQty()) &&
                condition.apply(criteria.getAvgCost(), copy.getAvgCost()) &&
                condition.apply(criteria.getLastPx(), copy.getLastPx()) &&
                condition.apply(criteria.getUnrealizedPnl(), copy.getUnrealizedPnl()) &&
                condition.apply(criteria.getRealizedPnl(), copy.getRealizedPnl()) &&
                condition.apply(criteria.getTradingAccountId(), copy.getTradingAccountId()) &&
                condition.apply(criteria.getInstrumentId(), copy.getInstrumentId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
