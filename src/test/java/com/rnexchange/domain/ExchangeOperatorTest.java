package com.rnexchange.domain;

import static com.rnexchange.domain.ExchangeOperatorTestSamples.*;
import static com.rnexchange.domain.ExchangeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ExchangeOperatorTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ExchangeOperator.class);
        ExchangeOperator exchangeOperator1 = getExchangeOperatorSample1();
        ExchangeOperator exchangeOperator2 = new ExchangeOperator();
        assertThat(exchangeOperator1).isNotEqualTo(exchangeOperator2);

        exchangeOperator2.setId(exchangeOperator1.getId());
        assertThat(exchangeOperator1).isEqualTo(exchangeOperator2);

        exchangeOperator2 = getExchangeOperatorSample2();
        assertThat(exchangeOperator1).isNotEqualTo(exchangeOperator2);
    }

    @Test
    void exchangeTest() {
        ExchangeOperator exchangeOperator = getExchangeOperatorRandomSampleGenerator();
        Exchange exchangeBack = getExchangeRandomSampleGenerator();

        exchangeOperator.setExchange(exchangeBack);
        assertThat(exchangeOperator.getExchange()).isEqualTo(exchangeBack);

        exchangeOperator.exchange(null);
        assertThat(exchangeOperator.getExchange()).isNull();
    }
}
