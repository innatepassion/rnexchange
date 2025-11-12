package com.rnexchange.repository;

import com.rnexchange.domain.MarginRule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MarginRule entity.
 */
@Repository
public interface MarginRuleRepository extends JpaRepository<MarginRule, Long>, JpaSpecificationExecutor<MarginRule> {
    default Optional<MarginRule> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<MarginRule> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<MarginRule> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select marginRule from MarginRule marginRule left join fetch marginRule.exchange",
        countQuery = "select count(marginRule) from MarginRule marginRule"
    )
    Page<MarginRule> findAllWithToOneRelationships(Pageable pageable);

    @Query("select marginRule from MarginRule marginRule left join fetch marginRule.exchange")
    List<MarginRule> findAllWithToOneRelationships();

    @Query("select marginRule from MarginRule marginRule left join fetch marginRule.exchange where marginRule.id =:id")
    Optional<MarginRule> findOneWithToOneRelationships(@Param("id") Long id);
}
