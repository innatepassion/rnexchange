package com.rnexchange.repository;

import com.rnexchange.domain.Exchange;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Exchange entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long>, JpaSpecificationExecutor<Exchange> {
    java.util.Optional<Exchange> findOneByCode(String code);
}
