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
}
