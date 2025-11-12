package com.rnexchange.repository;

import com.rnexchange.domain.Lot;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Lot entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LotRepository extends JpaRepository<Lot, Long>, JpaSpecificationExecutor<Lot> {}
