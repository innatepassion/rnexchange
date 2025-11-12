package com.rnexchange.repository;

import com.rnexchange.domain.ExchangeIntegration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ExchangeIntegration entity.
 */
@Repository
public interface ExchangeIntegrationRepository
    extends JpaRepository<ExchangeIntegration, Long>, JpaSpecificationExecutor<ExchangeIntegration> {
    default Optional<ExchangeIntegration> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<ExchangeIntegration> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<ExchangeIntegration> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select exchangeIntegration from ExchangeIntegration exchangeIntegration left join fetch exchangeIntegration.exchange",
        countQuery = "select count(exchangeIntegration) from ExchangeIntegration exchangeIntegration"
    )
    Page<ExchangeIntegration> findAllWithToOneRelationships(Pageable pageable);

    @Query("select exchangeIntegration from ExchangeIntegration exchangeIntegration left join fetch exchangeIntegration.exchange")
    List<ExchangeIntegration> findAllWithToOneRelationships();

    @Query(
        "select exchangeIntegration from ExchangeIntegration exchangeIntegration left join fetch exchangeIntegration.exchange where exchangeIntegration.id =:id"
    )
    Optional<ExchangeIntegration> findOneWithToOneRelationships(@Param("id") Long id);
}
