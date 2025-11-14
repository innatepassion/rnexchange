package com.rnexchange.service.marketdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InstrumentStateTest {

    @Test
    @DisplayName("should initialize state with open price and zero volume")
    void shouldInitializeState() {
        MutableClock clock = new MutableClock(Instant.parse("2025-11-14T09:15:00Z"));

        InstrumentState state = new InstrumentState("INFY", "NSE", new BigDecimal("100.00"), 0.02d, clock);

        assertThat(state.getSymbol()).isEqualTo("INFY");
        assertThat(state.getExchangeCode()).isEqualTo("NSE");
        assertThat(state.getSessionOpen()).isEqualByComparingTo("100.00");
        assertThat(state.getLastPrice()).isEqualByComparingTo("100.00");
        assertThat(state.getSessionHigh()).isEqualByComparingTo("100.00");
        assertThat(state.getSessionLow()).isEqualByComparingTo("100.00");
        assertThat(state.getCumulativeVolume()).isZero();
        assertThat(state.getLastUpdated()).isEqualTo(clock.instant());
    }

    @Test
    @DisplayName("should update quotes, highs, lows, volume, and derived metrics")
    void shouldUpdateStateWithTick() {
        MutableClock clock = new MutableClock(Instant.parse("2025-11-14T09:15:00Z"));
        InstrumentState state = new InstrumentState("INFY", "NSE", new BigDecimal("100.00"), 0.02d, clock);

        clock.advanceSeconds(1);
        state.updateWithTick(new BigDecimal("101.50"), 200L);

        assertThat(state.getLastPrice()).isEqualByComparingTo("101.50");
        assertThat(state.getSessionHigh()).isEqualByComparingTo("101.50");
        assertThat(state.getSessionLow()).isEqualByComparingTo("100.00");
        assertThat(state.getCumulativeVolume()).isEqualTo(200L);
        assertThat(state.getLastUpdated()).isEqualTo(clock.instant());
        assertThat(state.getChange()).isEqualByComparingTo("1.50");
        assertThat(state.getChangePercent()).isEqualByComparingTo("1.50");

        clock.advanceSeconds(1);
        state.updateWithTick(new BigDecimal("99.25"), 150L);

        assertThat(state.getLastPrice()).isEqualByComparingTo("99.25");
        assertThat(state.getSessionHigh()).isEqualByComparingTo("101.50");
        assertThat(state.getSessionLow()).isEqualByComparingTo("99.25");
        assertThat(state.getCumulativeVolume()).isEqualTo(350L);
        assertThat(state.getLastUpdated()).isEqualTo(clock.instant());
        assertThat(state.getChange()).isEqualByComparingTo("-0.75");
        assertThat(state.getChangePercent()).isEqualByComparingTo("-0.75");
    }

    private static final class MutableClock extends Clock {

        private Instant current;

        private MutableClock(Instant seed) {
            this.current = seed;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(current, zone);
        }

        @Override
        public Instant instant() {
            return current;
        }

        private void advanceSeconds(long seconds) {
            current = current.plusSeconds(seconds);
        }
    }
}
