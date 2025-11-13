package com.rnexchange.service.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.IntegrationTest;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import com.rnexchange.service.seed.dto.BaselineSeedValidationReport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class BaselinePrerequisiteValidatorIT extends AbstractBaselineSeedIT {

    @Autowired
    private BaselinePrerequisiteValidator baselinePrerequisiteValidator;

    @Test
    @Transactional
    void validationPassesWhenPrerequisitesSatisfied() {
        BaselineSeedRequest request = BaselineSeedRequest.builder().invocationId(UUID.randomUUID()).build();

        BaselineSeedValidationReport report = baselinePrerequisiteValidator.validate(request);

        assertThat(report.isSuccessful()).isTrue();
        assertThat(report.getErrors()).isEmpty();
    }

    @Test
    @Transactional
    void validationFailsWhenExchangeOperatorMissing() {
        long instrumentsBefore = instrumentRepository.count();
        jdbcTemplate.execute("DELETE FROM jhi_user_authority");
        userRepository.deleteAll();
        userRepository.flush();

        BaselineSeedValidationReport report = baselinePrerequisiteValidator.validate(BaselineSeedRequest.builder().force(true).build());

        assertThat(report.isSuccessful()).isFalse();
        assertThat(report.getErrors()).anyMatch(message -> message.contains("EXCHANGE_OPERATOR"));
        assertThat(instrumentRepository.count()).isEqualTo(instrumentsBefore);
    }
}
