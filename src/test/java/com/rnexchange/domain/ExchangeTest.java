package com.rnexchange.domain;

import static com.rnexchange.domain.ExchangeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ExchangeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Exchange.class);
        Exchange exchange1 = getExchangeSample1();
        Exchange exchange2 = new Exchange();
        assertThat(exchange1).isNotEqualTo(exchange2);

        exchange2.setId(exchange1.getId());
        assertThat(exchange1).isEqualTo(exchange2);

        exchange2 = getExchangeSample2();
        assertThat(exchange1).isNotEqualTo(exchange2);
    }
}
