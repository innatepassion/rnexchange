package com.rnexchange.domain;

import static com.rnexchange.domain.ExchangeIntegrationTestSamples.*;
import static com.rnexchange.domain.ExchangeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ExchangeIntegrationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ExchangeIntegration.class);
        ExchangeIntegration exchangeIntegration1 = getExchangeIntegrationSample1();
        ExchangeIntegration exchangeIntegration2 = new ExchangeIntegration();
        assertThat(exchangeIntegration1).isNotEqualTo(exchangeIntegration2);

        exchangeIntegration2.setId(exchangeIntegration1.getId());
        assertThat(exchangeIntegration1).isEqualTo(exchangeIntegration2);

        exchangeIntegration2 = getExchangeIntegrationSample2();
        assertThat(exchangeIntegration1).isNotEqualTo(exchangeIntegration2);
    }

    @Test
    void exchangeTest() {
        ExchangeIntegration exchangeIntegration = getExchangeIntegrationRandomSampleGenerator();
        Exchange exchangeBack = getExchangeRandomSampleGenerator();

        exchangeIntegration.setExchange(exchangeBack);
        assertThat(exchangeIntegration.getExchange()).isEqualTo(exchangeBack);

        exchangeIntegration.exchange(null);
        assertThat(exchangeIntegration.getExchange()).isNull();
    }
}
