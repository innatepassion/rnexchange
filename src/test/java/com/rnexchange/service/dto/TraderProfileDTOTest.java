package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TraderProfileDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TraderProfileDTO.class);
        TraderProfileDTO traderProfileDTO1 = new TraderProfileDTO();
        traderProfileDTO1.setId(1L);
        TraderProfileDTO traderProfileDTO2 = new TraderProfileDTO();
        assertThat(traderProfileDTO1).isNotEqualTo(traderProfileDTO2);
        traderProfileDTO2.setId(traderProfileDTO1.getId());
        assertThat(traderProfileDTO1).isEqualTo(traderProfileDTO2);
        traderProfileDTO2.setId(2L);
        assertThat(traderProfileDTO1).isNotEqualTo(traderProfileDTO2);
        traderProfileDTO1.setId(null);
        assertThat(traderProfileDTO1).isNotEqualTo(traderProfileDTO2);
    }
}
