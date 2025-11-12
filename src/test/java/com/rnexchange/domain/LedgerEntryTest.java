package com.rnexchange.domain;

import static com.rnexchange.domain.LedgerEntryTestSamples.*;
import static com.rnexchange.domain.TradingAccountTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class LedgerEntryTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(LedgerEntry.class);
        LedgerEntry ledgerEntry1 = getLedgerEntrySample1();
        LedgerEntry ledgerEntry2 = new LedgerEntry();
        assertThat(ledgerEntry1).isNotEqualTo(ledgerEntry2);

        ledgerEntry2.setId(ledgerEntry1.getId());
        assertThat(ledgerEntry1).isEqualTo(ledgerEntry2);

        ledgerEntry2 = getLedgerEntrySample2();
        assertThat(ledgerEntry1).isNotEqualTo(ledgerEntry2);
    }

    @Test
    void tradingAccountTest() {
        LedgerEntry ledgerEntry = getLedgerEntryRandomSampleGenerator();
        TradingAccount tradingAccountBack = getTradingAccountRandomSampleGenerator();

        ledgerEntry.setTradingAccount(tradingAccountBack);
        assertThat(ledgerEntry.getTradingAccount()).isEqualTo(tradingAccountBack);

        ledgerEntry.tradingAccount(null);
        assertThat(ledgerEntry.getTradingAccount()).isNull();
    }
}
