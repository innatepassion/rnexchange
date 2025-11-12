package com.rnexchange.repository;

import com.rnexchange.domain.SettlementBatch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SettlementBatch entity.
 */
@Repository
public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Long>, JpaSpecificationExecutor<SettlementBatch> {
    default Optional<SettlementBatch> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<SettlementBatch> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<SettlementBatch> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select settlementBatch from SettlementBatch settlementBatch left join fetch settlementBatch.exchange",
        countQuery = "select count(settlementBatch) from SettlementBatch settlementBatch"
    )
    Page<SettlementBatch> findAllWithToOneRelationships(Pageable pageable);

    @Query("select settlementBatch from SettlementBatch settlementBatch left join fetch settlementBatch.exchange")
    List<SettlementBatch> findAllWithToOneRelationships();

    @Query(
        "select settlementBatch from SettlementBatch settlementBatch left join fetch settlementBatch.exchange where settlementBatch.id =:id"
    )
    Optional<SettlementBatch> findOneWithToOneRelationships(@Param("id") Long id);
}
