package com.rnexchange.service.marketdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.DoubleSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PriceGeneratorTest {

    @Test
    @DisplayName("should apply bounded geometric random walk and round to two decimals")
    void shouldApplyBoundedRandomWalk() {
        StubGaussian gaussian = new StubGaussian(0.5d, 0.5d, 0.5d);
        PriceGenerator generator = new PriceGenerator(
            gaussian,
            new BigDecimal("1.00"),
            new BigDecimal("1000.00"),
            3,
            MathContext.DECIMAL64
        );

        BigDecimal open = new BigDecimal("100.00");
        BigDecimal next = generator.nextPrice(open, open, 0.01d);

        double factorPerStep = Math.exp(0.01d * 0.5d);
        BigDecimal expected = open
            .multiply(BigDecimal.valueOf(factorPerStep * factorPerStep * factorPerStep), MathContext.DECIMAL64)
            .setScale(2, RoundingMode.HALF_UP);

        assertThat(next).isEqualByComparingTo(expected);
        assertThat(gaussian.calls).isEqualTo(3);
    }

    @Test
    @DisplayName("should clamp price to configured floor and ceiling")
    void shouldClampToBounds() {
        StubGaussian minGaussian = new StubGaussian(-100.0d);
        PriceGenerator minGenerator = new PriceGenerator(
            minGaussian,
            new BigDecimal("95.00"),
            new BigDecimal("105.00"),
            1,
            MathContext.DECIMAL64
        );

        BigDecimal open = new BigDecimal("100.00");
        BigDecimal floorResult = minGenerator.nextPrice(open, open, 0.05d);
        assertThat(floorResult).isEqualByComparingTo("95.00");

        StubGaussian maxGaussian = new StubGaussian(100.0d);
        PriceGenerator maxGenerator = new PriceGenerator(
            maxGaussian,
            new BigDecimal("95.00"),
            new BigDecimal("105.00"),
            1,
            MathContext.DECIMAL64
        );

        BigDecimal ceilingResult = maxGenerator.nextPrice(open, open, 0.05d);
        assertThat(ceilingResult).isEqualByComparingTo("105.00");
    }

    private static final class StubGaussian implements DoubleSupplier {

        private final Queue<Double> values = new ArrayDeque<>();
        private int calls;

        private StubGaussian(double... values) {
            for (double value : values) {
                this.values.add(value);
            }
        }

        @Override
        public double getAsDouble() {
            calls++;
            if (values.isEmpty()) {
                throw new IllegalStateException("No more gaussian samples configured");
            }
            return values.poll();
        }
    }
}
