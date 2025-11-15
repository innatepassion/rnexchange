package com.rnexchange.service.marketdata;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

public class PriceGenerator {

    private final DoubleSupplier gaussianSupplier;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final int batchSize;
    private final MathContext mathContext;

    public PriceGenerator(
        DoubleSupplier gaussianSupplier,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        int batchSize,
        MathContext mathContext
    ) {
        this.gaussianSupplier = Objects.requireNonNull(gaussianSupplier, "gaussianSupplier must not be null");
        this.minPrice = Objects.requireNonNull(minPrice, "minPrice must not be null");
        this.maxPrice = Objects.requireNonNull(maxPrice, "maxPrice must not be null");
        this.batchSize = Math.max(1, batchSize);
        this.mathContext = mathContext == null ? MathContext.DECIMAL64 : mathContext;
    }

    public PriceGenerator(BigDecimal minPrice, BigDecimal maxPrice, int batchSize) {
        this(ThreadLocalRandom.current()::nextGaussian, minPrice, maxPrice, batchSize, MathContext.DECIMAL64);
    }

    public PriceGenerator() {
        this(BigDecimal.ONE, new BigDecimal("10000.00"), 1);
    }

    public BigDecimal nextPrice(BigDecimal lastPrice, BigDecimal openPrice, double volatility) {
        Objects.requireNonNull(lastPrice, "lastPrice must not be null");
        Objects.requireNonNull(openPrice, "openPrice must not be null");

        BigDecimal current = lastPrice;

        for (int i = 0; i < batchSize; i++) {
            double gaussian = gaussianSupplier.getAsDouble();
            double factor = Math.exp(volatility * gaussian);
            current = current.multiply(BigDecimal.valueOf(factor), mathContext);
            current = clamp(current);
        }

        return current.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal clamp(BigDecimal price) {
        if (price.compareTo(minPrice) < 0) {
            return minPrice.setScale(2, RoundingMode.HALF_UP);
        }
        if (price.compareTo(maxPrice) > 0) {
            return maxPrice.setScale(2, RoundingMode.HALF_UP);
        }
        return price;
    }
}
