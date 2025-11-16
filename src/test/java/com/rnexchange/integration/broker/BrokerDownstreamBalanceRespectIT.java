package com.rnexchange.integration.broker;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.TradingAccountRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class BrokerDownstreamBalanceRespectIT {

    @Autowired
    private TradingAccountRepository tradingAccountRepository;

    @Test
    @Transactional
    void updatedBalanceIsVisibleToDownstreamReaders() {
        // This is a lightweight placeholder asserting persistence visibility; deeper downstream
        // components (trading/risk) would be exercised in their modules.
        TradingAccount acc = tradingAccountRepository.findAll().stream().findFirst().orElseThrow();
        BigDecimal before = acc.getBalance();
        acc.setBalance(before.add(new BigDecimal("10.00")));
        tradingAccountRepository.saveAndFlush(acc);

        TradingAccount read = tradingAccountRepository.findById(acc.getId()).orElseThrow();
        assertThat(read.getBalance()).isEqualByComparingTo(before.add(new BigDecimal("10.00")));
    }
}
