package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ExecutionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Execution getExecutionSample1() {
        return new Execution().id(1L).liquidity("liquidity1");
    }

    public static Execution getExecutionSample2() {
        return new Execution().id(2L).liquidity("liquidity2");
    }

    public static Execution getExecutionRandomSampleGenerator() {
        return new Execution().id(longCount.incrementAndGet()).liquidity(UUID.randomUUID().toString());
    }
}
