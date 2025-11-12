package com.rnexchange.domain;

import static com.rnexchange.domain.RiskAlertTestSamples.*;
import static com.rnexchange.domain.TraderProfileTestSamples.*;
import static com.rnexchange.domain.TradingAccountTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RiskAlertTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RiskAlert.class);
        RiskAlert riskAlert1 = getRiskAlertSample1();
        RiskAlert riskAlert2 = new RiskAlert();
        assertThat(riskAlert1).isNotEqualTo(riskAlert2);

        riskAlert2.setId(riskAlert1.getId());
        assertThat(riskAlert1).isEqualTo(riskAlert2);

        riskAlert2 = getRiskAlertSample2();
        assertThat(riskAlert1).isNotEqualTo(riskAlert2);
    }

    @Test
    void tradingAccountTest() {
        RiskAlert riskAlert = getRiskAlertRandomSampleGenerator();
        TradingAccount tradingAccountBack = getTradingAccountRandomSampleGenerator();

        riskAlert.setTradingAccount(tradingAccountBack);
        assertThat(riskAlert.getTradingAccount()).isEqualTo(tradingAccountBack);

        riskAlert.tradingAccount(null);
        assertThat(riskAlert.getTradingAccount()).isNull();
    }

    @Test
    void traderTest() {
        RiskAlert riskAlert = getRiskAlertRandomSampleGenerator();
        TraderProfile traderProfileBack = getTraderProfileRandomSampleGenerator();

        riskAlert.setTrader(traderProfileBack);
        assertThat(riskAlert.getTrader()).isEqualTo(traderProfileBack);

        riskAlert.trader(null);
        assertThat(riskAlert.getTrader()).isNull();
    }
}
