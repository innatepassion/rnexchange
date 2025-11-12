package com.rnexchange.repository;

import com.rnexchange.domain.Execution;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Execution entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ExecutionRepository extends JpaRepository<Execution, Long>, JpaSpecificationExecutor<Execution> {}
