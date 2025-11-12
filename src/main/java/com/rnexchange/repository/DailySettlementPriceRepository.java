package com.rnexchange.repository;

import com.rnexchange.domain.DailySettlementPrice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DailySettlementPrice entity.
 */
@Repository
public interface DailySettlementPriceRepository
    extends JpaRepository<DailySettlementPrice, Long>, JpaSpecificationExecutor<DailySettlementPrice> {
    default Optional<DailySettlementPrice> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<DailySettlementPrice> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<DailySettlementPrice> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select dailySettlementPrice from DailySettlementPrice dailySettlementPrice left join fetch dailySettlementPrice.instrument",
        countQuery = "select count(dailySettlementPrice) from DailySettlementPrice dailySettlementPrice"
    )
    Page<DailySettlementPrice> findAllWithToOneRelationships(Pageable pageable);

    @Query("select dailySettlementPrice from DailySettlementPrice dailySettlementPrice left join fetch dailySettlementPrice.instrument")
    List<DailySettlementPrice> findAllWithToOneRelationships();

    @Query(
        "select dailySettlementPrice from DailySettlementPrice dailySettlementPrice left join fetch dailySettlementPrice.instrument where dailySettlementPrice.id =:id"
    )
    Optional<DailySettlementPrice> findOneWithToOneRelationships(@Param("id") Long id);
}
