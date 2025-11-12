package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.Lot;
import com.rnexchange.repository.LotRepository;
import com.rnexchange.service.criteria.LotCriteria;
import com.rnexchange.service.dto.LotDTO;
import com.rnexchange.service.mapper.LotMapper;
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
 * Service for executing complex queries for {@link Lot} entities in the database.
 * The main input is a {@link LotCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link LotDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class LotQueryService extends QueryService<Lot> {

    private static final Logger LOG = LoggerFactory.getLogger(LotQueryService.class);

    private final LotRepository lotRepository;

    private final LotMapper lotMapper;

    public LotQueryService(LotRepository lotRepository, LotMapper lotMapper) {
        this.lotRepository = lotRepository;
        this.lotMapper = lotMapper;
    }

    /**
     * Return a {@link Page} of {@link LotDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<LotDTO> findByCriteria(LotCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Lot> specification = createSpecification(criteria);
        return lotRepository.findAll(specification, page).map(lotMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(LotCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Lot> specification = createSpecification(criteria);
        return lotRepository.count(specification);
    }

    /**
     * Function to convert {@link LotCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Lot> createSpecification(LotCriteria criteria) {
        Specification<Lot> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Lot_.id),
                buildRangeSpecification(criteria.getOpenTs(), Lot_.openTs),
                buildRangeSpecification(criteria.getOpenPx(), Lot_.openPx),
                buildRangeSpecification(criteria.getQtyOpen(), Lot_.qtyOpen),
                buildRangeSpecification(criteria.getQtyClosed(), Lot_.qtyClosed),
                buildStringSpecification(criteria.getMethod(), Lot_.method),
                buildSpecification(criteria.getPositionId(), root -> root.join(Lot_.position, JoinType.LEFT).get(Position_.id))
            );
        }
        return specification;
    }
}
