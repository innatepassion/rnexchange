package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CorporateActionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CorporateActionDTO.class);
        CorporateActionDTO corporateActionDTO1 = new CorporateActionDTO();
        corporateActionDTO1.setId(1L);
        CorporateActionDTO corporateActionDTO2 = new CorporateActionDTO();
        assertThat(corporateActionDTO1).isNotEqualTo(corporateActionDTO2);
        corporateActionDTO2.setId(corporateActionDTO1.getId());
        assertThat(corporateActionDTO1).isEqualTo(corporateActionDTO2);
        corporateActionDTO2.setId(2L);
        assertThat(corporateActionDTO1).isNotEqualTo(corporateActionDTO2);
        corporateActionDTO1.setId(null);
        assertThat(corporateActionDTO1).isNotEqualTo(corporateActionDTO2);
    }
}
