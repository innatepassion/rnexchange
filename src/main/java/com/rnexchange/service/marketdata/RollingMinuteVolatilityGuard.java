package com.rnexchange.service.marketdata;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class RollingMinuteVolatilityGuard {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    private final Duration window;
    private final BigDecimal bandPercent;
    private final Clock clock;
    private final Map<String, GuardEntry> entries = new ConcurrentHashMap<>();

    public RollingMinuteVolatilityGuard(Duration window, BigDecimal bandPercent, Clock clock) {
        this.window = Objects.requireNonNull(window, "window must not be null");
        this.bandPercent = Objects.requireNonNull(bandPercent, "bandPercent must not be null");
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public RollingMinuteVolatilityGuard(Duration window, BigDecimal bandPercent) {
        this(window, bandPercent, Clock.systemUTC());
    }

    public RollingMinuteVolatilityGuard() {
        this(Duration.ofSeconds(60), new BigDecimal("0.05"));
    }

    public void register(String symbol, BigDecimal anchorPrice, BigDecimal candidatePrice) {
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(anchorPrice, "anchorPrice must not be null");
        Objects.requireNonNull(candidatePrice, "candidatePrice must not be null");

        GuardEntry entry = entries.computeIfAbsent(symbol, s -> new GuardEntry(anchorPrice));
        Instant now = clock.instant();

        synchronized (entry) {
            entry.anchorPrice = anchorPrice;
            pruneExpiredSamples(entry, now);
            entry.samples.addLast(new PriceSample(now, candidatePrice));
            entry.lastUpdated = now;
            evaluate(entry);
        }
    }

    public boolean canMoveUp(String symbol) {
        GuardEntry entry = entries.get(symbol);
        if (entry == null) {
            return true;
        }
        synchronized (entry) {
            return !entry.upSuppressed;
        }
    }

    public boolean canMoveDown(String symbol) {
        GuardEntry entry = entries.get(symbol);
        if (entry == null) {
            return true;
        }
        synchronized (entry) {
            return !entry.downSuppressed;
        }
    }

    public Optional<GuardSnapshot> snapshot(String symbol) {
        GuardEntry entry = entries.get(symbol);
        if (entry == null) {
            return Optional.empty();
        }
        synchronized (entry) {
            return Optional.of(new GuardSnapshot(entry.upSuppressed, entry.downSuppressed, entry.anchorPrice, entry.lastUpdated));
        }
    }

    private void pruneExpiredSamples(GuardEntry entry, Instant now) {
        Instant floor = now.minus(window);
        while (!entry.samples.isEmpty() && entry.samples.peekFirst().timestamp().isBefore(floor)) {
            entry.samples.removeFirst();
        }
    }

    private void evaluate(GuardEntry entry) {
        if (entry.anchorPrice.signum() == 0) {
            entry.upSuppressed = false;
            entry.downSuppressed = false;
            return;
        }

        BigDecimal upperBound = entry.anchorPrice
            .multiply(BigDecimal.ONE.add(bandPercent, MATH_CONTEXT), MATH_CONTEXT)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal lowerBound = entry.anchorPrice
            .multiply(BigDecimal.ONE.subtract(bandPercent, MATH_CONTEXT), MATH_CONTEXT)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal maxPrice = entry.samples.stream().map(PriceSample::price).max(BigDecimal::compareTo).orElse(entry.anchorPrice);
        BigDecimal minPrice = entry.samples.stream().map(PriceSample::price).min(BigDecimal::compareTo).orElse(entry.anchorPrice);

        entry.upSuppressed = maxPrice.compareTo(upperBound) > 0;
        entry.downSuppressed = minPrice.compareTo(lowerBound) < 0;
    }

    private static final class GuardEntry {

        private final Deque<PriceSample> samples = new ArrayDeque<>();
        private BigDecimal anchorPrice;
        private boolean upSuppressed;
        private boolean downSuppressed;
        private Instant lastUpdated;

        private GuardEntry(BigDecimal anchorPrice) {
            this.anchorPrice = anchorPrice;
        }
    }

    private record PriceSample(Instant timestamp, BigDecimal price) {}

    public record GuardSnapshot(boolean upSuppressed, boolean downSuppressed, BigDecimal anchorPrice, Instant lastUpdated) {}
}
