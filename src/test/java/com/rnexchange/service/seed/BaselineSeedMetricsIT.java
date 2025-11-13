package com.rnexchange.service.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.IntegrationTest;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class BaselineSeedMetricsIT extends AbstractBaselineSeedIT {

    private static final long MAX_ALLOWED_DURATION_MS = TimeUnit.MINUTES.toMillis(5);

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @Transactional
    void baselineSeedRecordsDurationMetricUnderThreshold() {
        baselineSeedService.runBaselineSeedBlocking(BaselineSeedRequest.builder().invocationId(UUID.randomUUID()).build());

        Timer timer = meterRegistry.find("baseline.seed.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThan(0);
        assertThat(timer.max(TimeUnit.MILLISECONDS)).isLessThan(MAX_ALLOWED_DURATION_MS);
    }
}
