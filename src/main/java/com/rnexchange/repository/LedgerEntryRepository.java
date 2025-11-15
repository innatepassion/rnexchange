package com.rnexchange.repository;

import com.rnexchange.domain.Broker;
import com.rnexchange.domain.LedgerEntry;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the LedgerEntry entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long>, JpaSpecificationExecutor<LedgerEntry> {
    /**
     * T024: Find all ledger entries for a specific broker by joining through trading accounts.
     * Used for Broker Admin portfolio views (Phase 5, US3).
     */
    @Query(
        value = "select le from LedgerEntry le " +
        "left join le.tradingAccount ta " +
        "where ta.broker = :broker " +
        "order by le.createdAt desc",
        countQuery = "select count(le) from LedgerEntry le " + "left join le.tradingAccount ta " + "where ta.broker = :broker"
    )
    Page<LedgerEntry> findByBroker(@Param("broker") Broker broker, Pageable pageable);

    /**
     * T024: Find all ledger entries for a specific broker (non-paginated).
     */
    @Query(
        "select le from LedgerEntry le " + "left join le.tradingAccount ta " + "where ta.broker = :broker " + "order by le.createdAt desc"
    )
    List<LedgerEntry> findByBrokerNonPaginated(@Param("broker") Broker broker);
}
