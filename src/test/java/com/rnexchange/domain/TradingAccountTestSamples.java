package com.rnexchange.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class TradingAccountTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static TradingAccount getTradingAccountSample1() {
        return new TradingAccount().id(1L);
    }

    public static TradingAccount getTradingAccountSample2() {
        return new TradingAccount().id(2L);
    }

    public static TradingAccount getTradingAccountRandomSampleGenerator() {
        return new TradingAccount().id(longCount.incrementAndGet());
    }
}
