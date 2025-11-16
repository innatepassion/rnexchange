package com.rnexchange.service.broker;

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
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BrokerTradersService {

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

        Page<?> traderPage = traderProfileRepository.findByBrokerId(scopedBrokerId, pageable);

        List<BrokerTraderSummaryDTO> summaries = new ArrayList<>();
        traderPage
            .getContent()
            .forEach(tp -> {
                BrokerTraderSummaryDTO dto = new BrokerTraderSummaryDTO();
                dto.setTraderId(null);
                dto.setDisplayName(null);
                dto.setTradingAccountId(null);
                dto.setCashBalance(BigDecimal.ZERO);
                dto.setUnrealizedPnl(BigDecimal.ZERO);
                dto.setUtilizationPct(BigDecimal.ZERO);
                dto.setStalePriceFlag(false);
                summaries.add(dto);
            });

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
