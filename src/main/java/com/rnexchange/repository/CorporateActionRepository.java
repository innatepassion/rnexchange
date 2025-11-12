package com.rnexchange.repository;

import com.rnexchange.domain.CorporateAction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CorporateAction entity.
 */
@Repository
public interface CorporateActionRepository extends JpaRepository<CorporateAction, Long>, JpaSpecificationExecutor<CorporateAction> {
    default Optional<CorporateAction> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CorporateAction> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CorporateAction> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select corporateAction from CorporateAction corporateAction left join fetch corporateAction.instrument",
        countQuery = "select count(corporateAction) from CorporateAction corporateAction"
    )
    Page<CorporateAction> findAllWithToOneRelationships(Pageable pageable);

    @Query("select corporateAction from CorporateAction corporateAction left join fetch corporateAction.instrument")
    List<CorporateAction> findAllWithToOneRelationships();

    @Query(
        "select corporateAction from CorporateAction corporateAction left join fetch corporateAction.instrument where corporateAction.id =:id"
    )
    Optional<CorporateAction> findOneWithToOneRelationships(@Param("id") Long id);
}
