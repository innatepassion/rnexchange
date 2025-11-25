package com.rnexchange.domain;

import com.rnexchange.domain.enumeration.OrderSide;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ExecutionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final Instant DEFAULT_EXEC_TS = Instant.ofEpochMilli(0L);
    private static final BigDecimal DEFAULT_PX = BigDecimal.ONE;
    private static final BigDecimal DEFAULT_QTY = BigDecimal.TEN;

    public static Execution getExecutionSample1() {
        return new Execution().id(1L).execTs(DEFAULT_EXEC_TS).side(OrderSide.BUY).px(DEFAULT_PX).qty(DEFAULT_QTY).liquidity("liquidity1");
    }

    public static Execution getExecutionSample2() {
        return new Execution()
            .id(2L)
            .execTs(DEFAULT_EXEC_TS.plusSeconds(1))
            .side(OrderSide.SELL)
            .px(DEFAULT_PX.add(BigDecimal.ONE))
            .qty(DEFAULT_QTY.add(BigDecimal.ONE))
            .liquidity("liquidity2");
    }

    public static Execution getExecutionRandomSampleGenerator() {
        return new Execution()
            .id(longCount.incrementAndGet())
            .execTs(Instant.now())
            .side(random.nextBoolean() ? OrderSide.BUY : OrderSide.SELL)
            .px(BigDecimal.valueOf(Math.abs(random.nextDouble()) + 1))
            .qty(BigDecimal.valueOf(Math.abs(random.nextDouble()) + 1))
            .liquidity(UUID.randomUUID().toString());
    }
}
