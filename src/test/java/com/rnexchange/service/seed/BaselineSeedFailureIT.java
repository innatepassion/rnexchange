package com.rnexchange.service.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rnexchange.IntegrationTest;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class BaselineSeedFailureIT extends AbstractBaselineSeedIT {

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Test
    @Transactional
    void missingExchangeOperatorPreventsSeedAndLeavesDataIntact() {
        long instrumentsBefore = instrumentRepository.count();
        userRepository
            .findOneByLogin("exchange-operator")
            .ifPresent(user -> {
                userRepository.delete(user);
                userRepository.flush();
            });

        assertThatThrownBy(() ->
            baselineSeedService.runBaselineSeedBlocking(BaselineSeedRequest.builder().invocationId(UUID.randomUUID()).build())
        )
            .isInstanceOf(BaselineSeedPrerequisiteException.class)
            .hasMessageContaining("EXCHANGE_OPERATOR");

        assertThat(instrumentRepository.count()).isEqualTo(instrumentsBefore);
    }
}
