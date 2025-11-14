package com.rnexchange.service.marketdata;

import com.rnexchange.service.dto.FeedState;
import java.time.Instant;
import java.util.Objects;

public record ExchangeStatus(String exchangeCode, FeedState state, Instant lastTickTime, int ticksPerSecond, int activeInstruments) {
    public ExchangeStatus {
        exchangeCode = Objects.requireNonNull(exchangeCode, "exchangeCode must not be null");
        state = Objects.requireNonNull(state, "state must not be null");
    }
}
