package com.rnexchange.service.marketdata;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.service.dto.BarDTO;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BarAggregatorTest {

    @Test
    @DisplayName("should create session-level OHLC bar aligned to minute boundary")
    void shouldCreateBarFromInstrumentState() {
        Clock clock = Clock.fixed(Instant.parse("2025-11-14T09:30:30Z"), ZoneOffset.UTC);
        InstrumentState state = new InstrumentState("RELIANCE", "NSE", new BigDecimal("100.00"), 0.01d, clock);

        state.updateWithTick(new BigDecimal("101.25"), 200L);
        state.updateWithTick(new BigDecimal("98.75"), 150L);
        state.updateWithTick(new BigDecimal("100.55"), 100L);

        BarAggregator aggregator = new BarAggregator();
        BarDTO bar = aggregator.createBar(state);

        assertThat(bar.symbol()).isEqualTo("RELIANCE");
        assertThat(bar.open()).isEqualByComparingTo("100.00");
        assertThat(bar.high()).isEqualByComparingTo("101.25");
        assertThat(bar.low()).isEqualByComparingTo("98.75");
        assertThat(bar.close()).isEqualByComparingTo("100.55");
        assertThat(bar.volume()).isEqualTo(450L);
        assertThat(bar.timestamp()).isEqualTo(state.getLastUpdated().truncatedTo(ChronoUnit.MINUTES));
    }
}
