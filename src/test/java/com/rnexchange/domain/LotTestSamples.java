package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class LotTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Lot getLotSample1() {
        return new Lot().id(1L).method("method1");
    }

    public static Lot getLotSample2() {
        return new Lot().id(2L).method("method2");
    }

    public static Lot getLotRandomSampleGenerator() {
        return new Lot().id(longCount.incrementAndGet()).method(UUID.randomUUID().toString());
    }
}
