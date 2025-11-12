package com.rnexchange.service.mapper;

import static com.rnexchange.domain.MarginRuleAsserts.*;
import static com.rnexchange.domain.MarginRuleTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MarginRuleMapperTest {

    private MarginRuleMapper marginRuleMapper;

    @BeforeEach
    void setUp() {
        marginRuleMapper = new MarginRuleMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMarginRuleSample1();
        var actual = marginRuleMapper.toEntity(marginRuleMapper.toDto(expected));
        assertMarginRuleAllPropertiesEquals(expected, actual);
    }
}
