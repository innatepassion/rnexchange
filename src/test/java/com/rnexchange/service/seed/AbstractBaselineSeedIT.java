package com.rnexchange.service.seed;

import com.rnexchange.domain.Authority;
import com.rnexchange.domain.User;
import com.rnexchange.repository.AuthorityRepository;
import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.repository.ContractRepository;
import com.rnexchange.repository.DailySettlementPriceRepository;
import com.rnexchange.repository.ExchangeOperatorRepository;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.repository.ExecutionRepository;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.repository.LedgerEntryRepository;
import com.rnexchange.repository.LotRepository;
import com.rnexchange.repository.MarginRuleRepository;
import com.rnexchange.repository.MarketHolidayRepository;
import com.rnexchange.repository.OrderRepository;
import com.rnexchange.repository.PositionRepository;
import com.rnexchange.repository.ReportLinkRepository;
import com.rnexchange.repository.SettlementBatchRepository;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.repository.UserRepository;
import com.rnexchange.repository.WatchlistItemRepository;
import com.rnexchange.repository.WatchlistRepository;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.seed.BaselineSeedService;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractBaselineSeedIT {

    protected static final String DEFAULT_PASSWORD_HASH = "$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC";
    protected static final String DEFAULT_LANG = "en";

    @Autowired
    protected AuthorityRepository authorityRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected BrokerRepository brokerRepository;

    @Autowired
    protected BrokerDeskRepository brokerDeskRepository;

    @Autowired
    protected TraderProfileRepository traderProfileRepository;

    @Autowired
    protected TradingAccountRepository tradingAccountRepository;

    @Autowired
    protected ReportLinkRepository reportLinkRepository;

    @Autowired
    protected SettlementBatchRepository settlementBatchRepository;

    @Autowired
    protected LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    protected PositionRepository positionRepository;

    @Autowired
    protected LotRepository lotRepository;

    @Autowired
    protected ExecutionRepository executionRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected ExchangeRepository exchangeRepository;

    @Autowired
    protected ExchangeOperatorRepository exchangeOperatorRepository;

    @Autowired
    protected InstrumentRepository instrumentRepository;

    @Autowired
    protected ContractRepository contractRepository;

    @Autowired
    protected MarketHolidayRepository marketHolidayRepository;

    @Autowired
    protected WatchlistRepository watchlistRepository;

    @Autowired
    protected WatchlistItemRepository watchlistItemRepository;

    @Autowired
    protected MarginRuleRepository marginRuleRepository;

    @Autowired
    protected DailySettlementPriceRepository dailySettlementPriceRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected BaselineSeedService baselineSeedService;

    @BeforeEach
    void resetBaselineData() {
        clearDomainData();
        seedRequiredAuthorities();
        seedRequiredUsers();
    }

    @AfterEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void restoreDemoDataset() {
        clearDomainData();
        seedRequiredAuthorities();
        seedRequiredUsers();
    }

    protected void clearDomainData() {
        watchlistItemRepository.deleteAllInBatch();
        watchlistRepository.deleteAllInBatch();
        reportLinkRepository.deleteAllInBatch();
        executionRepository.deleteAllInBatch();
        lotRepository.deleteAllInBatch();
        positionRepository.deleteAllInBatch();
        ledgerEntryRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        tradingAccountRepository.deleteAllInBatch();
        marginRuleRepository.deleteAllInBatch();
        marketHolidayRepository.deleteAllInBatch();
        dailySettlementPriceRepository.deleteAllInBatch();
        contractRepository.deleteAllInBatch();
        settlementBatchRepository.deleteAllInBatch();
        instrumentRepository.deleteAllInBatch();
        brokerDeskRepository.deleteAllInBatch();
        traderProfileRepository.deleteAllInBatch();
        exchangeOperatorRepository.deleteAllInBatch();
        brokerRepository.deleteAllInBatch();
        exchangeRepository.deleteAllInBatch();
    }

    private void seedRequiredAuthorities() {
        createAuthorityIfMissing(AuthoritiesConstants.EXCHANGE_OPERATOR);
        createAuthorityIfMissing(AuthoritiesConstants.BROKER_ADMIN);
        createAuthorityIfMissing(AuthoritiesConstants.TRADER);
        createAuthorityIfMissing(AuthoritiesConstants.ADMIN);
        createAuthorityIfMissing(AuthoritiesConstants.USER);
    }

    private void seedRequiredUsers() {
        createUserIfMissing(
            "exchange-operator",
            "Exchange",
            "Operator",
            "exchange.operator@rnexchange.test",
            AuthoritiesConstants.EXCHANGE_OPERATOR
        );
        createUserIfMissing("broker-admin", "Broker", "Admin", "broker.admin@rnexchange.test", AuthoritiesConstants.BROKER_ADMIN);
        createUserIfMissing("trader-one", "Trader", "One", "trader.one@rnexchange.test", AuthoritiesConstants.TRADER);
        createUserIfMissing("trader-two", "Trader", "Two", "trader.two@rnexchange.test", AuthoritiesConstants.TRADER);
    }

    private void createAuthorityIfMissing(String authorityName) {
        if (authorityRepository.existsById(authorityName)) {
            return;
        }
        Authority authority = new Authority().name(authorityName);
        authorityRepository.save(authority);
    }

    private void createUserIfMissing(String login, String firstName, String lastName, String email, String authorityName) {
        if (userRepository.findOneByLogin(login).isPresent()) {
            return;
        }
        Authority authority = authorityRepository
            .findById(authorityName)
            .orElseThrow(() -> new IllegalStateException("Missing authority " + authorityName));
        User user = new User();
        user.setLogin(login);
        user.setPassword(DEFAULT_PASSWORD_HASH);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setActivated(true);
        user.setLangKey(DEFAULT_LANG);
        user.setCreatedBy(AuthoritiesConstants.ADMIN);
        user.setCreatedDate(Instant.now());
        user.setAuthorities(Set.of(authority));
        userRepository.save(user);
    }
}
