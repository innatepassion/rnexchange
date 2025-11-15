package com.rnexchange.service.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record QuoteDTO(
    @NotNull String symbol,
    @NotNull BigDecimal lastPrice,
    @NotNull BigDecimal open,
    @NotNull BigDecimal change,
    @NotNull BigDecimal changePercent,
    long volume,
    @NotNull Instant timestamp
) {}
