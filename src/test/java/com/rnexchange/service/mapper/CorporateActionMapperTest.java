package com.rnexchange.service.mapper;

import static com.rnexchange.domain.CorporateActionAsserts.*;
import static com.rnexchange.domain.CorporateActionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CorporateActionMapperTest {

    private CorporateActionMapper corporateActionMapper;

    @BeforeEach
    void setUp() {
        corporateActionMapper = new CorporateActionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCorporateActionSample1();
        var actual = corporateActionMapper.toEntity(corporateActionMapper.toDto(expected));
        assertCorporateActionAllPropertiesEquals(expected, actual);
    }
}
