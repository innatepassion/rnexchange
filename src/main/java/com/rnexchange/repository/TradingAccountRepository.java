package com.rnexchange.repository;

import com.rnexchange.domain.TradingAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TradingAccount entity.
 */
@Repository
public interface TradingAccountRepository extends JpaRepository<TradingAccount, Long>, JpaSpecificationExecutor<TradingAccount> {
    default Optional<TradingAccount> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<TradingAccount> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<TradingAccount> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select tradingAccount from TradingAccount tradingAccount left join fetch tradingAccount.broker left join fetch tradingAccount.trader",
        countQuery = "select count(tradingAccount) from TradingAccount tradingAccount"
    )
    Page<TradingAccount> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select tradingAccount from TradingAccount tradingAccount left join fetch tradingAccount.broker left join fetch tradingAccount.trader"
    )
    List<TradingAccount> findAllWithToOneRelationships();

    @Query(
        "select tradingAccount from TradingAccount tradingAccount left join fetch tradingAccount.broker left join fetch tradingAccount.trader where tradingAccount.id =:id"
    )
    Optional<TradingAccount> findOneWithToOneRelationships(@Param("id") Long id);

    Optional<TradingAccount> findFirstByTrader_User_LoginOrderByIdAsc(String login);

    /**
     * Phase 2 (T008): Broker-scoped queries
     */
    Page<TradingAccount> findByBroker_Id(@Param("brokerId") Long brokerId, Pageable pageable);

    Optional<TradingAccount> findByIdAndBroker_Id(@Param("id") Long id, @Param("brokerId") Long brokerId);

    Optional<TradingAccount> findFirstByBroker_Id(@Param("brokerId") Long brokerId);

    @Query("select ta from TradingAccount ta where ta.trader.id = :traderId and ta.broker.id = :brokerId")
    List<TradingAccount> findByTraderIdAndBrokerId(@Param("traderId") Long traderId, @Param("brokerId") Long brokerId);

    @Query(
        "select ta from TradingAccount ta left join fetch ta.trader left join fetch ta.trader.user where ta.broker.id = :brokerId and ta.trader.id in :traderIds"
    )
    List<TradingAccount> findByBrokerIdAndTraderIdIn(@Param("brokerId") Long brokerId, @Param("traderIds") List<Long> traderIds);
}
