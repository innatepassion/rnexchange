package com.rnexchange.config;

import com.rnexchange.domain.Authority;
import com.rnexchange.domain.User;
import com.rnexchange.repository.AuthorityRepository;
import com.rnexchange.repository.UserRepository;
import com.rnexchange.security.AuthoritiesConstants;
import java.time.Instant;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds the deterministic baseline dataset once per integration-test application context without invoking
 * the destructive cleanup routines used by the production BaselineSeedService.
 */
@Configuration
public class IntegrationTestDataConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestDataConfiguration.class);

    private static final String DEFAULT_PASSWORD_HASH = "$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC";
    private static final String DEFAULT_LANG = "en";

    @Bean
    CommandLineRunner integrationTestBaselineSeeder(AuthorityRepository authorityRepository, UserRepository userRepository) {
        return args -> {
            LOG.info("Initializing deterministic authority/user dataset for integration tests");
            seedAuthority(authorityRepository, AuthoritiesConstants.EXCHANGE_OPERATOR);
            seedAuthority(authorityRepository, AuthoritiesConstants.BROKER_ADMIN);
            seedAuthority(authorityRepository, AuthoritiesConstants.TRADER);
            seedAuthority(authorityRepository, AuthoritiesConstants.ADMIN);
            seedAuthority(authorityRepository, AuthoritiesConstants.USER);

            seedUser(
                userRepository,
                authorityRepository,
                "exchange-operator",
                "Exchange",
                "Operator",
                "exchange.operator@rnexchange.test",
                AuthoritiesConstants.EXCHANGE_OPERATOR
            );
            seedUser(
                userRepository,
                authorityRepository,
                "broker-admin",
                "Broker",
                "Admin",
                "broker.admin@rnexchange.test",
                AuthoritiesConstants.BROKER_ADMIN
            );
            seedUser(
                userRepository,
                authorityRepository,
                "trader-one",
                "Trader",
                "One",
                "trader.one@rnexchange.test",
                AuthoritiesConstants.TRADER
            );
            seedUser(
                userRepository,
                authorityRepository,
                "trader-two",
                "Trader",
                "Two",
                "trader.two@rnexchange.test",
                AuthoritiesConstants.TRADER
            );
        };
    }

    private void seedAuthority(AuthorityRepository repository, String name) {
        if (repository.existsById(name)) {
            return;
        }
        repository.save(new Authority().name(name));
    }

    private void seedUser(
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        String login,
        String firstName,
        String lastName,
        String email,
        String authority
    ) {
        if (userRepository.findOneByLogin(login).isPresent()) {
            return;
        }
        Authority auth = authorityRepository.findById(authority).orElseThrow();
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
        user.setAuthorities(Set.of(auth));
        userRepository.save(user);
    }
}
