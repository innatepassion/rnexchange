package com.rnexchange.service.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record BarDTO(
    @NotNull String symbol,
    @NotNull BigDecimal open,
    @NotNull BigDecimal high,
    @NotNull BigDecimal low,
    @NotNull BigDecimal close,
    long volume,
    @NotNull Instant timestamp
) {}
