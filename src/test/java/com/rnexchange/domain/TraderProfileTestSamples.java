package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TraderProfileTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static TraderProfile getTraderProfileSample1() {
        return new TraderProfile().id(1L).displayName("displayName1").email("email1").mobile("mobile1");
    }

    public static TraderProfile getTraderProfileSample2() {
        return new TraderProfile().id(2L).displayName("displayName2").email("email2").mobile("mobile2");
    }

    public static TraderProfile getTraderProfileRandomSampleGenerator() {
        return new TraderProfile()
            .id(longCount.incrementAndGet())
            .displayName(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .mobile(UUID.randomUUID().toString());
    }
}
