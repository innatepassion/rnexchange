package com.rnexchange.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "marketdata.mock")
public class MockMarketDataProperties {

    @Min(100)
    @Max(1000)
    private int intervalMs = 750;

    @Min(1)
    @Max(10)
    private int batchSize = 4;

    @Min(1)
    @Max(600)
    private int barIntervalSeconds = 60;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal minPrice = new BigDecimal("1.00");

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal maxPrice = new BigDecimal("10000.00");

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal defaultPrice = new BigDecimal("100.00");

    @NotNull
    @DecimalMin(value = "0.001")
    private BigDecimal volatilityBandPercent = new BigDecimal("0.05");

    @Valid
    private final VolatilityProperties volatility = new VolatilityProperties();

    public int getIntervalMs() {
        return intervalMs;
    }

    public void setIntervalMs(int intervalMs) {
        this.intervalMs = intervalMs;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBarIntervalSeconds() {
        return barIntervalSeconds;
    }

    public void setBarIntervalSeconds(int barIntervalSeconds) {
        this.barIntervalSeconds = barIntervalSeconds;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(BigDecimal defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public BigDecimal getVolatilityBandPercent() {
        return volatilityBandPercent;
    }

    public void setVolatilityBandPercent(BigDecimal volatilityBandPercent) {
        this.volatilityBandPercent = volatilityBandPercent;
    }

    public VolatilityProperties getVolatility() {
        return volatility;
    }

    public static class VolatilityProperties {

        private final Map<String, BigDecimal> exchange = new HashMap<>();
        private final Map<String, BigDecimal> assetClass = new HashMap<>();

        public Map<String, BigDecimal> getExchange() {
            return exchange;
        }

        public Map<String, BigDecimal> getAssetClass() {
            return assetClass;
        }
    }
}
