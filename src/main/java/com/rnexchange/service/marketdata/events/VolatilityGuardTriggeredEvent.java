package com.rnexchange.service.marketdata.events;

import com.rnexchange.service.dto.FeedState;
import java.time.Instant;
import java.util.Objects;

public record VolatilityGuardTriggeredEvent(String symbol, String exchange, FeedState guardState, String direction, Instant timestamp) {
    public VolatilityGuardTriggeredEvent {
        symbol = Objects.requireNonNull(symbol, "symbol must not be null");
        exchange = Objects.requireNonNull(exchange, "exchange must not be null");
        guardState = Objects.requireNonNull(guardState, "guardState must not be null");
        direction = Objects.requireNonNull(direction, "direction must not be null");
        timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    }
}
