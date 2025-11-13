package com.rnexchange.service.seed;

import com.rnexchange.service.dto.BaselineSeedJobDTO;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class BaselineSeedJobRegistry {

    private final Map<UUID, BaselineSeedJobDTO> jobs = new ConcurrentHashMap<>();

    public BaselineSeedJobDTO startJob(UUID jobId) {
        BaselineSeedJobDTO existing = jobs.get(jobId);
        if (existing != null && !existing.isTerminal()) {
            throw new BaselineSeedJobInProgressException(jobId);
        }

        BaselineSeedJobDTO job = new BaselineSeedJobDTO();
        job.setJobId(jobId);
        job.setStatus("PENDING");
        job.setSubmittedAt(Instant.now());
        job.setMetrics(Collections.emptyMap());
        job.setErrors(Collections.emptyList());
        jobs.put(jobId, job);
        return job;
    }

    public void markRunning(UUID jobId) {
        BaselineSeedJobDTO job = getOrFail(jobId);
        job.setStatus("RUNNING");
        job.setStartedAt(Instant.now());
    }

    public void markCompleted(UUID jobId, long durationMs, Map<String, String> metrics) {
        BaselineSeedJobDTO job = getOrFail(jobId);
        job.setStatus("COMPLETED");
        job.setCompletedAt(Instant.now());
        job.setDurationMs(durationMs);
        job.setMetrics(metrics);
        job.setErrors(Collections.emptyList());
    }

    public void markFailed(UUID jobId, String failureReason) {
        BaselineSeedJobDTO job = getOrFail(jobId);
        Instant completedAt = Instant.now();
        job.setStatus("FAILED");
        job.setCompletedAt(completedAt);
        if (job.getStartedAt() != null) {
            job.setDurationMs(completedAt.toEpochMilli() - job.getStartedAt().toEpochMilli());
        }
        job.setErrors(Collections.singletonList(failureReason));
    }

    public Optional<BaselineSeedJobDTO> findJob(UUID jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    private BaselineSeedJobDTO getOrFail(UUID jobId) {
        BaselineSeedJobDTO job = jobs.get(jobId);
        if (job == null) {
            throw new IllegalStateException("Baseline seed job " + jobId + " not found in registry");
        }
        return job;
    }
}
