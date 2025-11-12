package com.rnexchange.service.mapper;

import static com.rnexchange.domain.ExchangeIntegrationAsserts.*;
import static com.rnexchange.domain.ExchangeIntegrationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeIntegrationMapperTest {

    private ExchangeIntegrationMapper exchangeIntegrationMapper;

    @BeforeEach
    void setUp() {
        exchangeIntegrationMapper = new ExchangeIntegrationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getExchangeIntegrationSample1();
        var actual = exchangeIntegrationMapper.toEntity(exchangeIntegrationMapper.toDto(expected));
        assertExchangeIntegrationAllPropertiesEquals(expected, actual);
    }
}
