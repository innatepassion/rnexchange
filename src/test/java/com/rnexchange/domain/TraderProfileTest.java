package com.rnexchange.domain;

import static com.rnexchange.domain.TraderProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TraderProfileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TraderProfile.class);
        TraderProfile traderProfile1 = getTraderProfileSample1();
        TraderProfile traderProfile2 = new TraderProfile();
        assertThat(traderProfile1).isNotEqualTo(traderProfile2);

        traderProfile2.setId(traderProfile1.getId());
        assertThat(traderProfile1).isEqualTo(traderProfile2);

        traderProfile2 = getTraderProfileSample2();
        assertThat(traderProfile1).isNotEqualTo(traderProfile2);
    }
}
