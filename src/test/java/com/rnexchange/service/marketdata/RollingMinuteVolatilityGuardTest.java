package com.rnexchange.service.marketdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RollingMinuteVolatilityGuardTest {

    @Test
    @DisplayName("should suppress upward moves beyond +5% band and release after window")
    void shouldSuppressAndReleaseUpwardMoves() {
        ControllableClock clock = new ControllableClock(Instant.parse("2025-11-14T09:15:00Z"));
        RollingMinuteVolatilityGuard guard = new RollingMinuteVolatilityGuard(Duration.ofSeconds(60), new BigDecimal("0.05"), clock);

        String symbol = "INFY";
        BigDecimal anchor = new BigDecimal("100.00");

        guard.register(symbol, anchor, new BigDecimal("104.90"));
        assertThat(guard.canMoveUp(symbol)).isTrue();
        assertThat(guard.canMoveDown(symbol)).isTrue();

        clock.advanceSeconds(1);
        guard.register(symbol, anchor, new BigDecimal("105.60"));
        assertThat(guard.canMoveUp(symbol)).isFalse();
        assertThat(guard.canMoveDown(symbol)).isTrue();

        Optional<RollingMinuteVolatilityGuard.GuardSnapshot> snapshot = guard.snapshot(symbol);
        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().upSuppressed()).isTrue();
        assertThat(snapshot.get().downSuppressed()).isFalse();

        clock.advanceSeconds(61);
        guard.register(symbol, anchor, new BigDecimal("104.20"));

        assertThat(guard.canMoveUp(symbol)).isTrue();
        assertThat(guard.canMoveDown(symbol)).isTrue();
    }

    @Test
    @DisplayName("should suppress downward moves beyond -5% band")
    void shouldSuppressDownwardMoves() {
        ControllableClock clock = new ControllableClock(Instant.parse("2025-11-14T09:20:00Z"));
        RollingMinuteVolatilityGuard guard = new RollingMinuteVolatilityGuard(Duration.ofSeconds(60), new BigDecimal("0.05"), clock);

        String symbol = "TCS";
        BigDecimal anchor = new BigDecimal("100.00");

        guard.register(symbol, anchor, new BigDecimal("94.50"));
        assertThat(guard.canMoveDown(symbol)).isFalse();
        assertThat(guard.canMoveUp(symbol)).isTrue();

        Optional<RollingMinuteVolatilityGuard.GuardSnapshot> snapshot = guard.snapshot(symbol);
        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().downSuppressed()).isTrue();
        assertThat(snapshot.get().upSuppressed()).isFalse();
        assertThat(snapshot.get().anchorPrice()).isEqualByComparingTo(anchor);
    }

    private static final class ControllableClock extends java.time.Clock {

        private Instant current;

        private ControllableClock(Instant seed) {
            this.current = seed;
        }

        @Override
        public java.time.ZoneId getZone() {
            return java.time.ZoneId.of("UTC");
        }

        @Override
        public java.time.Clock withZone(java.time.ZoneId zone) {
            return java.time.Clock.fixed(current, zone);
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
