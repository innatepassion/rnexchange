package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.DailySettlementPrice;
import com.rnexchange.repository.DailySettlementPriceRepository;
import com.rnexchange.service.criteria.DailySettlementPriceCriteria;
import com.rnexchange.service.dto.DailySettlementPriceDTO;
import com.rnexchange.service.mapper.DailySettlementPriceMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link DailySettlementPrice} entities in the database.
 * The main input is a {@link DailySettlementPriceCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link DailySettlementPriceDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class DailySettlementPriceQueryService extends QueryService<DailySettlementPrice> {

    private static final Logger LOG = LoggerFactory.getLogger(DailySettlementPriceQueryService.class);

    private final DailySettlementPriceRepository dailySettlementPriceRepository;

    private final DailySettlementPriceMapper dailySettlementPriceMapper;

    public DailySettlementPriceQueryService(
        DailySettlementPriceRepository dailySettlementPriceRepository,
        DailySettlementPriceMapper dailySettlementPriceMapper
    ) {
        this.dailySettlementPriceRepository = dailySettlementPriceRepository;
        this.dailySettlementPriceMapper = dailySettlementPriceMapper;
    }

    /**
     * Return a {@link Page} of {@link DailySettlementPriceDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<DailySettlementPriceDTO> findByCriteria(DailySettlementPriceCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<DailySettlementPrice> specification = createSpecification(criteria);
        return dailySettlementPriceRepository.findAll(specification, page).map(dailySettlementPriceMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(DailySettlementPriceCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<DailySettlementPrice> specification = createSpecification(criteria);
        return dailySettlementPriceRepository.count(specification);
    }

    /**
     * Function to convert {@link DailySettlementPriceCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<DailySettlementPrice> createSpecification(DailySettlementPriceCriteria criteria) {
        Specification<DailySettlementPrice> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), DailySettlementPrice_.id),
                buildRangeSpecification(criteria.getRefDate(), DailySettlementPrice_.refDate),
                buildStringSpecification(criteria.getInstrumentSymbol(), DailySettlementPrice_.instrumentSymbol),
                buildRangeSpecification(criteria.getSettlePrice(), DailySettlementPrice_.settlePrice),
                buildSpecification(criteria.getInstrumentId(), root ->
                    root.join(DailySettlementPrice_.instrument, JoinType.LEFT).get(Instrument_.id)
                )
            );
        }
        return specification;
    }
}
