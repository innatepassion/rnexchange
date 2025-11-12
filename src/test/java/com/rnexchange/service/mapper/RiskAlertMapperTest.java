package com.rnexchange.service.mapper;

import static com.rnexchange.domain.RiskAlertAsserts.*;
import static com.rnexchange.domain.RiskAlertTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RiskAlertMapperTest {

    private RiskAlertMapper riskAlertMapper;

    @BeforeEach
    void setUp() {
        riskAlertMapper = new RiskAlertMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getRiskAlertSample1();
        var actual = riskAlertMapper.toEntity(riskAlertMapper.toDto(expected));
        assertRiskAlertAllPropertiesEquals(expected, actual);
    }
}
