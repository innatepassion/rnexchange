package com.rnexchange.service.marketdata;

import com.rnexchange.config.MockMarketDataProperties;
import com.rnexchange.domain.ExchangeVolatilityOverride;
import com.rnexchange.repository.ExchangeVolatilityOverrideRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VolatilityConfigurationLoader {

    private static final Logger LOG = LoggerFactory.getLogger(VolatilityConfigurationLoader.class);

    private final ExchangeVolatilityOverrideRepository repository;
    private final MockMarketDataProperties properties;

    public VolatilityConfigurationLoader(ExchangeVolatilityOverrideRepository repository, MockMarketDataProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public VolatilityOverrides load() {
        Map<String, Map<String, BigDecimal>> dbOverrides = new HashMap<>();
        repository
            .findAll()
            .forEach(override ->
                dbOverrides
                    .computeIfAbsent(key(override.getExchangeCode()), key -> new HashMap<>())
                    .put(key(override.getAssetClass()), override.getVolatilityPct())
            );

        VolatilityOverrides overrides = new VolatilityOverrides(properties, dbOverrides);
        LOG.debug("Loaded {} volatility overrides from database", dbOverrides.values().stream().mapToInt(Map::size).sum());
        return overrides;
    }

    private static String key(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    public static final class VolatilityOverrides {

        private static final String ALL = "ALL";

        private final Map<String, Map<String, BigDecimal>> database;
        private final Map<String, BigDecimal> configExchange;
        private final Map<String, BigDecimal> configAssetClass;

        private VolatilityOverrides(MockMarketDataProperties properties, Map<String, Map<String, BigDecimal>> database) {
            this.database = copyNested(database);
            this.configExchange = normalize(properties.getVolatility().getExchange());
            this.configAssetClass = normalize(properties.getVolatility().getAssetClass());
        }

        public BigDecimal resolve(String exchangeCode, String assetClass, BigDecimal fallback) {
            Objects.requireNonNull(fallback, "fallback volatility must not be null");
            String exchangeKey = key(exchangeCode);
            String assetKey = key(assetClass);

            Map<String, BigDecimal> perAsset = database.get(exchangeKey);
            if (perAsset != null) {
                BigDecimal match = perAsset.get(assetKey);
                if (match != null) {
                    return match;
                }
                match = perAsset.get(ALL);
                if (match != null) {
                    return match;
                }
            }

            BigDecimal configMatch = configExchange.get(exchangeKey);
            if (configMatch != null) {
                return configMatch;
            }

            configMatch = configAssetClass.get(assetKey);
            if (configMatch != null) {
                return configMatch;
            }

            configMatch = configAssetClass.get(ALL);
            if (configMatch != null) {
                return configMatch;
            }

            return fallback;
        }

        public Map<String, Map<String, BigDecimal>> databaseOverrides() {
            return database;
        }

        public Map<String, BigDecimal> configuredExchangeDefaults() {
            return configExchange;
        }

        public Map<String, BigDecimal> configuredAssetClassDefaults() {
            return configAssetClass;
        }

        private static Map<String, BigDecimal> normalize(Map<String, BigDecimal> source) {
            Map<String, BigDecimal> normalized = new HashMap<>();
            source.forEach((key, value) -> normalized.put(key(key), value));
            return Collections.unmodifiableMap(normalized);
        }

        private static Map<String, Map<String, BigDecimal>> copyNested(Map<String, Map<String, BigDecimal>> source) {
            Map<String, Map<String, BigDecimal>> copy = new HashMap<>();
            source.forEach((exchange, assetMap) -> copy.put(exchange, Collections.unmodifiableMap(new HashMap<>(assetMap))));
            return Collections.unmodifiableMap(copy);
        }
    }
}
