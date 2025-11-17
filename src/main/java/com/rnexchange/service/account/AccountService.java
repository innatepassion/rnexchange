package com.rnexchange.service.account;

import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.service.dto.AccountSummaryDTO;
import com.rnexchange.service.trading.BuyingPowerService;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for account-related operations, including account summaries with risk flags.
 * M6 User Story 2 (T028): Provides AccountSummaryDTO with negative/at-risk balance detection.
 */
@Service
@Transactional(readOnly = true)
public class AccountService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);

    private final TradingAccountRepository tradingAccountRepository;
    private final BuyingPowerService buyingPowerService;

    public AccountService(TradingAccountRepository tradingAccountRepository, BuyingPowerService buyingPowerService) {
        this.tradingAccountRepository = tradingAccountRepository;
        this.buyingPowerService = buyingPowerService;
    }

    /**
     * Get account summary for a trading account, including balance, buying power, and risk flags.
     * M6 User Story 2: Detects negative/at-risk balances and exposes flags in DTO.
     *
     * @param tradingAccountId the trading account ID
     * @return the account summary DTO
     */
    public AccountSummaryDTO getAccountSummary(Long tradingAccountId) {
        TradingAccount account = tradingAccountRepository
            .findById(tradingAccountId)
            .orElseThrow(() -> new IllegalArgumentException("TradingAccount not found: " + tradingAccountId));

        BigDecimal balance = account.getBalance();
        BigDecimal buyingPower = buyingPowerService.calculateBuyingPower(account);

        AccountSummaryDTO summary = new AccountSummaryDTO(tradingAccountId, balance, buyingPower);

        LOG.debug(
            "Account summary: accountId={}, balance={}, buyingPower={}, isNegative={}, isAtRisk={}",
            tradingAccountId,
            balance,
            buyingPower,
            summary.getIsNegative(),
            summary.getIsAtRisk()
        );

        return summary;
    }

    /**
     * Get account summary for a trading account entity.
     *
     * @param account the trading account
     * @return the account summary DTO
     */
    public AccountSummaryDTO getAccountSummary(TradingAccount account) {
        return getAccountSummary(account.getId());
    }
}
