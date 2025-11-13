package com.rnexchange.service.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class BaselineSeedVerificationIT extends AbstractBaselineSeedIT {

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Test
    @Transactional
    void duplicateInstrumentTriggersVerificationFailure() {
        baselineSeedService.runBaselineSeedBlocking(BaselineSeedRequest.builder().invocationId(UUID.randomUUID()).build());
        long instrumentCountBefore = instrumentRepository.count();

        Exchange nse = exchangeRepository.findAll().stream().filter(exchange -> "NSE".equals(exchange.getCode())).findFirst().orElseThrow();

        Instrument duplicate = new Instrument()
            .symbol("RELIANCE")
            .name("Reliance Industries Duplicate")
            .assetClass(AssetClass.EQUITY)
            .exchangeCode(nse.getCode())
            .tickSize(new BigDecimal("0.05"))
            .lotSize(1L)
            .currency(Currency.INR)
            .status("ACTIVE")
            .exchange(nse);
        instrumentRepository.saveAndFlush(duplicate);

        assertThat(instrumentRepository.count()).isEqualTo(instrumentCountBefore + 1);

        assertThatThrownBy(() ->
            baselineSeedService.runBaselineSeedBlocking(BaselineSeedRequest.builder().invocationId(UUID.randomUUID()).build())
        )
            .isInstanceOf(BaselineSeedVerificationException.class)
            .hasMessageContaining("duplicate");

        assertThat(instrumentRepository.count()).isEqualTo(instrumentCountBefore + 1);
        assertThat(instrumentRepository.findAll())
            .filteredOn(instrument -> "RELIANCE".equals(instrument.getSymbol()))
            .hasSizeGreaterThan(1);
    }
}
