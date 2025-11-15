package com.rnexchange.service.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.User;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class BaselineAccessIT extends AbstractBaselineSeedIT {

    private static final String EXCHANGE_OPERATOR_LOGIN = "exchange-operator";
    private static final String BROKER_ADMIN_LOGIN = "broker-admin";
    private static final String TRADER_ONE_LOGIN = "trader-one";
    private static final String TRADER_TWO_LOGIN = "trader-two";

    @Autowired
    private BaselineSeedService baselineSeedService;

    @BeforeEach
    void seedBaseline() {
        baselineSeedService.runBaselineSeedBlocking(BaselineSeedRequest.builder().invocationId(UUID.randomUUID()).build());
    }

    @Test
    @Transactional
    void seededUsersHaveExpectedAuthorities() {
        assertUserHasAuthority(EXCHANGE_OPERATOR_LOGIN, AuthoritiesConstants.EXCHANGE_OPERATOR);
        assertUserHasAuthority(BROKER_ADMIN_LOGIN, AuthoritiesConstants.BROKER_ADMIN);
        assertUserHasAuthority(TRADER_ONE_LOGIN, AuthoritiesConstants.TRADER);
        assertUserHasAuthority(TRADER_TWO_LOGIN, AuthoritiesConstants.TRADER);
    }

    @Test
    @Transactional
    void traderProfilesMapToDistinctUsers() {
        Set<String> traderLogins = traderProfileRepository
            .findAll()
            .stream()
            .map(TraderProfile::getUser)
            .map(User::getLogin)
            .collect(java.util.stream.Collectors.toSet());

        assertThat(traderLogins).containsExactlyInAnyOrder(TRADER_ONE_LOGIN, TRADER_TWO_LOGIN);
    }

    @Test
    @Transactional
    void noLegacyDemoInstrumentsRemain() {
        assertThat(instrumentRepository.count()).isEqualTo(10);
        assertThat(instrumentRepository.findAll()).allSatisfy(instrument -> assertThat(instrument.getSymbol()).doesNotContain("REGION"));
    }

    private void assertUserHasAuthority(String login, String authority) {
        Optional<User> userOptional = userRepository.findOneWithAuthoritiesByLogin(login);
        assertThat(userOptional).isPresent();
        // Use orElseThrow instead of get() to comply with modernizer rules.
        User user = userOptional.orElseThrow();
        assertThat(user.isActivated()).isTrue();
        assertThat(user.getAuthorities()).extracting(com.rnexchange.domain.Authority::getName).contains(authority);
    }
}
