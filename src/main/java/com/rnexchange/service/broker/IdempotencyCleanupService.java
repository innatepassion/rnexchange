package com.rnexchange.service.broker;

import com.rnexchange.repository.IdempotencyTokenRepository;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyCleanupService {

    private static final Logger LOG = LoggerFactory.getLogger(IdempotencyCleanupService.class);
    private static final Duration RETENTION = Duration.ofHours(6);

    private final IdempotencyTokenRepository idempotencyTokenRepository;

    public IdempotencyCleanupService(IdempotencyTokenRepository idempotencyTokenRepository) {
        this.idempotencyTokenRepository = idempotencyTokenRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupOldTokens() {
        Instant cutoff = Instant.now().minus(RETENTION);
        long deleted = idempotencyTokenRepository.deleteByCreatedAtBefore(cutoff);
        if (deleted > 0) {
            LOG.info("IdempotencyCleanupService removed {} tokens older than {}", deleted, RETENTION);
        }
    }
}
