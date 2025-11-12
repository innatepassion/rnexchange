package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class DailySettlementPriceCriteriaTest {

    @Test
    void newDailySettlementPriceCriteriaHasAllFiltersNullTest() {
        var dailySettlementPriceCriteria = new DailySettlementPriceCriteria();
        assertThat(dailySettlementPriceCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void dailySettlementPriceCriteriaFluentMethodsCreatesFiltersTest() {
        var dailySettlementPriceCriteria = new DailySettlementPriceCriteria();

        setAllFilters(dailySettlementPriceCriteria);

        assertThat(dailySettlementPriceCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void dailySettlementPriceCriteriaCopyCreatesNullFilterTest() {
        var dailySettlementPriceCriteria = new DailySettlementPriceCriteria();
        var copy = dailySettlementPriceCriteria.copy();

        assertThat(dailySettlementPriceCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(dailySettlementPriceCriteria)
        );
    }

    @Test
    void dailySettlementPriceCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var dailySettlementPriceCriteria = new DailySettlementPriceCriteria();
        setAllFilters(dailySettlementPriceCriteria);

        var copy = dailySettlementPriceCriteria.copy();

        assertThat(dailySettlementPriceCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(dailySettlementPriceCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var dailySettlementPriceCriteria = new DailySettlementPriceCriteria();

        assertThat(dailySettlementPriceCriteria).hasToString("DailySettlementPriceCriteria{}");
    }

    private static void setAllFilters(DailySettlementPriceCriteria dailySettlementPriceCriteria) {
        dailySettlementPriceCriteria.id();
        dailySettlementPriceCriteria.refDate();
        dailySettlementPriceCriteria.instrumentSymbol();
        dailySettlementPriceCriteria.settlePrice();
        dailySettlementPriceCriteria.instrumentId();
        dailySettlementPriceCriteria.distinct();
    }

    private static Condition<DailySettlementPriceCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getRefDate()) &&
                condition.apply(criteria.getInstrumentSymbol()) &&
                condition.apply(criteria.getSettlePrice()) &&
                condition.apply(criteria.getInstrumentId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<DailySettlementPriceCriteria> copyFiltersAre(
        DailySettlementPriceCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getRefDate(), copy.getRefDate()) &&
                condition.apply(criteria.getInstrumentSymbol(), copy.getInstrumentSymbol()) &&
                condition.apply(criteria.getSettlePrice(), copy.getSettlePrice()) &&
                condition.apply(criteria.getInstrumentId(), copy.getInstrumentId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
