package com.rnexchange.service.marketdata.events;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record FeedStartedEvent(List<String> exchangeCodes, String triggeredBy, Instant timestamp) {
    public FeedStartedEvent {
        exchangeCodes = List.copyOf(Objects.requireNonNull(exchangeCodes, "exchangeCodes must not be null"));
        triggeredBy = Objects.requireNonNull(triggeredBy, "triggeredBy must not be null");
        timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    }
}
