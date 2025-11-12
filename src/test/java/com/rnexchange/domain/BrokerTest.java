package com.rnexchange.domain;

import static com.rnexchange.domain.BrokerTestSamples.*;
import static com.rnexchange.domain.ExchangeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BrokerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Broker.class);
        Broker broker1 = getBrokerSample1();
        Broker broker2 = new Broker();
        assertThat(broker1).isNotEqualTo(broker2);

        broker2.setId(broker1.getId());
        assertThat(broker1).isEqualTo(broker2);

        broker2 = getBrokerSample2();
        assertThat(broker1).isNotEqualTo(broker2);
    }

    @Test
    void exchangeTest() {
        Broker broker = getBrokerRandomSampleGenerator();
        Exchange exchangeBack = getExchangeRandomSampleGenerator();

        broker.setExchange(exchangeBack);
        assertThat(broker.getExchange()).isEqualTo(exchangeBack);

        broker.exchange(null);
        assertThat(broker.getExchange()).isNull();
    }
}
