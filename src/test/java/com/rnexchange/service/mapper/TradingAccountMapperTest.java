package com.rnexchange.service.mapper;

import static com.rnexchange.domain.TradingAccountAsserts.*;
import static com.rnexchange.domain.TradingAccountTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TradingAccountMapperTest {

    private TradingAccountMapper tradingAccountMapper;

    @BeforeEach
    void setUp() {
        tradingAccountMapper = new TradingAccountMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTradingAccountSample1();
        var actual = tradingAccountMapper.toEntity(tradingAccountMapper.toDto(expected));
        assertTradingAccountAllPropertiesEquals(expected, actual);
    }
}
