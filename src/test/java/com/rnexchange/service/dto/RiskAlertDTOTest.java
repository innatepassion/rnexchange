package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RiskAlertDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RiskAlertDTO.class);
        RiskAlertDTO riskAlertDTO1 = new RiskAlertDTO();
        riskAlertDTO1.setId(1L);
        RiskAlertDTO riskAlertDTO2 = new RiskAlertDTO();
        assertThat(riskAlertDTO1).isNotEqualTo(riskAlertDTO2);
        riskAlertDTO2.setId(riskAlertDTO1.getId());
        assertThat(riskAlertDTO1).isEqualTo(riskAlertDTO2);
        riskAlertDTO2.setId(2L);
        assertThat(riskAlertDTO1).isNotEqualTo(riskAlertDTO2);
        riskAlertDTO1.setId(null);
        assertThat(riskAlertDTO1).isNotEqualTo(riskAlertDTO2);
    }
}
