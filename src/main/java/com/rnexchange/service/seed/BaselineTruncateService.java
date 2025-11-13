package com.rnexchange.service.seed;

import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.repository.ContractRepository;
import com.rnexchange.repository.CorporateActionRepository;
import com.rnexchange.repository.DailySettlementPriceRepository;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.repository.ExecutionRepository;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.repository.LedgerEntryRepository;
import com.rnexchange.repository.LotRepository;
import com.rnexchange.repository.MarginRuleRepository;
import com.rnexchange.repository.MarketHolidayRepository;
import com.rnexchange.repository.OrderRepository;
import com.rnexchange.repository.PositionRepository;
import com.rnexchange.repository.RiskAlertRepository;
import com.rnexchange.repository.SettlementBatchRepository;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.repository.TradingAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaselineTruncateService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final ExecutionRepository executionRepository;
    private final LotRepository lotRepository;
    private final PositionRepository positionRepository;
    private final OrderRepository orderRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final DailySettlementPriceRepository dailySettlementPriceRepository;
    private final ContractRepository contractRepository;
    private final CorporateActionRepository corporateActionRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final MarginRuleRepository marginRuleRepository;
    private final MarketHolidayRepository marketHolidayRepository;
    private final InstrumentRepository instrumentRepository;
    private final SettlementBatchRepository settlementBatchRepository;
    private final BrokerDeskRepository brokerDeskRepository;
    private final TraderProfileRepository traderProfileRepository;
    private final BrokerRepository brokerRepository;
    private final ExchangeRepository exchangeRepository;

    public BaselineTruncateService(
        LedgerEntryRepository ledgerEntryRepository,
        ExecutionRepository executionRepository,
        LotRepository lotRepository,
        PositionRepository positionRepository,
        OrderRepository orderRepository,
        RiskAlertRepository riskAlertRepository,
        DailySettlementPriceRepository dailySettlementPriceRepository,
        ContractRepository contractRepository,
        CorporateActionRepository corporateActionRepository,
        TradingAccountRepository tradingAccountRepository,
        MarginRuleRepository marginRuleRepository,
        MarketHolidayRepository marketHolidayRepository,
        InstrumentRepository instrumentRepository,
        SettlementBatchRepository settlementBatchRepository,
        BrokerDeskRepository brokerDeskRepository,
        TraderProfileRepository traderProfileRepository,
        BrokerRepository brokerRepository,
        ExchangeRepository exchangeRepository
    ) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.executionRepository = executionRepository;
        this.lotRepository = lotRepository;
        this.positionRepository = positionRepository;
        this.orderRepository = orderRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.dailySettlementPriceRepository = dailySettlementPriceRepository;
        this.contractRepository = contractRepository;
        this.corporateActionRepository = corporateActionRepository;
        this.tradingAccountRepository = tradingAccountRepository;
        this.marginRuleRepository = marginRuleRepository;
        this.marketHolidayRepository = marketHolidayRepository;
        this.instrumentRepository = instrumentRepository;
        this.settlementBatchRepository = settlementBatchRepository;
        this.brokerDeskRepository = brokerDeskRepository;
        this.traderProfileRepository = traderProfileRepository;
        this.brokerRepository = brokerRepository;
        this.exchangeRepository = exchangeRepository;
    }

    @Transactional
    public void cleanup() {
        ledgerEntryRepository.deleteAllInBatch();
        executionRepository.deleteAllInBatch();
        lotRepository.deleteAllInBatch();
        positionRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        riskAlertRepository.deleteAllInBatch();
        dailySettlementPriceRepository.deleteAllInBatch();
        contractRepository.deleteAllInBatch();
        corporateActionRepository.deleteAllInBatch();
        tradingAccountRepository.deleteAllInBatch();
        marginRuleRepository.deleteAllInBatch();
        marketHolidayRepository.deleteAllInBatch();
        brokerDeskRepository.deleteAllInBatch();
        traderProfileRepository.deleteAllInBatch();
        settlementBatchRepository.deleteAllInBatch();
        instrumentRepository.deleteAllInBatch();
        brokerRepository.deleteAllInBatch();
        exchangeRepository.deleteAllInBatch();
    }
}
