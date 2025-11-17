package com.rnexchange.service.broker;

import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.service.api.dto.TraderDetails;
import com.rnexchange.service.api.dto.TraderPage;
import com.rnexchange.service.api.dto.TraderSummary;
import com.rnexchange.service.dto.BrokerTraderSummaryDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BrokerTradersService {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerTradersService.class);

    private final BrokerScopeService brokerScopeService;
    private final TraderProfileRepository traderProfileRepository;
    private final TradingAccountRepository tradingAccountRepository;

    public BrokerTradersService(
        BrokerScopeService brokerScopeService,
        TraderProfileRepository traderProfileRepository,
        TradingAccountRepository tradingAccountRepository
    ) {
        this.brokerScopeService = brokerScopeService;
        this.traderProfileRepository = traderProfileRepository;
        this.tradingAccountRepository = tradingAccountRepository;
    }

    public Map<String, Object> listTraders(Long brokerId, int page, int size) {
        brokerScopeService.assertBrokerAdmin();
        Long scopedBrokerId = brokerScopeService.requireBrokerId(brokerId);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

        LOG.debug("Fetching traders for brokerId: {}, page: {}, size: {}", scopedBrokerId, page, size);
        Page<TraderProfile> traderPage = traderProfileRepository.findByBrokerId(scopedBrokerId, pageable);
        LOG.debug("Found {} trader profiles", traderPage.getTotalElements());

        // Fetch all trading accounts for these traders in one query to avoid N+1 problem
        List<Long> traderProfileIds = traderPage.getContent().stream().map(TraderProfile::getId).collect(Collectors.toList());

        // Create a map of traderId -> first TradingAccount for efficient lookup
        Map<Long, TradingAccount> tradingAccountMap = new HashMap<>();
        if (!traderProfileIds.isEmpty()) {
            try {
                LOG.debug("Fetching trading accounts for {} trader profiles", traderProfileIds.size());
                List<TradingAccount> allTradingAccounts = tradingAccountRepository.findByBrokerIdAndTraderIdIn(
                    scopedBrokerId,
                    traderProfileIds
                );
                LOG.debug("Found {} trading accounts", allTradingAccounts.size());
                // Group by trader ID, taking the first account for each trader
                for (TradingAccount account : allTradingAccounts) {
                    if (account.getTrader() != null) {
                        Long traderId = account.getTrader().getId();
                        if (traderId != null) {
                            tradingAccountMap.putIfAbsent(traderId, account);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Error fetching trading accounts for broker {}: {}", scopedBrokerId, e.getMessage(), e);
                // Continue with empty map - traders will show with zero balance
            }
        }

        List<Map<String, Object>> summaries = new ArrayList<>();
        for (TraderProfile traderProfile : traderPage.getContent()) {
            Map<String, Object> summary = new HashMap<>();

            // Get trading account from map (O(1) lookup)
            TradingAccount tradingAccount = tradingAccountMap.get(traderProfile.getId());

            // Set trader ID (use trading account ID as traderId for frontend compatibility)
            Long tradingAccountId = tradingAccount != null ? tradingAccount.getId() : null;
            summary.put("traderId", tradingAccountId != null ? String.valueOf(tradingAccountId) : null);

            // Set display name
            summary.put("name", traderProfile.getDisplayName());

            // Set login from user
            String login = traderProfile.getUser() != null ? traderProfile.getUser().getLogin() : null;
            summary.put("login", login);

            // Set status (convert AccountStatus to 'active' | 'disabled')
            String status = traderProfile.getStatus() != null && traderProfile.getStatus().name().equals("ACTIVE") ? "active" : "disabled";
            summary.put("status", status);

            // Set cash balance from trading account
            BigDecimal cash = tradingAccount != null ? tradingAccount.getBalance() : BigDecimal.ZERO;
            summary.put("cash", cash);

            // Set current P&L (placeholder - would need position calculation)
            summary.put("currentPnl", BigDecimal.ZERO);

            summaries.add(summary);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", summaries);
        result.put("page", traderPage.getNumber());
        result.put("size", traderPage.getSize());
        result.put("totalElements", traderPage.getTotalElements());
        return result;
    }

    public Map<String, Object> getTraderDetails(Long brokerId, Long traderId) {
        brokerScopeService.assertBrokerAdmin();
        brokerScopeService.requireBrokerId(brokerId);

        Map<String, Object> result = new HashMap<>();
        result.put("summary", new HashMap<String, Object>());
        result.put("recentLedger", List.of());
        return result;
    }

    public TraderPage listTradersApi(int page, int size) {
        brokerScopeService.assertBrokerAdmin();
        // In absence of request-scoped broker id in contract, use a default broker context
        long brokerId = 1L;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<?> traderPage = traderProfileRepository.findByBrokerId(brokerId, pageable);

        TraderPage response = new TraderPage();
        response.setPage(traderPage.getNumber());
        response.setSize(traderPage.getSize());
        response.setTotalElements((int) traderPage.getTotalElements());
        response.setContent(new ArrayList<TraderSummary>());
        return response;
    }

    public TraderDetails getTraderDetailsApi(UUID traderId) {
        brokerScopeService.assertBrokerAdmin();
        TraderDetails details = new TraderDetails();
        details.setSummary(new TraderSummary());
        details.setRecentLedger(new ArrayList<>());
        return details;
    }
}
