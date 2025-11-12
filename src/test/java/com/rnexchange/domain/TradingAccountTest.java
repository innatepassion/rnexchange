package com.rnexchange.domain;

import static com.rnexchange.domain.BrokerTestSamples.*;
import static com.rnexchange.domain.TraderProfileTestSamples.*;
import static com.rnexchange.domain.TradingAccountTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TradingAccountTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TradingAccount.class);
        TradingAccount tradingAccount1 = getTradingAccountSample1();
        TradingAccount tradingAccount2 = new TradingAccount();
        assertThat(tradingAccount1).isNotEqualTo(tradingAccount2);

        tradingAccount2.setId(tradingAccount1.getId());
        assertThat(tradingAccount1).isEqualTo(tradingAccount2);

        tradingAccount2 = getTradingAccountSample2();
        assertThat(tradingAccount1).isNotEqualTo(tradingAccount2);
    }

    @Test
    void brokerTest() {
        TradingAccount tradingAccount = getTradingAccountRandomSampleGenerator();
        Broker brokerBack = getBrokerRandomSampleGenerator();

        tradingAccount.setBroker(brokerBack);
        assertThat(tradingAccount.getBroker()).isEqualTo(brokerBack);

        tradingAccount.broker(null);
        assertThat(tradingAccount.getBroker()).isNull();
    }

    @Test
    void traderTest() {
        TradingAccount tradingAccount = getTradingAccountRandomSampleGenerator();
        TraderProfile traderProfileBack = getTraderProfileRandomSampleGenerator();

        tradingAccount.setTrader(traderProfileBack);
        assertThat(tradingAccount.getTrader()).isEqualTo(traderProfileBack);

        tradingAccount.trader(null);
        assertThat(tradingAccount.getTrader()).isNull();
    }
}
