package com.rnexchange.service.seed;

import com.rnexchange.config.BaselineSeedCleanupRunner;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.dto.BaselineSeedJobDTO;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import com.rnexchange.service.seed.dto.BaselineSeedValidationReport;
import com.rnexchange.service.startup.BaselineValidationRunner;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BaselineSeedServiceImpl implements BaselineSeedService {

    private static final Logger log = LoggerFactory.getLogger(BaselineSeedService.class);

    private final BaselinePrerequisiteValidator prerequisiteValidator;
    private final BaselineSeedVerificationService verificationService;
    private final BaselineSeedCleanupRunner cleanupRunner;
    private final BaselineSeedDataLoader dataLoader;
    private final BaselineValidationRunner validationRunner;
    private final BaselineSeedStructuredLogger structuredLogger;
    private final BaselineSeedJobRegistry jobRegistry;
    private final MeterRegistry meterRegistry;

    public BaselineSeedServiceImpl(
        BaselinePrerequisiteValidator prerequisiteValidator,
        BaselineSeedVerificationService verificationService,
        BaselineSeedCleanupRunner cleanupRunner,
        BaselineSeedDataLoader dataLoader,
        BaselineValidationRunner validationRunner,
        BaselineSeedStructuredLogger structuredLogger,
        BaselineSeedJobRegistry jobRegistry,
        MeterRegistry meterRegistry
    ) {
        this.prerequisiteValidator = prerequisiteValidator;
        this.verificationService = verificationService;
        this.cleanupRunner = cleanupRunner;
        this.dataLoader = dataLoader;
        this.validationRunner = validationRunner;
        this.structuredLogger = structuredLogger;
        this.jobRegistry = jobRegistry;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void runBaselineSeedBlocking(BaselineSeedRequest request) {
        UUID jobId = request.getInvocationId();
        BaselineSeedJobDTO job = jobRegistry.startJob(jobId);
        jobRegistry.markRunning(jobId);
        Timer.Sample sample = Timer.start(meterRegistry);
        Timer timer = meterRegistry.timer("baseline.seed.duration");
        long startTime = System.currentTimeMillis();

        try {
            BaselineSeedValidationReport report = prerequisiteValidator.validate(request);
            if (!report.isSuccessful()) {
                throw new BaselineSeedPrerequisiteException(String.join("; ", report.getErrors()));
            }

            verificationService.assertNoDuplicateInstruments();
            cleanupRunner.runCleanup(request);
            Map<String, String> metrics = dataLoader.seed(request);
            validationRunner.runPostSeedValidation(request);

            long duration = System.currentTimeMillis() - startTime;
            sample.stop(timer);
            jobRegistry.markCompleted(jobId, duration, metrics);
            log.info(
                structuredLogger.build(
                    "seed",
                    "baseline",
                    "SUCCESS",
                    duration,
                    "",
                    request.getInvocationId(),
                    AuthoritiesConstants.EXCHANGE_OPERATOR,
                    "N/A",
                    "COMPLETED"
                )
            );
        } catch (BaselineSeedPrerequisiteException | BaselineSeedVerificationException ex) {
            handleFailure(request, startTime, sample, timer, ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            handleFailure(request, startTime, sample, timer, ex.getMessage(), ex);
        }
    }

    private void handleFailure(
        BaselineSeedRequest request,
        long startTime,
        Timer.Sample sample,
        Timer timer,
        String failureReason,
        RuntimeException exception
    ) {
        long duration = System.currentTimeMillis() - startTime;
        sample.stop(timer);
        jobRegistry.markFailed(request.getInvocationId(), failureReason);
        log.error(
            structuredLogger.build(
                "seed",
                "baseline",
                "FAILED",
                duration,
                failureReason,
                request.getInvocationId(),
                AuthoritiesConstants.EXCHANGE_OPERATOR,
                "N/A",
                "FAILED"
            ),
            exception
        );
        throw exception;
    }
}
