package com.rnexchange.service.mapper;

import static com.rnexchange.domain.PositionAsserts.*;
import static com.rnexchange.domain.PositionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PositionMapperTest {

    private PositionMapper positionMapper;

    @BeforeEach
    void setUp() {
        positionMapper = new PositionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPositionSample1();
        var actual = positionMapper.toEntity(positionMapper.toDto(expected));
        assertPositionAllPropertiesEquals(expected, actual);
    }
}
