package com.rnexchange.service.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ExchangeStatusDTO(
    @NotNull String exchangeCode,
    @NotNull FeedState state,
    Instant lastTickTime,
    int ticksPerSecond,
    int activeInstruments
) {}
