package com.rnexchange.service.startup;

import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.seed.BaselineSeedStructuredLogger;
import com.rnexchange.service.seed.BaselineSeedVerificationException;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BaselineValidationRunner {

    private static final Logger log = LoggerFactory.getLogger(BaselineValidationRunner.class);

    private final InstrumentRepository instrumentRepository;
    private final BaselineSeedStructuredLogger structuredLogger;

    public BaselineValidationRunner(InstrumentRepository instrumentRepository, BaselineSeedStructuredLogger structuredLogger) {
        this.instrumentRepository = instrumentRepository;
        this.structuredLogger = structuredLogger;
    }

    public void runPostSeedValidation(BaselineSeedRequest request) {
        long start = System.currentTimeMillis();
        try {
            long instrumentCount = instrumentRepository.count();
            if (instrumentCount == 0) {
                throw new BaselineSeedVerificationException("No instruments persisted after baseline seed");
            }
            boolean legacySymbolsPresent = instrumentRepository
                .findAll()
                .stream()
                .anyMatch(instrument -> instrument.getSymbol().toUpperCase().contains("REGION"));
            if (legacySymbolsPresent) {
                throw new BaselineSeedVerificationException("Legacy demo instrument detected after baseline seed");
            }
            long duration = System.currentTimeMillis() - start;
            log.info(
                structuredLogger.build(
                    "validation",
                    "dataset",
                    "SUCCESS",
                    duration,
                    "",
                    request.getInvocationId(),
                    AuthoritiesConstants.EXCHANGE_OPERATOR,
                    "N/A",
                    "VALID"
                )
            );
        } catch (RuntimeException ex) {
            long duration = System.currentTimeMillis() - start;
            log.error(
                structuredLogger.build(
                    "validation",
                    "dataset",
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
