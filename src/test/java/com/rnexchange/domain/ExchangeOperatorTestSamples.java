package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ExchangeOperatorTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static ExchangeOperator getExchangeOperatorSample1() {
        return new ExchangeOperator().id(1L).name("name1");
    }

    public static ExchangeOperator getExchangeOperatorSample2() {
        return new ExchangeOperator().id(2L).name("name2");
    }

    public static ExchangeOperator getExchangeOperatorRandomSampleGenerator() {
        return new ExchangeOperator().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString());
    }
}
