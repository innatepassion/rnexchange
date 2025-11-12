package com.rnexchange.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ExchangeIntegrationCriteriaTest {

    @Test
    void newExchangeIntegrationCriteriaHasAllFiltersNullTest() {
        var exchangeIntegrationCriteria = new ExchangeIntegrationCriteria();
        assertThat(exchangeIntegrationCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void exchangeIntegrationCriteriaFluentMethodsCreatesFiltersTest() {
        var exchangeIntegrationCriteria = new ExchangeIntegrationCriteria();

        setAllFilters(exchangeIntegrationCriteria);

        assertThat(exchangeIntegrationCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void exchangeIntegrationCriteriaCopyCreatesNullFilterTest() {
        var exchangeIntegrationCriteria = new ExchangeIntegrationCriteria();
        var copy = exchangeIntegrationCriteria.copy();

        assertThat(exchangeIntegrationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(exchangeIntegrationCriteria)
        );
    }

    @Test
    void exchangeIntegrationCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var exchangeIntegrationCriteria = new ExchangeIntegrationCriteria();
        setAllFilters(exchangeIntegrationCriteria);

        var copy = exchangeIntegrationCriteria.copy();

        assertThat(exchangeIntegrationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(exchangeIntegrationCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var exchangeIntegrationCriteria = new ExchangeIntegrationCriteria();

        assertThat(exchangeIntegrationCriteria).hasToString("ExchangeIntegrationCriteria{}");
    }

    private static void setAllFilters(ExchangeIntegrationCriteria exchangeIntegrationCriteria) {
        exchangeIntegrationCriteria.id();
        exchangeIntegrationCriteria.provider();
        exchangeIntegrationCriteria.apiKey();
        exchangeIntegrationCriteria.apiSecret();
        exchangeIntegrationCriteria.status();
        exchangeIntegrationCriteria.lastHeartbeat();
        exchangeIntegrationCriteria.exchangeId();
        exchangeIntegrationCriteria.distinct();
    }

    private static Condition<ExchangeIntegrationCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getProvider()) &&
                condition.apply(criteria.getApiKey()) &&
                condition.apply(criteria.getApiSecret()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getLastHeartbeat()) &&
                condition.apply(criteria.getExchangeId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ExchangeIntegrationCriteria> copyFiltersAre(
        ExchangeIntegrationCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getProvider(), copy.getProvider()) &&
                condition.apply(criteria.getApiKey(), copy.getApiKey()) &&
                condition.apply(criteria.getApiSecret(), copy.getApiSecret()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getLastHeartbeat(), copy.getLastHeartbeat()) &&
                condition.apply(criteria.getExchangeId(), copy.getExchangeId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
