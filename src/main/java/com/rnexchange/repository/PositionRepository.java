package com.rnexchange.repository;

import com.rnexchange.domain.Position;
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
}
