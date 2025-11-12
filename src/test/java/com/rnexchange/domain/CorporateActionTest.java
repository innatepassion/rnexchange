package com.rnexchange.domain;

import static com.rnexchange.domain.CorporateActionTestSamples.*;
import static com.rnexchange.domain.InstrumentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CorporateActionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CorporateAction.class);
        CorporateAction corporateAction1 = getCorporateActionSample1();
        CorporateAction corporateAction2 = new CorporateAction();
        assertThat(corporateAction1).isNotEqualTo(corporateAction2);

        corporateAction2.setId(corporateAction1.getId());
        assertThat(corporateAction1).isEqualTo(corporateAction2);

        corporateAction2 = getCorporateActionSample2();
        assertThat(corporateAction1).isNotEqualTo(corporateAction2);
    }

    @Test
    void instrumentTest() {
        CorporateAction corporateAction = getCorporateActionRandomSampleGenerator();
        Instrument instrumentBack = getInstrumentRandomSampleGenerator();

        corporateAction.setInstrument(instrumentBack);
        assertThat(corporateAction.getInstrument()).isEqualTo(instrumentBack);

        corporateAction.instrument(null);
        assertThat(corporateAction.getInstrument()).isNull();
    }
}
