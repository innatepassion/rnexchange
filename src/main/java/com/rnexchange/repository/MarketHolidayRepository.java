package com.rnexchange.repository;

import com.rnexchange.domain.MarketHoliday;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MarketHoliday entity.
 */
@Repository
public interface MarketHolidayRepository extends JpaRepository<MarketHoliday, Long>, JpaSpecificationExecutor<MarketHoliday> {
    default Optional<MarketHoliday> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<MarketHoliday> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<MarketHoliday> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select marketHoliday from MarketHoliday marketHoliday left join fetch marketHoliday.exchange",
        countQuery = "select count(marketHoliday) from MarketHoliday marketHoliday"
    )
    Page<MarketHoliday> findAllWithToOneRelationships(Pageable pageable);

    @Query("select marketHoliday from MarketHoliday marketHoliday left join fetch marketHoliday.exchange")
    List<MarketHoliday> findAllWithToOneRelationships();

    @Query("select marketHoliday from MarketHoliday marketHoliday left join fetch marketHoliday.exchange where marketHoliday.id =:id")
    Optional<MarketHoliday> findOneWithToOneRelationships(@Param("id") Long id);
}
