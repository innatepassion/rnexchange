package com.rnexchange.service.marketdata.events;

import java.time.Instant;
import java.util.Objects;

public record VolatilityGuardReleasedEvent(String symbol, String exchange, Instant releasedAt) {
    public VolatilityGuardReleasedEvent {
        symbol = Objects.requireNonNull(symbol, "symbol must not be null");
        exchange = Objects.requireNonNull(exchange, "exchange must not be null");
        releasedAt = Objects.requireNonNull(releasedAt, "releasedAt must not be null");
    }
}
