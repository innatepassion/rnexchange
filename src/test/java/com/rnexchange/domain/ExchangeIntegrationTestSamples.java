package com.rnexchange.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ExchangeIntegrationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static ExchangeIntegration getExchangeIntegrationSample1() {
        return new ExchangeIntegration().id(1L).provider("provider1").apiKey("apiKey1").apiSecret("apiSecret1");
    }

    public static ExchangeIntegration getExchangeIntegrationSample2() {
        return new ExchangeIntegration().id(2L).provider("provider2").apiKey("apiKey2").apiSecret("apiSecret2");
    }

    public static ExchangeIntegration getExchangeIntegrationRandomSampleGenerator() {
        return new ExchangeIntegration()
            .id(longCount.incrementAndGet())
            .provider(UUID.randomUUID().toString())
            .apiKey(UUID.randomUUID().toString())
            .apiSecret(UUID.randomUUID().toString());
    }
}
