package com.rnexchange.service.seed;

import com.rnexchange.domain.Authority;
import com.rnexchange.domain.User;
import com.rnexchange.repository.AuthorityRepository;
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
import com.rnexchange.security.AuthoritiesConstants;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

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
    protected ExchangeRepository exchangeRepository;

    @Autowired
    protected InstrumentRepository instrumentRepository;

    @Autowired
    protected ContractRepository contractRepository;

    @Autowired
    protected MarketHolidayRepository marketHolidayRepository;

    @Autowired
    protected MarginRuleRepository marginRuleRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetBaselineData() {
        clearDomainData();
        seedRequiredAuthorities();
        seedRequiredUsers();
    }

    protected void clearDomainData() {
        contractRepository.deleteAllInBatch();
        tradingAccountRepository.deleteAllInBatch();
        marginRuleRepository.deleteAllInBatch();
        marketHolidayRepository.deleteAllInBatch();
        instrumentRepository.deleteAllInBatch();
        brokerDeskRepository.deleteAllInBatch();
        traderProfileRepository.deleteAllInBatch();
        brokerRepository.deleteAllInBatch();
        exchangeRepository.deleteAllInBatch();
        jdbcTemplate.execute("DELETE FROM jhi_user_authority");
        userRepository.deleteAll();
        userRepository.flush();
        authorityRepository.deleteAll();
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
