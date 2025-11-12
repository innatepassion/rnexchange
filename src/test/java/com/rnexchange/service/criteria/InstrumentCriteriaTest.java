package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class InstrumentCriteriaTest {

    @Test
    void newInstrumentCriteriaHasAllFiltersNullTest() {
        var instrumentCriteria = new InstrumentCriteria();
        assertThat(instrumentCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void instrumentCriteriaFluentMethodsCreatesFiltersTest() {
        var instrumentCriteria = new InstrumentCriteria();

        setAllFilters(instrumentCriteria);

        assertThat(instrumentCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void instrumentCriteriaCopyCreatesNullFilterTest() {
        var instrumentCriteria = new InstrumentCriteria();
        var copy = instrumentCriteria.copy();

        assertThat(instrumentCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(instrumentCriteria)
        );
    }

    @Test
    void instrumentCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var instrumentCriteria = new InstrumentCriteria();
        setAllFilters(instrumentCriteria);

        var copy = instrumentCriteria.copy();

        assertThat(instrumentCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(instrumentCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var instrumentCriteria = new InstrumentCriteria();

        assertThat(instrumentCriteria).hasToString("InstrumentCriteria{}");
    }

    private static void setAllFilters(InstrumentCriteria instrumentCriteria) {
        instrumentCriteria.id();
        instrumentCriteria.symbol();
        instrumentCriteria.name();
        instrumentCriteria.assetClass();
        instrumentCriteria.exchangeCode();
        instrumentCriteria.tickSize();
        instrumentCriteria.lotSize();
        instrumentCriteria.currency();
        instrumentCriteria.status();
        instrumentCriteria.exchangeId();
        instrumentCriteria.distinct();
    }

    private static Condition<InstrumentCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getSymbol()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getAssetClass()) &&
                condition.apply(criteria.getExchangeCode()) &&
                condition.apply(criteria.getTickSize()) &&
                condition.apply(criteria.getLotSize()) &&
                condition.apply(criteria.getCurrency()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getExchangeId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<InstrumentCriteria> copyFiltersAre(InstrumentCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getSymbol(), copy.getSymbol()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getAssetClass(), copy.getAssetClass()) &&
                condition.apply(criteria.getExchangeCode(), copy.getExchangeCode()) &&
                condition.apply(criteria.getTickSize(), copy.getTickSize()) &&
                condition.apply(criteria.getLotSize(), copy.getLotSize()) &&
                condition.apply(criteria.getCurrency(), copy.getCurrency()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getExchangeId(), copy.getExchangeId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
