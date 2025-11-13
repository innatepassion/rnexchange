package com.rnexchange.repository;

import com.rnexchange.domain.Broker;
import com.rnexchange.repository.projection.BrokerInstrumentRow;
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

    Optional<Broker> findOneByCode(String code);

    @Query(
        """
            select new com.rnexchange.repository.projection.BrokerInstrumentRow(
                instrument.symbol,
                instrument.name,
                instrument.exchangeCode,
                instrument.assetClass,
                instrument.tickSize,
                instrument.lotSize,
                instrument.currency
            )
            from Broker broker, Instrument instrument
            where broker.id = :brokerId and instrument.status = 'ACTIVE'
            order by instrument.symbol
        """
    )
    List<BrokerInstrumentRow> findBaselineInstrumentCatalog(@Param("brokerId") Long brokerId);
}
