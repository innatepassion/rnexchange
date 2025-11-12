package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class RiskAlertTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static RiskAlert getRiskAlertSample1() {
        return new RiskAlert().id(1L).description("description1");
    }

    public static RiskAlert getRiskAlertSample2() {
        return new RiskAlert().id(2L).description("description2");
    }

    public static RiskAlert getRiskAlertRandomSampleGenerator() {
        return new RiskAlert().id(longCount.incrementAndGet()).description(UUID.randomUUID().toString());
    }
}
