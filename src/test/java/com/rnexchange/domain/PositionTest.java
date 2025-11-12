package com.rnexchange.domain;

import static com.rnexchange.domain.InstrumentTestSamples.*;
import static com.rnexchange.domain.PositionTestSamples.*;
import static com.rnexchange.domain.TradingAccountTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Position.class);
        Position position1 = getPositionSample1();
        Position position2 = new Position();
        assertThat(position1).isNotEqualTo(position2);

        position2.setId(position1.getId());
        assertThat(position1).isEqualTo(position2);

        position2 = getPositionSample2();
        assertThat(position1).isNotEqualTo(position2);
    }

    @Test
    void tradingAccountTest() {
        Position position = getPositionRandomSampleGenerator();
        TradingAccount tradingAccountBack = getTradingAccountRandomSampleGenerator();

        position.setTradingAccount(tradingAccountBack);
        assertThat(position.getTradingAccount()).isEqualTo(tradingAccountBack);

        position.tradingAccount(null);
        assertThat(position.getTradingAccount()).isNull();
    }

    @Test
    void instrumentTest() {
        Position position = getPositionRandomSampleGenerator();
        Instrument instrumentBack = getInstrumentRandomSampleGenerator();

        position.setInstrument(instrumentBack);
        assertThat(position.getInstrument()).isEqualTo(instrumentBack);

        position.instrument(null);
        assertThat(position.getInstrument()).isNull();
    }
}
