package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MarketHolidayTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static MarketHoliday getMarketHolidaySample1() {
        return new MarketHoliday().id(1L).reason("reason1");
    }

    public static MarketHoliday getMarketHolidaySample2() {
        return new MarketHoliday().id(2L).reason("reason2");
    }

    public static MarketHoliday getMarketHolidayRandomSampleGenerator() {
        return new MarketHoliday().id(longCount.incrementAndGet()).reason(UUID.randomUUID().toString());
    }
}
