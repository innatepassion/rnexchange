package com.rnexchange.service.mapper;

import static com.rnexchange.domain.BrokerAsserts.*;
import static com.rnexchange.domain.BrokerTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BrokerMapperTest {

    private BrokerMapper brokerMapper;

    @BeforeEach
    void setUp() {
        brokerMapper = new BrokerMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getBrokerSample1();
        var actual = brokerMapper.toEntity(brokerMapper.toDto(expected));
        assertBrokerAllPropertiesEquals(expected, actual);
    }
}
