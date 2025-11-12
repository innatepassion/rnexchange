package com.rnexchange.service.mapper;

import static com.rnexchange.domain.BrokerDeskAsserts.*;
import static com.rnexchange.domain.BrokerDeskTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BrokerDeskMapperTest {

    private BrokerDeskMapper brokerDeskMapper;

    @BeforeEach
    void setUp() {
        brokerDeskMapper = new BrokerDeskMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getBrokerDeskSample1();
        var actual = brokerDeskMapper.toEntity(brokerDeskMapper.toDto(expected));
        assertBrokerDeskAllPropertiesEquals(expected, actual);
    }
}
