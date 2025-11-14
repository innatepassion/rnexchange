package com.rnexchange.repository;

import com.rnexchange.domain.TraderProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TraderProfile entity.
 */
@Repository
public interface TraderProfileRepository extends JpaRepository<TraderProfile, Long>, JpaSpecificationExecutor<TraderProfile> {
    default Optional<TraderProfile> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<TraderProfile> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<TraderProfile> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select traderProfile from TraderProfile traderProfile left join fetch traderProfile.user",
        countQuery = "select count(traderProfile) from TraderProfile traderProfile"
    )
    Page<TraderProfile> findAllWithToOneRelationships(Pageable pageable);

    @Query("select traderProfile from TraderProfile traderProfile left join fetch traderProfile.user")
    List<TraderProfile> findAllWithToOneRelationships();

    @Query("select traderProfile from TraderProfile traderProfile left join fetch traderProfile.user where traderProfile.id =:id")
    Optional<TraderProfile> findOneWithToOneRelationships(@Param("id") Long id);

    @EntityGraph(attributePaths = "user")
    Optional<TraderProfile> findOneByUserLogin(String login);
}
