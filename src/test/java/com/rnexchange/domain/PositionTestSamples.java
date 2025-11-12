package com.rnexchange.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class PositionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Position getPositionSample1() {
        return new Position().id(1L);
    }

    public static Position getPositionSample2() {
        return new Position().id(2L);
    }

    public static Position getPositionRandomSampleGenerator() {
        return new Position().id(longCount.incrementAndGet());
    }
}
