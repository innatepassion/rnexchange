package com.rnexchange.service.mapper;

import static com.rnexchange.domain.MarketHolidayAsserts.*;
import static com.rnexchange.domain.MarketHolidayTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MarketHolidayMapperTest {

    private MarketHolidayMapper marketHolidayMapper;

    @BeforeEach
    void setUp() {
        marketHolidayMapper = new MarketHolidayMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMarketHolidaySample1();
        var actual = marketHolidayMapper.toEntity(marketHolidayMapper.toDto(expected));
        assertMarketHolidayAllPropertiesEquals(expected, actual);
    }
}
