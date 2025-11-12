package com.rnexchange.service.mapper;

import static com.rnexchange.domain.InstrumentAsserts.*;
import static com.rnexchange.domain.InstrumentTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InstrumentMapperTest {

    private InstrumentMapper instrumentMapper;

    @BeforeEach
    void setUp() {
        instrumentMapper = new InstrumentMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getInstrumentSample1();
        var actual = instrumentMapper.toEntity(instrumentMapper.toDto(expected));
        assertInstrumentAllPropertiesEquals(expected, actual);
    }
}
