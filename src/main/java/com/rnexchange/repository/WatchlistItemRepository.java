package com.rnexchange.repository;

import com.rnexchange.domain.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {}
