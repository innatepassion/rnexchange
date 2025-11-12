package com.rnexchange.repository;

import com.rnexchange.domain.Broker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Broker entity.
 */
@Repository
public interface BrokerRepository extends JpaRepository<Broker, Long>, JpaSpecificationExecutor<Broker> {
    default Optional<Broker> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Broker> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Broker> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select broker from Broker broker left join fetch broker.exchange",
        countQuery = "select count(broker) from Broker broker"
    )
    Page<Broker> findAllWithToOneRelationships(Pageable pageable);

    @Query("select broker from Broker broker left join fetch broker.exchange")
    List<Broker> findAllWithToOneRelationships();

    @Query("select broker from Broker broker left join fetch broker.exchange where broker.id =:id")
    Optional<Broker> findOneWithToOneRelationships(@Param("id") Long id);
}
