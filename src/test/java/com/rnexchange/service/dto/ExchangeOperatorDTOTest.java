package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ExchangeOperatorDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ExchangeOperatorDTO.class);
        ExchangeOperatorDTO exchangeOperatorDTO1 = new ExchangeOperatorDTO();
        exchangeOperatorDTO1.setId(1L);
        ExchangeOperatorDTO exchangeOperatorDTO2 = new ExchangeOperatorDTO();
        assertThat(exchangeOperatorDTO1).isNotEqualTo(exchangeOperatorDTO2);
        exchangeOperatorDTO2.setId(exchangeOperatorDTO1.getId());
        assertThat(exchangeOperatorDTO1).isEqualTo(exchangeOperatorDTO2);
        exchangeOperatorDTO2.setId(2L);
        assertThat(exchangeOperatorDTO1).isNotEqualTo(exchangeOperatorDTO2);
        exchangeOperatorDTO1.setId(null);
        assertThat(exchangeOperatorDTO1).isNotEqualTo(exchangeOperatorDTO2);
    }
}
