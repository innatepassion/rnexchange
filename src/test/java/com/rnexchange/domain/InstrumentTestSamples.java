package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class InstrumentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Instrument getInstrumentSample1() {
        return new Instrument().id(1L).symbol("symbol1").name("name1").exchangeCode("exchangeCode1").lotSize(1L).status("status1");
    }

    public static Instrument getInstrumentSample2() {
        return new Instrument().id(2L).symbol("symbol2").name("name2").exchangeCode("exchangeCode2").lotSize(2L).status("status2");
    }

    public static Instrument getInstrumentRandomSampleGenerator() {
        return new Instrument()
            .id(longCount.incrementAndGet())
            .symbol(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .exchangeCode(UUID.randomUUID().toString())
            .lotSize(longCount.incrementAndGet())
            .status(UUID.randomUUID().toString());
    }
}
