package com.rnexchange.repository;

import com.rnexchange.domain.BrokerDesk;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BrokerDesk entity.
 */
@Repository
public interface BrokerDeskRepository extends JpaRepository<BrokerDesk, Long>, JpaSpecificationExecutor<BrokerDesk> {
    default Optional<BrokerDesk> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<BrokerDesk> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<BrokerDesk> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select brokerDesk from BrokerDesk brokerDesk left join fetch brokerDesk.user left join fetch brokerDesk.broker",
        countQuery = "select count(brokerDesk) from BrokerDesk brokerDesk"
    )
    Page<BrokerDesk> findAllWithToOneRelationships(Pageable pageable);

    @Query("select brokerDesk from BrokerDesk brokerDesk left join fetch brokerDesk.user left join fetch brokerDesk.broker")
    List<BrokerDesk> findAllWithToOneRelationships();

    @Query(
        "select brokerDesk from BrokerDesk brokerDesk left join fetch brokerDesk.user left join fetch brokerDesk.broker where brokerDesk.id =:id"
    )
    Optional<BrokerDesk> findOneWithToOneRelationships(@Param("id") Long id);
}
