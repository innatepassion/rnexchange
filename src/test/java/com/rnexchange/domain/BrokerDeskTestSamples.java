package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class BrokerDeskTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static BrokerDesk getBrokerDeskSample1() {
        return new BrokerDesk().id(1L).name("name1");
    }

    public static BrokerDesk getBrokerDeskSample2() {
        return new BrokerDesk().id(2L).name("name2");
    }

    public static BrokerDesk getBrokerDeskRandomSampleGenerator() {
        return new BrokerDesk().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString());
    }
}
