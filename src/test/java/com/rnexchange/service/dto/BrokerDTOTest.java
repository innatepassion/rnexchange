package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BrokerDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BrokerDTO.class);
        BrokerDTO brokerDTO1 = new BrokerDTO();
        brokerDTO1.setId(1L);
        BrokerDTO brokerDTO2 = new BrokerDTO();
        assertThat(brokerDTO1).isNotEqualTo(brokerDTO2);
        brokerDTO2.setId(brokerDTO1.getId());
        assertThat(brokerDTO1).isEqualTo(brokerDTO2);
        brokerDTO2.setId(2L);
        assertThat(brokerDTO1).isNotEqualTo(brokerDTO2);
        brokerDTO1.setId(null);
        assertThat(brokerDTO1).isNotEqualTo(brokerDTO2);
    }
}
