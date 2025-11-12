package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ExecutionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ExecutionDTO.class);
        ExecutionDTO executionDTO1 = new ExecutionDTO();
        executionDTO1.setId(1L);
        ExecutionDTO executionDTO2 = new ExecutionDTO();
        assertThat(executionDTO1).isNotEqualTo(executionDTO2);
        executionDTO2.setId(executionDTO1.getId());
        assertThat(executionDTO1).isEqualTo(executionDTO2);
        executionDTO2.setId(2L);
        assertThat(executionDTO1).isNotEqualTo(executionDTO2);
        executionDTO1.setId(null);
        assertThat(executionDTO1).isNotEqualTo(executionDTO2);
    }
}
