package com.rnexchange.domain;

import static com.rnexchange.domain.ContractTestSamples.*;
import static com.rnexchange.domain.InstrumentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ContractTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Contract.class);
        Contract contract1 = getContractSample1();
        Contract contract2 = new Contract();
        assertThat(contract1).isNotEqualTo(contract2);

        contract2.setId(contract1.getId());
        assertThat(contract1).isEqualTo(contract2);

        contract2 = getContractSample2();
        assertThat(contract1).isNotEqualTo(contract2);
    }

    @Test
    void instrumentTest() {
        Contract contract = getContractRandomSampleGenerator();
        Instrument instrumentBack = getInstrumentRandomSampleGenerator();

        contract.setInstrument(instrumentBack);
        assertThat(contract.getInstrument()).isEqualTo(instrumentBack);

        contract.instrument(null);
        assertThat(contract.getInstrument()).isNull();
    }
}
