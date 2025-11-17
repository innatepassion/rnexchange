package com.rnexchange.service.trading;

import com.rnexchange.domain.Position;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.PositionRepository;
import com.rnexchange.repository.TradingAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for calculating buying power for trading accounts.
 * M6 User Story 2 (T026): Incorporates funds journal entries and is reused consistently.
 *
 * <p>Buying power is calculated as:
 * <ul>
 *   <li>Cash balance (includes all journal entries: credits, debits)</li>
 *   <li>Plus: Unrealized P&L from open positions</li>
 *   <li>Minus: Margin requirements (if applicable)</li>
 * </ul>
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class BuyingPowerService {

    private static final Logger LOG = LoggerFactory.getLogger(BuyingPowerService.class);

    private final TradingAccountRepository tradingAccountRepository;
    private final PositionRepository positionRepository;

    public BuyingPowerService(TradingAccountRepository tradingAccountRepository, PositionRepository positionRepository) {
        this.tradingAccountRepository = tradingAccountRepository;
        this.positionRepository = positionRepository;
    }

    /**
     * Calculate buying power for a trading account.
     * M6 User Story 2: Incorporates funds journal entries via the account balance.
     *
     * @param tradingAccountId the trading account ID
     * @return the buying power amount
     */
    public BigDecimal calculateBuyingPower(Long tradingAccountId) {
        TradingAccount account = tradingAccountRepository
            .findById(tradingAccountId)
            .orElseThrow(() -> new IllegalArgumentException("TradingAccount not found: " + tradingAccountId));

        // Start with cash balance (which includes all journal entries)
        BigDecimal buyingPower = account.getBalance();

        // Add unrealized P&L from open positions
        List<Position> positions = positionRepository.findByTradingAccount(account);
        BigDecimal unrealizedPnl = positions
            .stream()
            .map(p -> p.getUnrealizedPnl() != null ? p.getUnrealizedPnl() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        buyingPower = buyingPower.add(unrealizedPnl);

        // TODO: Subtract margin requirements when margin rules are implemented
        // For now, buying power = cash + unrealized P&L

        LOG.debug(
            "Calculated buying power: accountId={}, cashBalance={}, unrealizedPnl={}, buyingPower={}",
            tradingAccountId,
            account.getBalance(),
            unrealizedPnl,
            buyingPower
        );

        return buyingPower;
    }

    /**
     * Calculate buying power for a trading account entity.
     *
     * @param account the trading account
     * @return the buying power amount
     */
    public BigDecimal calculateBuyingPower(TradingAccount account) {
        return calculateBuyingPower(account.getId());
    }
}
