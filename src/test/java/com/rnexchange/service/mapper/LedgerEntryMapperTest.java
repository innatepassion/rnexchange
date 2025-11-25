package com.rnexchange.service.mapper;

import static com.rnexchange.domain.LedgerEntryAsserts.*;
import static com.rnexchange.domain.LedgerEntryTestSamples.*;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LedgerEntryMapperTest {

    private LedgerEntryMapper ledgerEntryMapper;

    @BeforeEach
    void setUp() throws Exception {
        LedgerEntryMapperImpl mapperImpl = new LedgerEntryMapperImpl();
        TradingAccountMapper tradingAccountMapper = new TradingAccountMapperImpl();
        Field field = LedgerEntryMapperImpl.class.getDeclaredField("tradingAccountMapper");
        field.setAccessible(true);
        field.set(mapperImpl, tradingAccountMapper);
        ledgerEntryMapper = mapperImpl;
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getLedgerEntrySample1();
        var actual = ledgerEntryMapper.toEntity(ledgerEntryMapper.toDto(expected));
        assertLedgerEntryAllPropertiesEquals(expected, actual);
    }
}
