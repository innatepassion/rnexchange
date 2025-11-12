package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ExchangeIntegrationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ExchangeIntegrationDTO.class);
        ExchangeIntegrationDTO exchangeIntegrationDTO1 = new ExchangeIntegrationDTO();
        exchangeIntegrationDTO1.setId(1L);
        ExchangeIntegrationDTO exchangeIntegrationDTO2 = new ExchangeIntegrationDTO();
        assertThat(exchangeIntegrationDTO1).isNotEqualTo(exchangeIntegrationDTO2);
        exchangeIntegrationDTO2.setId(exchangeIntegrationDTO1.getId());
        assertThat(exchangeIntegrationDTO1).isEqualTo(exchangeIntegrationDTO2);
        exchangeIntegrationDTO2.setId(2L);
        assertThat(exchangeIntegrationDTO1).isNotEqualTo(exchangeIntegrationDTO2);
        exchangeIntegrationDTO1.setId(null);
        assertThat(exchangeIntegrationDTO1).isNotEqualTo(exchangeIntegrationDTO2);
    }
}
