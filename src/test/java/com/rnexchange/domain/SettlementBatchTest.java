package com.rnexchange.domain;

import static com.rnexchange.domain.ExchangeTestSamples.*;
import static com.rnexchange.domain.SettlementBatchTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SettlementBatchTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SettlementBatch.class);
        SettlementBatch settlementBatch1 = getSettlementBatchSample1();
        SettlementBatch settlementBatch2 = new SettlementBatch();
        assertThat(settlementBatch1).isNotEqualTo(settlementBatch2);

        settlementBatch2.setId(settlementBatch1.getId());
        assertThat(settlementBatch1).isEqualTo(settlementBatch2);

        settlementBatch2 = getSettlementBatchSample2();
        assertThat(settlementBatch1).isNotEqualTo(settlementBatch2);
    }

    @Test
    void exchangeTest() {
        SettlementBatch settlementBatch = getSettlementBatchRandomSampleGenerator();
        Exchange exchangeBack = getExchangeRandomSampleGenerator();

        settlementBatch.setExchange(exchangeBack);
        assertThat(settlementBatch.getExchange()).isEqualTo(exchangeBack);

        settlementBatch.exchange(null);
        assertThat(settlementBatch.getExchange()).isNull();
    }
}
