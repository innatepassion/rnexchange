package com.rnexchange.service.mapper;

import static com.rnexchange.domain.ExchangeOperatorAsserts.*;
import static com.rnexchange.domain.ExchangeOperatorTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeOperatorMapperTest {

    private ExchangeOperatorMapper exchangeOperatorMapper;

    @BeforeEach
    void setUp() {
        exchangeOperatorMapper = new ExchangeOperatorMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getExchangeOperatorSample1();
        var actual = exchangeOperatorMapper.toEntity(exchangeOperatorMapper.toDto(expected));
        assertExchangeOperatorAllPropertiesEquals(expected, actual);
    }
}
