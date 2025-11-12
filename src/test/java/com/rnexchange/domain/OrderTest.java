package com.rnexchange.domain;

import static com.rnexchange.domain.InstrumentTestSamples.*;
import static com.rnexchange.domain.OrderTestSamples.*;
import static com.rnexchange.domain.TradingAccountTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Order.class);
        Order order1 = getOrderSample1();
        Order order2 = new Order();
        assertThat(order1).isNotEqualTo(order2);

        order2.setId(order1.getId());
        assertThat(order1).isEqualTo(order2);

        order2 = getOrderSample2();
        assertThat(order1).isNotEqualTo(order2);
    }

    @Test
    void tradingAccountTest() {
        Order order = getOrderRandomSampleGenerator();
        TradingAccount tradingAccountBack = getTradingAccountRandomSampleGenerator();

        order.setTradingAccount(tradingAccountBack);
        assertThat(order.getTradingAccount()).isEqualTo(tradingAccountBack);

        order.tradingAccount(null);
        assertThat(order.getTradingAccount()).isNull();
    }

    @Test
    void instrumentTest() {
        Order order = getOrderRandomSampleGenerator();
        Instrument instrumentBack = getInstrumentRandomSampleGenerator();

        order.setInstrument(instrumentBack);
        assertThat(order.getInstrument()).isEqualTo(instrumentBack);

        order.instrument(null);
        assertThat(order.getInstrument()).isNull();
    }
}
