package com.rnexchange.service.mapper;

import static com.rnexchange.domain.SettlementBatchAsserts.*;
import static com.rnexchange.domain.SettlementBatchTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SettlementBatchMapperTest {

    private SettlementBatchMapper settlementBatchMapper;

    @BeforeEach
    void setUp() {
        settlementBatchMapper = new SettlementBatchMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getSettlementBatchSample1();
        var actual = settlementBatchMapper.toEntity(settlementBatchMapper.toDto(expected));
        assertSettlementBatchAllPropertiesEquals(expected, actual);
    }
}
