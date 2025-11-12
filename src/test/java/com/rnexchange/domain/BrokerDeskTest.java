package com.rnexchange.domain;

import static com.rnexchange.domain.BrokerDeskTestSamples.*;
import static com.rnexchange.domain.BrokerTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BrokerDeskTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BrokerDesk.class);
        BrokerDesk brokerDesk1 = getBrokerDeskSample1();
        BrokerDesk brokerDesk2 = new BrokerDesk();
        assertThat(brokerDesk1).isNotEqualTo(brokerDesk2);

        brokerDesk2.setId(brokerDesk1.getId());
        assertThat(brokerDesk1).isEqualTo(brokerDesk2);

        brokerDesk2 = getBrokerDeskSample2();
        assertThat(brokerDesk1).isNotEqualTo(brokerDesk2);
    }

    @Test
    void brokerTest() {
        BrokerDesk brokerDesk = getBrokerDeskRandomSampleGenerator();
        Broker brokerBack = getBrokerRandomSampleGenerator();

        brokerDesk.setBroker(brokerBack);
        assertThat(brokerDesk.getBroker()).isEqualTo(brokerBack);

        brokerDesk.broker(null);
        assertThat(brokerDesk.getBroker()).isNull();
    }
}
