package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SettlementBatchTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static SettlementBatch getSettlementBatchSample1() {
        return new SettlementBatch().id(1L).remarks("remarks1");
    }

    public static SettlementBatch getSettlementBatchSample2() {
        return new SettlementBatch().id(2L).remarks("remarks2");
    }

    public static SettlementBatch getSettlementBatchRandomSampleGenerator() {
        return new SettlementBatch().id(longCount.incrementAndGet()).remarks(UUID.randomUUID().toString());
    }
}
