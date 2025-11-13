package com.rnexchange.service.seed;

import com.rnexchange.domain.Broker;
import com.rnexchange.domain.BrokerDesk;
import com.rnexchange.domain.Contract;
import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.MarginRule;
import com.rnexchange.domain.MarketHoliday;
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.User;
import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.AccountType;
import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.ContractType;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.domain.enumeration.ExchangeStatus;
import com.rnexchange.domain.enumeration.KycStatus;
import com.rnexchange.domain.enumeration.OptionType;
import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.repository.ContractRepository;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.repository.MarginRuleRepository;
import com.rnexchange.repository.MarketHolidayRepository;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.repository.UserRepository;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaselineSeedDataLoader {

    private final ExchangeRepository exchangeRepository;
    private final InstrumentRepository instrumentRepository;
    private final BrokerRepository brokerRepository;
    private final BrokerDeskRepository brokerDeskRepository;
    private final TraderProfileRepository traderProfileRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final MarketHolidayRepository marketHolidayRepository;
    private final MarginRuleRepository marginRuleRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    public BaselineSeedDataLoader(
        ExchangeRepository exchangeRepository,
        InstrumentRepository instrumentRepository,
        BrokerRepository brokerRepository,
        BrokerDeskRepository brokerDeskRepository,
        TraderProfileRepository traderProfileRepository,
        TradingAccountRepository tradingAccountRepository,
        MarketHolidayRepository marketHolidayRepository,
        MarginRuleRepository marginRuleRepository,
        ContractRepository contractRepository,
        UserRepository userRepository
    ) {
        this.exchangeRepository = exchangeRepository;
        this.instrumentRepository = instrumentRepository;
        this.brokerRepository = brokerRepository;
        this.brokerDeskRepository = brokerDeskRepository;
        this.traderProfileRepository = traderProfileRepository;
        this.tradingAccountRepository = tradingAccountRepository;
        this.marketHolidayRepository = marketHolidayRepository;
        this.marginRuleRepository = marginRuleRepository;
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, String> seed(BaselineSeedRequest request) {
        Exchange nse = saveExchange("NSE", "National Stock Exchange", ExchangeStatus.ACTIVE);
        Exchange bse = saveExchange("BSE", "Bombay Stock Exchange", ExchangeStatus.ACTIVE);
        Exchange mcx = saveExchange("MCX", "Multi Commodity Exchange", ExchangeStatus.ACTIVE);

        Map<String, Instrument> instruments = new LinkedHashMap<>();
        instruments.put(
            "NSE:RELIANCE",
            saveInstrument(nse, "RELIANCE", "Reliance Industries", AssetClass.EQUITY, new BigDecimal("0.05"), 1L)
        );
        instruments.put("NSE:HDFCBANK", saveInstrument(nse, "HDFCBANK", "HDFC Bank", AssetClass.EQUITY, new BigDecimal("0.05"), 1L));
        instruments.put("NSE:INFY", saveInstrument(nse, "INFY", "Infosys", AssetClass.EQUITY, new BigDecimal("0.05"), 1L));
        instruments.put("NSE:TCS", saveInstrument(nse, "TCS", "Tata Consultancy Services", AssetClass.EQUITY, new BigDecimal("0.05"), 1L));
        instruments.put("NSE:NIFTY50", saveInstrument(nse, "NIFTY50", "NIFTY 50 Index", AssetClass.EQUITY, new BigDecimal("0.05"), 1L));
        instruments.put(
            "BSE:RELIANCE_BSE",
            saveInstrument(bse, "RELIANCE_BSE", "Reliance Industries BSE", AssetClass.EQUITY, new BigDecimal("0.05"), 1L)
        );
        instruments.put(
            "BSE:SBIN_BSE",
            saveInstrument(bse, "SBIN_BSE", "State Bank of India BSE", AssetClass.EQUITY, new BigDecimal("0.05"), 1L)
        );
        instruments.put("MCX:GOLD", saveInstrument(mcx, "GOLD", "Gold Futures", AssetClass.COMMODITY, new BigDecimal("1.00"), 1L));
        instruments.put(
            "MCX:CRUDEOIL",
            saveInstrument(mcx, "CRUDEOIL", "Crude Oil Futures", AssetClass.COMMODITY, new BigDecimal("1.00"), 10L)
        );
        instruments.put("MCX:SILVER", saveInstrument(mcx, "SILVER", "Silver Futures", AssetClass.COMMODITY, new BigDecimal("1.00"), 1L));

        saveContract(instruments.get("NSE:NIFTY50"), ContractType.FUTURE, LocalDate.parse("2025-12-26"), null, null, "NSE_FNO");
        saveContract(
            instruments.get("NSE:NIFTY50"),
            ContractType.OPTION,
            LocalDate.parse("2025-12-26"),
            new BigDecimal("23500"),
            OptionType.CE,
            "NSE_FNO"
        );
        saveContract(
            instruments.get("NSE:NIFTY50"),
            ContractType.OPTION,
            LocalDate.parse("2025-12-26"),
            new BigDecimal("23500"),
            OptionType.PE,
            "NSE_FNO"
        );
        saveContract(instruments.get("NSE:TCS"), ContractType.FUTURE, LocalDate.parse("2025-12-26"), null, null, "NSE_FNO");
        saveContract(instruments.get("NSE:RELIANCE"), ContractType.FUTURE, LocalDate.parse("2025-12-26"), null, null, "NSE_FNO");

        saveHoliday(nse, LocalDate.parse("2025-12-31"), "Year End Trading Holiday");
        saveHoliday(bse, LocalDate.parse("2026-01-26"), "Republic Day Holiday");
        saveHoliday(mcx, LocalDate.parse("2025-12-12"), "Commodity Operations Maintenance");

        saveMarginRule(nse, "NSE_CASH", new BigDecimal("0.20"), new BigDecimal("0.15"));
        saveMarginRule(nse, "NSE_FNO", new BigDecimal("0.40"), new BigDecimal("0.30"));

        Broker broker = new Broker()
            .code("RN_DEMO")
            .name("RN DEMO BROKING")
            .status("ACTIVE")
            .createdDate(Instant.parse("2025-01-01T00:00:00Z"))
            .exchange(nse);
        broker = brokerRepository.save(broker);

        BrokerDesk brokerDesk = new BrokerDesk().name("RN DEMO BROKING Desk").broker(broker);
        User brokerUser = userRepository
            .findOneWithAuthoritiesByLogin("broker-admin")
            .orElseThrow(() -> new IllegalStateException("Missing broker-admin user"));
        brokerDesk.user(brokerUser);
        brokerDeskRepository.save(brokerDesk);

        TraderProfile traderOne = saveTraderProfile("trader-one", "Trader One", "trader.one@rnexchange.test");
        TraderProfile traderTwo = saveTraderProfile("trader-two", "Trader Two", "trader.two@rnexchange.test");

        saveTradingAccount(traderOne, broker);
        saveTradingAccount(traderTwo, broker);

        Map<String, String> metrics = new LinkedHashMap<>();
        metrics.put("exchangeCount", String.valueOf(exchangeRepository.count()));
        metrics.put("instrumentCount", String.valueOf(instrumentRepository.count()));
        metrics.put("contractCount", String.valueOf(contractRepository.count()));
        metrics.put("traderCount", String.valueOf(traderProfileRepository.count()));
        metrics.put("accountCount", String.valueOf(tradingAccountRepository.count()));
        return metrics;
    }

    private Exchange saveExchange(String code, String name, ExchangeStatus status) {
        Exchange exchange = new Exchange().code(code).name(name).timezone("Asia/Kolkata").status(status);
        return exchangeRepository.save(exchange);
    }

    private Instrument saveInstrument(
        Exchange exchange,
        String symbol,
        String name,
        AssetClass assetClass,
        BigDecimal tickSize,
        long lotSize
    ) {
        Instrument instrument = new Instrument()
            .symbol(symbol)
            .name(name)
            .assetClass(assetClass)
            .exchangeCode(exchange.getCode())
            .tickSize(tickSize)
            .lotSize(lotSize)
            .currency(Currency.INR)
            .status("ACTIVE")
            .exchange(exchange);
        return instrumentRepository.save(instrument);
    }

    private void saveContract(
        Instrument instrument,
        ContractType type,
        LocalDate expiry,
        BigDecimal strike,
        OptionType optionType,
        String segment
    ) {
        Contract contract = new Contract()
            .instrument(instrument)
            .instrumentSymbol(instrument.getSymbol())
            .contractType(type)
            .expiry(expiry)
            .strike(strike)
            .optionType(optionType)
            .segment(segment);
        contractRepository.save(contract);
    }

    private void saveHoliday(Exchange exchange, LocalDate date, String description) {
        MarketHoliday holiday = new MarketHoliday().exchange(exchange).tradeDate(date).reason(description).isHoliday(true);
        marketHolidayRepository.save(holiday);
    }

    private void saveMarginRule(Exchange exchange, String scope, BigDecimal initial, BigDecimal maint) {
        MarginRule rule = new MarginRule().exchange(exchange).scope(scope).initialPct(initial).maintPct(maint);
        marginRuleRepository.save(rule);
    }

    private TraderProfile saveTraderProfile(String login, String displayName, String email) {
        User user = userRepository
            .findOneWithAuthoritiesByLogin(login)
            .orElseThrow(() -> new IllegalStateException("Missing user " + login));
        TraderProfile profile = new TraderProfile()
            .displayName(displayName)
            .email(email)
            .status(AccountStatus.ACTIVE)
            .kycStatus(KycStatus.APPROVED)
            .user(user);
        return traderProfileRepository.save(profile);
    }

    private void saveTradingAccount(TraderProfile traderProfile, Broker broker) {
        TradingAccount account = new TradingAccount()
            .trader(traderProfile)
            .broker(broker)
            .type(AccountType.CASH)
            .baseCcy(Currency.INR)
            .balance(new BigDecimal("1000000.00"))
            .status(AccountStatus.ACTIVE);
        tradingAccountRepository.save(account);
    }
}
