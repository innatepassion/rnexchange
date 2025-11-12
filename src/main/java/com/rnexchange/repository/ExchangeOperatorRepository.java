package com.rnexchange.repository;

import com.rnexchange.domain.ExchangeOperator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ExchangeOperator entity.
 */
@Repository
public interface ExchangeOperatorRepository extends JpaRepository<ExchangeOperator, Long>, JpaSpecificationExecutor<ExchangeOperator> {
    default Optional<ExchangeOperator> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<ExchangeOperator> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<ExchangeOperator> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select exchangeOperator from ExchangeOperator exchangeOperator left join fetch exchangeOperator.user left join fetch exchangeOperator.exchange",
        countQuery = "select count(exchangeOperator) from ExchangeOperator exchangeOperator"
    )
    Page<ExchangeOperator> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select exchangeOperator from ExchangeOperator exchangeOperator left join fetch exchangeOperator.user left join fetch exchangeOperator.exchange"
    )
    List<ExchangeOperator> findAllWithToOneRelationships();

    @Query(
        "select exchangeOperator from ExchangeOperator exchangeOperator left join fetch exchangeOperator.user left join fetch exchangeOperator.exchange where exchangeOperator.id =:id"
    )
    Optional<ExchangeOperator> findOneWithToOneRelationships(@Param("id") Long id);
}
