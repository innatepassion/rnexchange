package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CorporateActionCriteriaTest {

    @Test
    void newCorporateActionCriteriaHasAllFiltersNullTest() {
        var corporateActionCriteria = new CorporateActionCriteria();
        assertThat(corporateActionCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void corporateActionCriteriaFluentMethodsCreatesFiltersTest() {
        var corporateActionCriteria = new CorporateActionCriteria();

        setAllFilters(corporateActionCriteria);

        assertThat(corporateActionCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void corporateActionCriteriaCopyCreatesNullFilterTest() {
        var corporateActionCriteria = new CorporateActionCriteria();
        var copy = corporateActionCriteria.copy();

        assertThat(corporateActionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(corporateActionCriteria)
        );
    }

    @Test
    void corporateActionCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var corporateActionCriteria = new CorporateActionCriteria();
        setAllFilters(corporateActionCriteria);

        var copy = corporateActionCriteria.copy();

        assertThat(corporateActionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(corporateActionCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var corporateActionCriteria = new CorporateActionCriteria();

        assertThat(corporateActionCriteria).hasToString("CorporateActionCriteria{}");
    }

    private static void setAllFilters(CorporateActionCriteria corporateActionCriteria) {
        corporateActionCriteria.id();
        corporateActionCriteria.type();
        corporateActionCriteria.instrumentSymbol();
        corporateActionCriteria.exDate();
        corporateActionCriteria.payDate();
        corporateActionCriteria.ratio();
        corporateActionCriteria.cashAmount();
        corporateActionCriteria.instrumentId();
        corporateActionCriteria.distinct();
    }

    private static Condition<CorporateActionCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getInstrumentSymbol()) &&
                condition.apply(criteria.getExDate()) &&
                condition.apply(criteria.getPayDate()) &&
                condition.apply(criteria.getRatio()) &&
                condition.apply(criteria.getCashAmount()) &&
                condition.apply(criteria.getInstrumentId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CorporateActionCriteria> copyFiltersAre(
        CorporateActionCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getInstrumentSymbol(), copy.getInstrumentSymbol()) &&
                condition.apply(criteria.getExDate(), copy.getExDate()) &&
                condition.apply(criteria.getPayDate(), copy.getPayDate()) &&
                condition.apply(criteria.getRatio(), copy.getRatio()) &&
                condition.apply(criteria.getCashAmount(), copy.getCashAmount()) &&
                condition.apply(criteria.getInstrumentId(), copy.getInstrumentId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
