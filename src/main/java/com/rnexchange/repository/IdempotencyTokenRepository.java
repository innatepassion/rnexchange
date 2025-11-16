package com.rnexchange.repository;

import com.rnexchange.domain.IdempotencyToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyTokenRepository extends JpaRepository<IdempotencyToken, Long> {
    Optional<IdempotencyToken> findByToken(String token);
    long deleteByCreatedAtBefore(Instant threshold);
}
