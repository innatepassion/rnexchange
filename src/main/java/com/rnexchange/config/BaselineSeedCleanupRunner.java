package com.rnexchange.config;

import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.seed.BaselineSeedStructuredLogger;
import com.rnexchange.service.seed.BaselineTruncateService;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BaselineSeedCleanupRunner {

    private static final Logger log = LoggerFactory.getLogger(BaselineSeedCleanupRunner.class);

    private final BaselineTruncateService truncateService;
    private final BaselineSeedStructuredLogger structuredLogger;

    public BaselineSeedCleanupRunner(BaselineTruncateService truncateService, BaselineSeedStructuredLogger structuredLogger) {
        this.truncateService = truncateService;
        this.structuredLogger = structuredLogger;
    }

    public void runCleanup(BaselineSeedRequest request) {
        long start = System.currentTimeMillis();
        try {
            truncateService.cleanup();
            long duration = System.currentTimeMillis() - start;
            log.info(
                structuredLogger.build(
                    "cleanup",
                    "database",
                    "SUCCESS",
                    duration,
                    "",
                    request.getInvocationId(),
                    AuthoritiesConstants.EXCHANGE_OPERATOR,
                    "N/A",
                    "CLEANED"
                )
            );
        } catch (RuntimeException ex) {
            long duration = System.currentTimeMillis() - start;
            log.error(
                structuredLogger.build(
                    "cleanup",
                    "database",
                    "FAILED",
                    duration,
                    ex.getMessage(),
                    request.getInvocationId(),
                    AuthoritiesConstants.EXCHANGE_OPERATOR,
                    "N/A",
                    "FAILED"
                ),
                ex
            );
            throw ex;
        }
    }
}
