package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TradingAccountDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TradingAccountDTO.class);
        TradingAccountDTO tradingAccountDTO1 = new TradingAccountDTO();
        tradingAccountDTO1.setId(1L);
        TradingAccountDTO tradingAccountDTO2 = new TradingAccountDTO();
        assertThat(tradingAccountDTO1).isNotEqualTo(tradingAccountDTO2);
        tradingAccountDTO2.setId(tradingAccountDTO1.getId());
        assertThat(tradingAccountDTO1).isEqualTo(tradingAccountDTO2);
        tradingAccountDTO2.setId(2L);
        assertThat(tradingAccountDTO1).isNotEqualTo(tradingAccountDTO2);
        tradingAccountDTO1.setId(null);
        assertThat(tradingAccountDTO1).isNotEqualTo(tradingAccountDTO2);
    }
}
