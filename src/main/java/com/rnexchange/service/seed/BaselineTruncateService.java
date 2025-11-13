package com.rnexchange.service.seed;

import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.repository.ContractRepository;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.repository.MarginRuleRepository;
import com.rnexchange.repository.MarketHolidayRepository;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.repository.TradingAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaselineTruncateService {

    private final ContractRepository contractRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final MarginRuleRepository marginRuleRepository;
    private final MarketHolidayRepository marketHolidayRepository;
    private final InstrumentRepository instrumentRepository;
    private final BrokerDeskRepository brokerDeskRepository;
    private final TraderProfileRepository traderProfileRepository;
    private final BrokerRepository brokerRepository;
    private final ExchangeRepository exchangeRepository;

    public BaselineTruncateService(
        ContractRepository contractRepository,
        TradingAccountRepository tradingAccountRepository,
        MarginRuleRepository marginRuleRepository,
        MarketHolidayRepository marketHolidayRepository,
        InstrumentRepository instrumentRepository,
        BrokerDeskRepository brokerDeskRepository,
        TraderProfileRepository traderProfileRepository,
        BrokerRepository brokerRepository,
        ExchangeRepository exchangeRepository
    ) {
        this.contractRepository = contractRepository;
        this.tradingAccountRepository = tradingAccountRepository;
        this.marginRuleRepository = marginRuleRepository;
        this.marketHolidayRepository = marketHolidayRepository;
        this.instrumentRepository = instrumentRepository;
        this.brokerDeskRepository = brokerDeskRepository;
        this.traderProfileRepository = traderProfileRepository;
        this.brokerRepository = brokerRepository;
        this.exchangeRepository = exchangeRepository;
    }

    @Transactional
    public void cleanup() {
        contractRepository.deleteAllInBatch();
        tradingAccountRepository.deleteAllInBatch();
        marginRuleRepository.deleteAllInBatch();
        marketHolidayRepository.deleteAllInBatch();
        instrumentRepository.deleteAllInBatch();
        brokerDeskRepository.deleteAllInBatch();
        traderProfileRepository.deleteAllInBatch();
        brokerRepository.deleteAllInBatch();
        exchangeRepository.deleteAllInBatch();
    }
}
