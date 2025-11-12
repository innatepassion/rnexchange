package com.rnexchange.service.mapper;

import static com.rnexchange.domain.DailySettlementPriceAsserts.*;
import static com.rnexchange.domain.DailySettlementPriceTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DailySettlementPriceMapperTest {

    private DailySettlementPriceMapper dailySettlementPriceMapper;

    @BeforeEach
    void setUp() {
        dailySettlementPriceMapper = new DailySettlementPriceMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDailySettlementPriceSample1();
        var actual = dailySettlementPriceMapper.toEntity(dailySettlementPriceMapper.toDto(expected));
        assertDailySettlementPriceAllPropertiesEquals(expected, actual);
    }
}
