package com.rnexchange.repository;

import com.rnexchange.domain.Broker;
import com.rnexchange.domain.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Order entity.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    default Optional<Order> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Order> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Order> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select jhiOrder from Order jhiOrder left join fetch jhiOrder.instrument",
        countQuery = "select count(jhiOrder) from Order jhiOrder"
    )
    Page<Order> findAllWithToOneRelationships(Pageable pageable);

    @Query("select jhiOrder from Order jhiOrder left join fetch jhiOrder.instrument")
    List<Order> findAllWithToOneRelationships();

    @Query("select jhiOrder from Order jhiOrder left join fetch jhiOrder.instrument where jhiOrder.id =:id")
    Optional<Order> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * T024: Find all orders for a specific broker by joining through trading accounts.
     * Used for Broker Admin portfolio views (Phase 5, US3).
     */
    @Query(
        value = "select jhiOrder from Order jhiOrder " +
        "left join fetch jhiOrder.instrument " +
        "left join jhiOrder.tradingAccount ta " +
        "where ta.broker = :broker " +
        "order by jhiOrder.createdAt desc",
        countQuery = "select count(jhiOrder) from Order jhiOrder " + "left join jhiOrder.tradingAccount ta " + "where ta.broker = :broker"
    )
    Page<Order> findByBroker(@Param("broker") Broker broker, Pageable pageable);

    /**
     * T024: Find all orders for a specific broker (non-paginated).
     */
    @Query(
        "select jhiOrder from Order jhiOrder " +
        "left join jhiOrder.tradingAccount ta " +
        "where ta.broker = :broker " +
        "order by jhiOrder.createdAt desc"
    )
    List<Order> findByBrokerNonPaginated(@Param("broker") Broker broker);
}
