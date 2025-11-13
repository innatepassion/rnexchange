package com.rnexchange.service.dto;

import java.math.BigDecimal;

public record MarginAssessment(
    BigDecimal initialRequirement,
    BigDecimal maintenanceRequirement,
    BigDecimal availableBalance,
    BigDecimal remainingBalance,
    boolean sufficient
) {}
