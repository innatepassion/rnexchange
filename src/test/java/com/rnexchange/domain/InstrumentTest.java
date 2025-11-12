package com.rnexchange.domain;

import static com.rnexchange.domain.ExchangeTestSamples.*;
import static com.rnexchange.domain.InstrumentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InstrumentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Instrument.class);
        Instrument instrument1 = getInstrumentSample1();
        Instrument instrument2 = new Instrument();
        assertThat(instrument1).isNotEqualTo(instrument2);

        instrument2.setId(instrument1.getId());
        assertThat(instrument1).isEqualTo(instrument2);

        instrument2 = getInstrumentSample2();
        assertThat(instrument1).isNotEqualTo(instrument2);
    }

    @Test
    void exchangeTest() {
        Instrument instrument = getInstrumentRandomSampleGenerator();
        Exchange exchangeBack = getExchangeRandomSampleGenerator();

        instrument.setExchange(exchangeBack);
        assertThat(instrument.getExchange()).isEqualTo(exchangeBack);

        instrument.exchange(null);
        assertThat(instrument.getExchange()).isNull();
    }
}
