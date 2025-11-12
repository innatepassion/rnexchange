package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MarginRuleDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MarginRuleDTO.class);
        MarginRuleDTO marginRuleDTO1 = new MarginRuleDTO();
        marginRuleDTO1.setId(1L);
        MarginRuleDTO marginRuleDTO2 = new MarginRuleDTO();
        assertThat(marginRuleDTO1).isNotEqualTo(marginRuleDTO2);
        marginRuleDTO2.setId(marginRuleDTO1.getId());
        assertThat(marginRuleDTO1).isEqualTo(marginRuleDTO2);
        marginRuleDTO2.setId(2L);
        assertThat(marginRuleDTO1).isNotEqualTo(marginRuleDTO2);
        marginRuleDTO1.setId(null);
        assertThat(marginRuleDTO1).isNotEqualTo(marginRuleDTO2);
    }
}
