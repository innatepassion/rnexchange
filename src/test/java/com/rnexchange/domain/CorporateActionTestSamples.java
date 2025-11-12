package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CorporateActionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static CorporateAction getCorporateActionSample1() {
        return new CorporateAction().id(1L).instrumentSymbol("instrumentSymbol1");
    }

    public static CorporateAction getCorporateActionSample2() {
        return new CorporateAction().id(2L).instrumentSymbol("instrumentSymbol2");
    }

    public static CorporateAction getCorporateActionRandomSampleGenerator() {
        return new CorporateAction().id(longCount.incrementAndGet()).instrumentSymbol(UUID.randomUUID().toString());
    }
}
