package com.rnexchange.service.marketdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rnexchange.service.dto.FeedState;
import com.rnexchange.service.marketdata.events.FeedStartedEvent;
import com.rnexchange.service.marketdata.events.FeedStoppedEvent;
import com.rnexchange.service.marketdata.events.VolatilityGuardReleasedEvent;
import com.rnexchange.service.marketdata.events.VolatilityGuardTriggeredEvent;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MarketDataEventsTest {

    @Test
    @DisplayName("feed lifecycle events should expose immutable payloads")
    void shouldExposeFeedLifecycleEventPayload() {
        Instant now = Instant.parse("2025-11-14T09:20:00Z");

        FeedStartedEvent started = new FeedStartedEvent(List.of("NSE", "BSE"), "system", now);
        assertThat(started.exchangeCodes()).containsExactly("NSE", "BSE");
        assertThat(started.triggeredBy()).isEqualTo("system");
        assertThat(started.timestamp()).isEqualTo(now);

        FeedStoppedEvent stopped = new FeedStoppedEvent(List.of("NSE"), "system", now, "manual");
        assertThat(stopped.exchangeCodes()).containsExactly("NSE");
        assertThat(stopped.triggeredBy()).isEqualTo("system");
        assertThat(stopped.timestamp()).isEqualTo(now);
        assertThat(stopped.reason()).isEqualTo("manual");
    }

    @Test
    @DisplayName("volatility guard events should expose guard metadata")
    void shouldExposeVolatilityGuardPayload() {
        Instant now = Instant.parse("2025-11-14T09:21:00Z");

        VolatilityGuardTriggeredEvent triggered = new VolatilityGuardTriggeredEvent("RELIANCE", "NSE", FeedState.RUNNING, "UP", now);

        assertThat(triggered.symbol()).isEqualTo("RELIANCE");
        assertThat(triggered.exchange()).isEqualTo("NSE");
        assertThat(triggered.guardState()).isEqualTo(FeedState.RUNNING);
        assertThat(triggered.direction()).isEqualTo("UP");
        assertThat(triggered.timestamp()).isEqualTo(now);

        VolatilityGuardReleasedEvent released = new VolatilityGuardReleasedEvent("RELIANCE", "NSE", now);
        assertThat(released.symbol()).isEqualTo("RELIANCE");
        assertThat(released.exchange()).isEqualTo("NSE");
        assertThat(released.releasedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("event constructors should reject null critical fields")
    void shouldRejectNullArguments() {
        Instant now = Instant.parse("2025-11-14T09:22:00Z");

        assertThatThrownBy(() -> new FeedStartedEvent(null, "system", now)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new FeedStoppedEvent(List.of("NSE"), null, now, "manual")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new VolatilityGuardTriggeredEvent("RELIANCE", null, FeedState.RUNNING, "UP", now)).isInstanceOf(
            NullPointerException.class
        );
        assertThatThrownBy(() -> new VolatilityGuardReleasedEvent("RELIANCE", "NSE", null)).isInstanceOf(NullPointerException.class);
    }
}
