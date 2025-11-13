package com.rnexchange.repository;

import com.rnexchange.domain.Instrument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Instrument entity.
 */
@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long>, JpaSpecificationExecutor<Instrument> {
    default Optional<Instrument> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Instrument> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Instrument> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select instrument from Instrument instrument left join fetch instrument.exchange",
        countQuery = "select count(instrument) from Instrument instrument"
    )
    Page<Instrument> findAllWithToOneRelationships(Pageable pageable);

    @Query("select instrument from Instrument instrument left join fetch instrument.exchange")
    List<Instrument> findAllWithToOneRelationships();

    @Query("select instrument from Instrument instrument left join fetch instrument.exchange where instrument.id =:id")
    Optional<Instrument> findOneWithToOneRelationships(@Param("id") Long id);

    Optional<Instrument> findOneBySymbol(String symbol);
}
