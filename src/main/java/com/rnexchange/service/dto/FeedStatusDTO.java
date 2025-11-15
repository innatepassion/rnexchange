package com.rnexchange.service.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record FeedStatusDTO(@NotNull FeedState globalState, Instant startedAt, @NotNull List<ExchangeStatusDTO> exchanges) {}
