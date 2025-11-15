package com.rnexchange.service;

import com.rnexchange.domain.Instrument;
import java.math.BigDecimal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for matching orders against market prices.
 *
 * T010: Obtain latest price from mock market data and determine if order can be filled.
 *
 * Responsibilities:
 * - Get latest available price for instruments from mock data feed (M1)
 * - Determine execution price for MARKET and LIMIT orders
 * - Check if orders can be filled at current prices
 *
 * For M2, all orders are filled immediately ("self-matched") against the latest available price.
 * Complex order routing and queuing will be addressed in later phases.
 */
@Service
@Transactional(readOnly = true)
public class MatchingService {

    private static final Logger LOG = LoggerFactory.getLogger(MatchingService.class);

    private final DailySettlementPriceService dailySettlementPriceService;

    public MatchingService(DailySettlementPriceService dailySettlementPriceService) {
        this.dailySettlementPriceService = dailySettlementPriceService;
    }

    /**
     * Get the latest available price for an instrument.
     * Used by both MARKET and LIMIT orders to determine execution price.
     *
     * Flow:
     * 1. Check if instrument has a recent mock market price
     * 2. Return the price if available
     * 3. Return empty if no price is available (instrument inactive/new)
     *
     * @param instrument the instrument to get price for
     * @return Optional containing the latest price, or empty if no price available
     */
    public Optional<BigDecimal> getLatestPrice(Instrument instrument) {
        if (instrument == null || instrument.getId() == null) {
            LOG.warn("Cannot get price for null or invalid instrument");
            return Optional.empty();
        }

        LOG.debug("Getting latest price for instrument: {}", instrument.getSymbol());

        try {
            // TODO: Query DailySettlementPrice or mock market data feed
            // For now, return empty - will be integrated with M1 market data
            // Expected: fetch latest price from market data source, ordered by date DESC
            // Example:
            // Optional<DailySettlementPriceDTO> latestPrice = dailySettlementPriceService
            //     .findLatestByInstrument(instrument.getId());
            // return latestPrice.map(DailySettlementPriceDTO::getClosePrice);

            LOG.warn("Price lookup not yet integrated with market data feed for instrument: {}", instrument.getSymbol());
            return Optional.empty();
        } catch (Exception e) {
            LOG.error("Error fetching latest price for instrument: {}", instrument.getSymbol(), e);
            return Optional.empty();
        }
    }

    /**
     * Get execution price for a MARKET order.
     * Market orders are filled immediately at the latest available price.
     *
     * @param instrument the instrument being traded
     * @return the execution price for the market order
     */
    public Optional<BigDecimal> getMarketExecutionPrice(Instrument instrument) {
        return getLatestPrice(instrument);
    }

    /**
     * Get execution price for a LIMIT order, if it can be filled.
     * Limit orders are only filled if the market price satisfies the limit condition.
     *
     * For BUY LIMIT: fills if marketPrice <= limitPrice
     * For SELL LIMIT: fills if marketPrice >= limitPrice
     *
     * @param instrument the instrument being traded
     * @param limitPrice the limit price specified in the order
     * @param isBuyOrder true if this is a BUY order, false for SELL
     * @return Optional with execution price if order can be filled, empty otherwise
     */
    public Optional<BigDecimal> getLimitExecutionPrice(Instrument instrument, BigDecimal limitPrice, boolean isBuyOrder) {
        Optional<BigDecimal> marketPrice = getLatestPrice(instrument);

        if (marketPrice.isEmpty()) {
            LOG.debug("No market price available for limit order on {}", instrument.getSymbol());
            return Optional.empty();
        }

        BigDecimal price = marketPrice.orElseThrow();

        if (isBuyOrder) {
            // BUY limit: fills if market price <= limit price
            if (price.compareTo(limitPrice) <= 0) {
                LOG.debug("BUY limit order on {} fills at {}, limit was {}", instrument.getSymbol(), price, limitPrice);
                return Optional.of(price);
            } else {
                LOG.debug("BUY limit order on {} cannot fill: market {} > limit {}", instrument.getSymbol(), price, limitPrice);
                return Optional.empty();
            }
        } else {
            // SELL limit: fills if market price >= limit price
            if (price.compareTo(limitPrice) >= 0) {
                LOG.debug("SELL limit order on {} fills at {}, limit was {}", instrument.getSymbol(), price, limitPrice);
                return Optional.of(price);
            } else {
                LOG.debug("SELL limit order on {} cannot fill: market {} < limit {}", instrument.getSymbol(), price, limitPrice);
                return Optional.empty();
            }
        }
    }

    /**
     * Check if an instrument is currently tradable (has recent price data and is active).
     *
     * @param instrument the instrument to check
     * @return true if instrument is tradable, false otherwise
     */
    public boolean isInstrumentTradable(Instrument instrument) {
        if (instrument == null || !"ACTIVE".equals(instrument.getStatus())) {
            return false;
        }

        // Check if latest price is available
        return getLatestPrice(instrument).isPresent();
    }

    /**
     * Get the bid-ask spread estimate for an instrument (placeholder for future).
     * For M2, we assume no spread (single price for both buy and sell).
     *
     * @param instrument the instrument
     * @return Optional containing spread estimate
     */
    public Optional<BigDecimal> getBidAskSpread(Instrument instrument) {
        // TODO: Implement in future phases when bid-ask data is available
        // For now, return zero spread (no difference between buy and sell prices)
        return Optional.of(BigDecimal.ZERO);
    }
}
