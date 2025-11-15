package com.rnexchange.service.marketdata;

import com.rnexchange.config.MockMarketDataProperties;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockMarketDataConfigurationValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MockMarketDataConfigurationValidator.class);

    private final MockMarketDataProperties properties;

    public MockMarketDataConfigurationValidator(MockMarketDataProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void validateConfiguration() {
        BigDecimal min = properties.getMinPrice();
        BigDecimal max = properties.getMaxPrice();
        BigDecimal defaultPrice = properties.getDefaultPrice();

        if (min.compareTo(max) >= 0) {
            throw new IllegalStateException(
                "marketdata.mock.min-price must be strictly less than marketdata.mock.max-price (check application.yml)"
            );
        }

        if (defaultPrice.compareTo(min) < 0 || defaultPrice.compareTo(max) > 0) {
            throw new IllegalStateException("marketdata.mock.default-price must fall within the configured min/max price rails");
        }

        LOG.debug(
            "Mock market data configuration active: interval={} ms, batchSize={}, priceRange=[{}, {}]",
            properties.getIntervalMs(),
            properties.getBatchSize(),
            min,
            max
        );
    }
}
