package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ContractCriteriaTest {

    @Test
    void newContractCriteriaHasAllFiltersNullTest() {
        var contractCriteria = new ContractCriteria();
        assertThat(contractCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void contractCriteriaFluentMethodsCreatesFiltersTest() {
        var contractCriteria = new ContractCriteria();

        setAllFilters(contractCriteria);

        assertThat(contractCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void contractCriteriaCopyCreatesNullFilterTest() {
        var contractCriteria = new ContractCriteria();
        var copy = contractCriteria.copy();

        assertThat(contractCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(contractCriteria)
        );
    }

    @Test
    void contractCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var contractCriteria = new ContractCriteria();
        setAllFilters(contractCriteria);

        var copy = contractCriteria.copy();

        assertThat(contractCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(contractCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var contractCriteria = new ContractCriteria();

        assertThat(contractCriteria).hasToString("ContractCriteria{}");
    }

    private static void setAllFilters(ContractCriteria contractCriteria) {
        contractCriteria.id();
        contractCriteria.instrumentSymbol();
        contractCriteria.contractType();
        contractCriteria.expiry();
        contractCriteria.strike();
        contractCriteria.optionType();
        contractCriteria.segment();
        contractCriteria.instrumentId();
        contractCriteria.distinct();
    }

    private static Condition<ContractCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getInstrumentSymbol()) &&
                condition.apply(criteria.getContractType()) &&
                condition.apply(criteria.getExpiry()) &&
                condition.apply(criteria.getStrike()) &&
                condition.apply(criteria.getOptionType()) &&
                condition.apply(criteria.getSegment()) &&
                condition.apply(criteria.getInstrumentId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ContractCriteria> copyFiltersAre(ContractCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getInstrumentSymbol(), copy.getInstrumentSymbol()) &&
                condition.apply(criteria.getContractType(), copy.getContractType()) &&
                condition.apply(criteria.getExpiry(), copy.getExpiry()) &&
                condition.apply(criteria.getStrike(), copy.getStrike()) &&
                condition.apply(criteria.getOptionType(), copy.getOptionType()) &&
                condition.apply(criteria.getSegment(), copy.getSegment()) &&
                condition.apply(criteria.getInstrumentId(), copy.getInstrumentId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
