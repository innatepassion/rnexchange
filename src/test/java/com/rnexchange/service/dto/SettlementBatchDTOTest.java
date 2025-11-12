package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SettlementBatchDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SettlementBatchDTO.class);
        SettlementBatchDTO settlementBatchDTO1 = new SettlementBatchDTO();
        settlementBatchDTO1.setId(1L);
        SettlementBatchDTO settlementBatchDTO2 = new SettlementBatchDTO();
        assertThat(settlementBatchDTO1).isNotEqualTo(settlementBatchDTO2);
        settlementBatchDTO2.setId(settlementBatchDTO1.getId());
        assertThat(settlementBatchDTO1).isEqualTo(settlementBatchDTO2);
        settlementBatchDTO2.setId(2L);
        assertThat(settlementBatchDTO1).isNotEqualTo(settlementBatchDTO2);
        settlementBatchDTO1.setId(null);
        assertThat(settlementBatchDTO1).isNotEqualTo(settlementBatchDTO2);
    }
}
