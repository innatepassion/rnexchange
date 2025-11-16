package com.rnexchange.service.broker;

import com.rnexchange.domain.Broker;
import com.rnexchange.domain.Position;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.repository.PositionRepository;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.service.dto.BrokerOverviewDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Phase 3: Computes broker overview metrics.
 *
 * - activeTraderCount
 * - totalCash
 * - totalEquityExposure
 *
 * Notes:
 * - Utilization calculation details are applied in Phase 3 services that require ranking; here we aggregate exposure.
 * - Price staleness exclusion depends on last price timestamps; if unavailable, we treat all as fresh.
 */
@Service
@Transactional(readOnly = true)
public class BrokerOverviewService {

    private static final BigDecimal EPSILON = BigDecimal.valueOf(1.0d);

    private final BrokerRepository brokerRepository;
    private final TraderProfileRepository traderProfileRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final PositionRepository positionRepository;

    public BrokerOverviewService(
        BrokerRepository brokerRepository,
        TraderProfileRepository traderProfileRepository,
        TradingAccountRepository tradingAccountRepository,
        PositionRepository positionRepository
    ) {
        this.brokerRepository = brokerRepository;
        this.traderProfileRepository = traderProfileRepository;
        this.tradingAccountRepository = tradingAccountRepository;
        this.positionRepository = positionRepository;
    }

    public BrokerOverviewDTO computeOverview(Long brokerId) {
        Broker broker = brokerRepository
            .findById(brokerId)
            .orElseThrow(() -> new IllegalArgumentException("Broker not found: " + brokerId));

        // Count distinct traders under the broker (via TradingAccount join)
        Page<?> tradersPage = traderProfileRepository.findByBrokerId(brokerId, PageRequest.of(0, 1));
        long activeTraderCount = tradersPage.getTotalElements();

        // Sum cash balances across trading accounts
        Page<TradingAccount> accountsPage = tradingAccountRepository.findByBroker_Id(brokerId, Pageable.unpaged());
        BigDecimal totalCash = accountsPage
            .getContent()
            .stream()
            .map(TradingAccount::getBalance)
            .filter(b -> b != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sum absolute exposure across positions
        List<Position> positions = positionRepository.findByBrokerNonPaginated(broker);
        BigDecimal totalEquityExposure = positions
            .stream()
            .map(p -> {
                if (p.getQty() == null || p.getLastPx() == null) {
                    return BigDecimal.ZERO;
                }
                BigDecimal notional = p.getQty().abs().multiply(p.getLastPx().abs());
                return notional;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BrokerOverviewDTO dto = new BrokerOverviewDTO();
        dto.setActiveTraderCount(activeTraderCount);
        dto.setTotalCash(totalCash);
        dto.setTotalEquityExposure(totalEquityExposure);
        dto.setGeneratedAt(Instant.now());
        return dto;
    }
}
