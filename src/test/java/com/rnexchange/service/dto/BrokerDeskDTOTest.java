package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BrokerDeskDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BrokerDeskDTO.class);
        BrokerDeskDTO brokerDeskDTO1 = new BrokerDeskDTO();
        brokerDeskDTO1.setId(1L);
        BrokerDeskDTO brokerDeskDTO2 = new BrokerDeskDTO();
        assertThat(brokerDeskDTO1).isNotEqualTo(brokerDeskDTO2);
        brokerDeskDTO2.setId(brokerDeskDTO1.getId());
        assertThat(brokerDeskDTO1).isEqualTo(brokerDeskDTO2);
        brokerDeskDTO2.setId(2L);
        assertThat(brokerDeskDTO1).isNotEqualTo(brokerDeskDTO2);
        brokerDeskDTO1.setId(null);
        assertThat(brokerDeskDTO1).isNotEqualTo(brokerDeskDTO2);
    }
}
