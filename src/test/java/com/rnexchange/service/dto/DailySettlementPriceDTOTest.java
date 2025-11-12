package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DailySettlementPriceDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DailySettlementPriceDTO.class);
        DailySettlementPriceDTO dailySettlementPriceDTO1 = new DailySettlementPriceDTO();
        dailySettlementPriceDTO1.setId(1L);
        DailySettlementPriceDTO dailySettlementPriceDTO2 = new DailySettlementPriceDTO();
        assertThat(dailySettlementPriceDTO1).isNotEqualTo(dailySettlementPriceDTO2);
        dailySettlementPriceDTO2.setId(dailySettlementPriceDTO1.getId());
        assertThat(dailySettlementPriceDTO1).isEqualTo(dailySettlementPriceDTO2);
        dailySettlementPriceDTO2.setId(2L);
        assertThat(dailySettlementPriceDTO1).isNotEqualTo(dailySettlementPriceDTO2);
        dailySettlementPriceDTO1.setId(null);
        assertThat(dailySettlementPriceDTO1).isNotEqualTo(dailySettlementPriceDTO2);
    }
}
