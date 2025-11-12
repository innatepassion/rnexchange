package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class MarketHolidayCriteriaTest {

    @Test
    void newMarketHolidayCriteriaHasAllFiltersNullTest() {
        var marketHolidayCriteria = new MarketHolidayCriteria();
        assertThat(marketHolidayCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void marketHolidayCriteriaFluentMethodsCreatesFiltersTest() {
        var marketHolidayCriteria = new MarketHolidayCriteria();

        setAllFilters(marketHolidayCriteria);

        assertThat(marketHolidayCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void marketHolidayCriteriaCopyCreatesNullFilterTest() {
        var marketHolidayCriteria = new MarketHolidayCriteria();
        var copy = marketHolidayCriteria.copy();

        assertThat(marketHolidayCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(marketHolidayCriteria)
        );
    }

    @Test
    void marketHolidayCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var marketHolidayCriteria = new MarketHolidayCriteria();
        setAllFilters(marketHolidayCriteria);

        var copy = marketHolidayCriteria.copy();

        assertThat(marketHolidayCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(marketHolidayCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var marketHolidayCriteria = new MarketHolidayCriteria();

        assertThat(marketHolidayCriteria).hasToString("MarketHolidayCriteria{}");
    }

    private static void setAllFilters(MarketHolidayCriteria marketHolidayCriteria) {
        marketHolidayCriteria.id();
        marketHolidayCriteria.tradeDate();
        marketHolidayCriteria.reason();
        marketHolidayCriteria.isHoliday();
        marketHolidayCriteria.exchangeId();
        marketHolidayCriteria.distinct();
    }

    private static Condition<MarketHolidayCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTradeDate()) &&
                condition.apply(criteria.getReason()) &&
                condition.apply(criteria.getIsHoliday()) &&
                condition.apply(criteria.getExchangeId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<MarketHolidayCriteria> copyFiltersAre(
        MarketHolidayCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTradeDate(), copy.getTradeDate()) &&
                condition.apply(criteria.getReason(), copy.getReason()) &&
                condition.apply(criteria.getIsHoliday(), copy.getIsHoliday()) &&
                condition.apply(criteria.getExchangeId(), copy.getExchangeId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
