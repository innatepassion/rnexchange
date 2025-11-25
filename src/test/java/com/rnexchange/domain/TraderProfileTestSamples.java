package com.rnexchange.domain;

import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.KycStatus;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TraderProfileTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static TraderProfile getTraderProfileSample1() {
        return new TraderProfile()
            .id(1L)
            .displayName("displayName1")
            .email("display1@example.test")
            .mobile("mobile1")
            .kycStatus(KycStatus.APPROVED)
            .status(AccountStatus.ACTIVE);
    }

    public static TraderProfile getTraderProfileSample2() {
        return new TraderProfile()
            .id(2L)
            .displayName("displayName2")
            .email("display2@example.test")
            .mobile("mobile2")
            .kycStatus(KycStatus.PENDING)
            .status(AccountStatus.INACTIVE);
    }

    public static TraderProfile getTraderProfileRandomSampleGenerator() {
        return new TraderProfile()
            .id(longCount.incrementAndGet())
            .displayName(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString() + "@example.test")
            .mobile(UUID.randomUUID().toString())
            .kycStatus(random.nextBoolean() ? KycStatus.APPROVED : KycStatus.PENDING)
            .status(random.nextBoolean() ? AccountStatus.ACTIVE : AccountStatus.SUSPENDED);
    }
}
