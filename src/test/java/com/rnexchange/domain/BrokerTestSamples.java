package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class BrokerTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Broker getBrokerSample1() {
        return new Broker().id(1L).code("code1").name("name1").status("status1");
    }

    public static Broker getBrokerSample2() {
        return new Broker().id(2L).code("code2").name("name2").status("status2");
    }

    public static Broker getBrokerRandomSampleGenerator() {
        return new Broker()
            .id(longCount.incrementAndGet())
            .code(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .status(UUID.randomUUID().toString());
    }
}
