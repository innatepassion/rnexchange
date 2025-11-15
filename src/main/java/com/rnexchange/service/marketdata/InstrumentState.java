package com.rnexchange.service.marketdata;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class InstrumentState {

    private static final MathContext CHANGE_MATH_CONTEXT = MathContext.DECIMAL64;

    private final String symbol;
    private final String exchangeCode;
    private final double volatility;
    private final Clock clock;

    private final BigDecimal sessionOpen;
    private BigDecimal sessionHigh;
    private BigDecimal sessionLow;
    private BigDecimal lastPrice;
    private long cumulativeVolume;
    private Instant lastUpdated;

    public InstrumentState(String symbol, String exchangeCode, BigDecimal openPrice, double volatility, Clock clock) {
        this.symbol = Objects.requireNonNull(symbol, "symbol must not be null");
        this.exchangeCode = Objects.requireNonNull(exchangeCode, "exchangeCode must not be null");
        this.sessionOpen = Objects.requireNonNull(openPrice, "openPrice must not be null");
        this.volatility = volatility;
        this.clock = clock == null ? Clock.systemUTC() : clock;

        this.sessionHigh = openPrice;
        this.sessionLow = openPrice;
        this.lastPrice = openPrice;
        this.lastUpdated = this.clock.instant();
    }

    public InstrumentState(String symbol, String exchangeCode, BigDecimal openPrice, double volatility) {
        this(symbol, exchangeCode, openPrice, volatility, Clock.systemUTC());
    }

    public synchronized void updateWithTick(BigDecimal newPrice, long volumeDelta) {
        Objects.requireNonNull(newPrice, "newPrice must not be null");
        this.lastPrice = newPrice;
        if (newPrice.compareTo(sessionHigh) > 0) {
            this.sessionHigh = newPrice;
        }
        if (newPrice.compareTo(sessionLow) < 0) {
            this.sessionLow = newPrice;
        }
        if (volumeDelta > 0) {
            this.cumulativeVolume += volumeDelta;
        }
        this.lastUpdated = clock.instant();
    }

    public String getSymbol() {
        return symbol;
    }

    public String getExchangeCode() {
        return exchangeCode;
    }

    public double getVolatility() {
        return volatility;
    }

    public BigDecimal getSessionOpen() {
        return sessionOpen;
    }

    public synchronized BigDecimal getSessionHigh() {
        return sessionHigh;
    }

    public synchronized BigDecimal getSessionLow() {
        return sessionLow;
    }

    public synchronized BigDecimal getLastPrice() {
        return lastPrice;
    }

    public synchronized long getCumulativeVolume() {
        return cumulativeVolume;
    }

    public synchronized Instant getLastUpdated() {
        return lastUpdated;
    }

    public synchronized BigDecimal getChange() {
        return lastPrice.subtract(sessionOpen, CHANGE_MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP);
    }

    public synchronized BigDecimal getChangePercent() {
        if (sessionOpen.signum() == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal percent = getChange().divide(sessionOpen, CHANGE_MATH_CONTEXT).multiply(BigDecimal.valueOf(100), CHANGE_MATH_CONTEXT);
        return percent.setScale(2, RoundingMode.HALF_UP);
    }

    public synchronized Instant getLastUpdatedMinuteBucket() {
        return lastUpdated.truncatedTo(ChronoUnit.MINUTES);
    }
}
