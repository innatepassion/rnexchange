package com.rnexchange.domain;

import static com.rnexchange.domain.DailySettlementPriceTestSamples.*;
import static com.rnexchange.domain.InstrumentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DailySettlementPriceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DailySettlementPrice.class);
        DailySettlementPrice dailySettlementPrice1 = getDailySettlementPriceSample1();
        DailySettlementPrice dailySettlementPrice2 = new DailySettlementPrice();
        assertThat(dailySettlementPrice1).isNotEqualTo(dailySettlementPrice2);

        dailySettlementPrice2.setId(dailySettlementPrice1.getId());
        assertThat(dailySettlementPrice1).isEqualTo(dailySettlementPrice2);

        dailySettlementPrice2 = getDailySettlementPriceSample2();
        assertThat(dailySettlementPrice1).isNotEqualTo(dailySettlementPrice2);
    }

    @Test
    void instrumentTest() {
        DailySettlementPrice dailySettlementPrice = getDailySettlementPriceRandomSampleGenerator();
        Instrument instrumentBack = getInstrumentRandomSampleGenerator();

        dailySettlementPrice.setInstrument(instrumentBack);
        assertThat(dailySettlementPrice.getInstrument()).isEqualTo(instrumentBack);

        dailySettlementPrice.instrument(null);
        assertThat(dailySettlementPrice.getInstrument()).isNull();
    }
}
