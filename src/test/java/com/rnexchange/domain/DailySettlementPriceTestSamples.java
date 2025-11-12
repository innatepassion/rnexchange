package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DailySettlementPriceTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static DailySettlementPrice getDailySettlementPriceSample1() {
        return new DailySettlementPrice().id(1L).instrumentSymbol("instrumentSymbol1");
    }

    public static DailySettlementPrice getDailySettlementPriceSample2() {
        return new DailySettlementPrice().id(2L).instrumentSymbol("instrumentSymbol2");
    }

    public static DailySettlementPrice getDailySettlementPriceRandomSampleGenerator() {
        return new DailySettlementPrice().id(longCount.incrementAndGet()).instrumentSymbol(UUID.randomUUID().toString());
    }
}
