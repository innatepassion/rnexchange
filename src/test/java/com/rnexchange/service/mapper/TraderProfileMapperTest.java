package com.rnexchange.service.mapper;

import static com.rnexchange.domain.TraderProfileAsserts.*;
import static com.rnexchange.domain.TraderProfileTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TraderProfileMapperTest {

    private TraderProfileMapper traderProfileMapper;

    @BeforeEach
    void setUp() {
        traderProfileMapper = new TraderProfileMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTraderProfileSample1();
        var actual = traderProfileMapper.toEntity(traderProfileMapper.toDto(expected));
        assertTraderProfileAllPropertiesEquals(expected, actual);
    }
}
