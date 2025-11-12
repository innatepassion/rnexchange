package com.rnexchange.repository;

import com.rnexchange.domain.RiskAlert;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the RiskAlert entity.
 */
@Repository
public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long>, JpaSpecificationExecutor<RiskAlert> {
    default Optional<RiskAlert> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<RiskAlert> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<RiskAlert> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select riskAlert from RiskAlert riskAlert left join fetch riskAlert.trader",
        countQuery = "select count(riskAlert) from RiskAlert riskAlert"
    )
    Page<RiskAlert> findAllWithToOneRelationships(Pageable pageable);

    @Query("select riskAlert from RiskAlert riskAlert left join fetch riskAlert.trader")
    List<RiskAlert> findAllWithToOneRelationships();

    @Query("select riskAlert from RiskAlert riskAlert left join fetch riskAlert.trader where riskAlert.id =:id")
    Optional<RiskAlert> findOneWithToOneRelationships(@Param("id") Long id);
}
