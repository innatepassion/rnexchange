package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class LedgerEntryTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static LedgerEntry getLedgerEntrySample1() {
        return new LedgerEntry().id(1L).type("type1").reference("reference1").remarks("remarks1");
    }

    public static LedgerEntry getLedgerEntrySample2() {
        return new LedgerEntry().id(2L).type("type2").reference("reference2").remarks("remarks2");
    }

    public static LedgerEntry getLedgerEntryRandomSampleGenerator() {
        return new LedgerEntry()
            .id(longCount.incrementAndGet())
            .type(UUID.randomUUID().toString())
            .reference(UUID.randomUUID().toString())
            .remarks(UUID.randomUUID().toString());
    }
}
