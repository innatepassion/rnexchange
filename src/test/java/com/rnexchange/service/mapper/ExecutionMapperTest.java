package com.rnexchange.service.mapper;

import static com.rnexchange.domain.ExecutionAsserts.*;
import static com.rnexchange.domain.ExecutionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExecutionMapperTest {

    private ExecutionMapper executionMapper;

    @BeforeEach
    void setUp() {
        executionMapper = new ExecutionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getExecutionSample1();
        var actual = executionMapper.toEntity(executionMapper.toDto(expected));
        assertExecutionAllPropertiesEquals(expected, actual);
    }
}
