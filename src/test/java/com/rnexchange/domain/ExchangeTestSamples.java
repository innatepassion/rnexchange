package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ExchangeTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Exchange getExchangeSample1() {
        return new Exchange().id(1L).code("code1").name("name1").timezone("timezone1");
    }

    public static Exchange getExchangeSample2() {
        return new Exchange().id(2L).code("code2").name("name2").timezone("timezone2");
    }

    public static Exchange getExchangeRandomSampleGenerator() {
        return new Exchange()
            .id(longCount.incrementAndGet())
            .code(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .timezone(UUID.randomUUID().toString());
    }
}
