package com.rnexchange.repository;

import com.rnexchange.domain.ExchangeVolatilityOverride;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeVolatilityOverrideRepository extends JpaRepository<ExchangeVolatilityOverride, Long> {
    @Override
    @EntityGraph(attributePaths = "exchange")
    List<ExchangeVolatilityOverride> findAll();
}
