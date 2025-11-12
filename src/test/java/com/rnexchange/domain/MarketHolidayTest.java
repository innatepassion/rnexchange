package com.rnexchange.domain;

import static com.rnexchange.domain.ExchangeTestSamples.*;
import static com.rnexchange.domain.MarketHolidayTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MarketHolidayTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MarketHoliday.class);
        MarketHoliday marketHoliday1 = getMarketHolidaySample1();
        MarketHoliday marketHoliday2 = new MarketHoliday();
        assertThat(marketHoliday1).isNotEqualTo(marketHoliday2);

        marketHoliday2.setId(marketHoliday1.getId());
        assertThat(marketHoliday1).isEqualTo(marketHoliday2);

        marketHoliday2 = getMarketHolidaySample2();
        assertThat(marketHoliday1).isNotEqualTo(marketHoliday2);
    }

    @Test
    void exchangeTest() {
        MarketHoliday marketHoliday = getMarketHolidayRandomSampleGenerator();
        Exchange exchangeBack = getExchangeRandomSampleGenerator();

        marketHoliday.setExchange(exchangeBack);
        assertThat(marketHoliday.getExchange()).isEqualTo(exchangeBack);

        marketHoliday.exchange(null);
        assertThat(marketHoliday.getExchange()).isNull();
    }
}
