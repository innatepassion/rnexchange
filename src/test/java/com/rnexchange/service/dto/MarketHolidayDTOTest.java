package com.rnexchange.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MarketHolidayDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MarketHolidayDTO.class);
        MarketHolidayDTO marketHolidayDTO1 = new MarketHolidayDTO();
        marketHolidayDTO1.setId(1L);
        MarketHolidayDTO marketHolidayDTO2 = new MarketHolidayDTO();
        assertThat(marketHolidayDTO1).isNotEqualTo(marketHolidayDTO2);
        marketHolidayDTO2.setId(marketHolidayDTO1.getId());
        assertThat(marketHolidayDTO1).isEqualTo(marketHolidayDTO2);
        marketHolidayDTO2.setId(2L);
        assertThat(marketHolidayDTO1).isNotEqualTo(marketHolidayDTO2);
        marketHolidayDTO1.setId(null);
        assertThat(marketHolidayDTO1).isNotEqualTo(marketHolidayDTO2);
    }
}
