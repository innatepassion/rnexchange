package com.rnexchange.repository;

import com.rnexchange.domain.Broker;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.Position;
import com.rnexchange.domain.TradingAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Position entity.
 */
@Repository
public interface PositionRepository extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {
    default Optional<Position> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Position> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Position> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select position from Position position left join fetch position.instrument",
        countQuery = "select count(position) from Position position"
    )
    Page<Position> findAllWithToOneRelationships(Pageable pageable);

    @Query("select position from Position position left join fetch position.instrument")
    List<Position> findAllWithToOneRelationships();

    @Query("select position from Position position left join fetch position.instrument where position.id =:id")
    Optional<Position> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * Find position for a specific trading account and instrument.
     * Used for position tracking during trade execution.
     */
    Optional<Position> findByTradingAccountAndInstrument(TradingAccount tradingAccount, Instrument instrument);

    /**
     * Find all positions for a trading account.
     */
    List<Position> findByTradingAccount(TradingAccount tradingAccount);

    /**
     * T024: Find all positions for a specific broker by joining through trading accounts.
     * Used for Broker Admin portfolio views (Phase 5, US3).
     */
    @Query(
        value = "select position from Position position " +
        "left join fetch position.instrument " +
        "left join position.tradingAccount ta " +
        "where ta.broker = :broker " +
        "order by position.updatedAt desc",
        countQuery = "select count(position) from Position position " +
        "left join position.tradingAccount ta " +
        "where ta.broker = :broker"
    )
    Page<Position> findByBroker(@Param("broker") Broker broker, Pageable pageable);

    /**
     * T024: Find all positions for a specific broker (non-paginated).
     */
    @Query(
        "select position from Position position " +
        "left join position.tradingAccount ta " +
        "where ta.broker = :broker " +
        "order by position.updatedAt desc"
    )
    List<Position> findByBrokerNonPaginated(@Param("broker") Broker broker);
}
