package com.rnexchange.domain;

import static com.rnexchange.domain.ExchangeTestSamples.*;
import static com.rnexchange.domain.MarginRuleTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MarginRuleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MarginRule.class);
        MarginRule marginRule1 = getMarginRuleSample1();
        MarginRule marginRule2 = new MarginRule();
        assertThat(marginRule1).isNotEqualTo(marginRule2);

        marginRule2.setId(marginRule1.getId());
        assertThat(marginRule1).isEqualTo(marginRule2);

        marginRule2 = getMarginRuleSample2();
        assertThat(marginRule1).isNotEqualTo(marginRule2);
    }

    @Test
    void exchangeTest() {
        MarginRule marginRule = getMarginRuleRandomSampleGenerator();
        Exchange exchangeBack = getExchangeRandomSampleGenerator();

        marginRule.setExchange(exchangeBack);
        assertThat(marginRule.getExchange()).isEqualTo(exchangeBack);

        marginRule.exchange(null);
        assertThat(marginRule.getExchange()).isNull();
    }
}
