package com.rnexchange.service.marketdata;

import com.rnexchange.service.dto.FeedState;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record FeedStatus(FeedState globalState, Instant startedAt, List<ExchangeStatus> exchanges) {
    public FeedStatus {
        globalState = Objects.requireNonNull(globalState, "globalState must not be null");
        exchanges = List.copyOf(Objects.requireNonNull(exchanges, "exchanges must not be null"));
    }
}
