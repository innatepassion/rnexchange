package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MarginRuleTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static MarginRule getMarginRuleSample1() {
        return new MarginRule().id(1L).scope("scope1");
    }

    public static MarginRule getMarginRuleSample2() {
        return new MarginRule().id(2L).scope("scope2");
    }

    public static MarginRule getMarginRuleRandomSampleGenerator() {
        return new MarginRule().id(longCount.incrementAndGet()).scope(UUID.randomUUID().toString());
    }
}
