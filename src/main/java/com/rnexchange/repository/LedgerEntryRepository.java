package com.rnexchange.repository;

import com.rnexchange.domain.LedgerEntry;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the LedgerEntry entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long>, JpaSpecificationExecutor<LedgerEntry> {}
