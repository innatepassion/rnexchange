package com.rnexchange.repository;

import com.rnexchange.domain.Watchlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Watchlist entity.
 */
@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    @EntityGraph(attributePaths = "items")
    List<Watchlist> findAllByTraderProfileUserLoginOrderByNameAsc(String login);

    @EntityGraph(attributePaths = "items")
    Optional<Watchlist> findByIdAndTraderProfileUserLogin(Long id, String login);
}
