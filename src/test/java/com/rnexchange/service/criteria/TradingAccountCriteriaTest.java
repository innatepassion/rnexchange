package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class TradingAccountCriteriaTest {

    @Test
    void newTradingAccountCriteriaHasAllFiltersNullTest() {
        var tradingAccountCriteria = new TradingAccountCriteria();
        assertThat(tradingAccountCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void tradingAccountCriteriaFluentMethodsCreatesFiltersTest() {
        var tradingAccountCriteria = new TradingAccountCriteria();

        setAllFilters(tradingAccountCriteria);

        assertThat(tradingAccountCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void tradingAccountCriteriaCopyCreatesNullFilterTest() {
        var tradingAccountCriteria = new TradingAccountCriteria();
        var copy = tradingAccountCriteria.copy();

        assertThat(tradingAccountCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(tradingAccountCriteria)
        );
    }

    @Test
    void tradingAccountCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var tradingAccountCriteria = new TradingAccountCriteria();
        setAllFilters(tradingAccountCriteria);

        var copy = tradingAccountCriteria.copy();

        assertThat(tradingAccountCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(tradingAccountCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var tradingAccountCriteria = new TradingAccountCriteria();

        assertThat(tradingAccountCriteria).hasToString("TradingAccountCriteria{}");
    }

    private static void setAllFilters(TradingAccountCriteria tradingAccountCriteria) {
        tradingAccountCriteria.id();
        tradingAccountCriteria.type();
        tradingAccountCriteria.baseCcy();
        tradingAccountCriteria.balance();
        tradingAccountCriteria.status();
        tradingAccountCriteria.brokerId();
        tradingAccountCriteria.traderId();
        tradingAccountCriteria.distinct();
    }

    private static Condition<TradingAccountCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getBaseCcy()) &&
                condition.apply(criteria.getBalance()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getBrokerId()) &&
                condition.apply(criteria.getTraderId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<TradingAccountCriteria> copyFiltersAre(
        TradingAccountCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getBaseCcy(), copy.getBaseCcy()) &&
                condition.apply(criteria.getBalance(), copy.getBalance()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getBrokerId(), copy.getBrokerId()) &&
                condition.apply(criteria.getTraderId(), copy.getTraderId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
