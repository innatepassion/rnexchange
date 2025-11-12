package com.rnexchange.service.mapper;

import static com.rnexchange.domain.LedgerEntryAsserts.*;
import static com.rnexchange.domain.LedgerEntryTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LedgerEntryMapperTest {

    private LedgerEntryMapper ledgerEntryMapper;

    @BeforeEach
    void setUp() {
        ledgerEntryMapper = new LedgerEntryMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getLedgerEntrySample1();
        var actual = ledgerEntryMapper.toEntity(ledgerEntryMapper.toDto(expected));
        assertLedgerEntryAllPropertiesEquals(expected, actual);
    }
}
