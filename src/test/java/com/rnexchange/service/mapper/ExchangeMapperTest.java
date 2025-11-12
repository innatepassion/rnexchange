package com.rnexchange.service.mapper;

import static com.rnexchange.domain.ExchangeAsserts.*;
import static com.rnexchange.domain.ExchangeTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeMapperTest {

    private ExchangeMapper exchangeMapper;

    @BeforeEach
    void setUp() {
        exchangeMapper = new ExchangeMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getExchangeSample1();
        var actual = exchangeMapper.toEntity(exchangeMapper.toDto(expected));
        assertExchangeAllPropertiesEquals(expected, actual);
    }
}
