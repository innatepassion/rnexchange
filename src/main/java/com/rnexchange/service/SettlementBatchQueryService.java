package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.SettlementBatch;
import com.rnexchange.repository.SettlementBatchRepository;
import com.rnexchange.service.criteria.SettlementBatchCriteria;
import com.rnexchange.service.dto.SettlementBatchDTO;
import com.rnexchange.service.mapper.SettlementBatchMapper;
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
 * Service for executing complex queries for {@link SettlementBatch} entities in the database.
 * The main input is a {@link SettlementBatchCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link SettlementBatchDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SettlementBatchQueryService extends QueryService<SettlementBatch> {

    private static final Logger LOG = LoggerFactory.getLogger(SettlementBatchQueryService.class);

    private final SettlementBatchRepository settlementBatchRepository;

    private final SettlementBatchMapper settlementBatchMapper;

    public SettlementBatchQueryService(SettlementBatchRepository settlementBatchRepository, SettlementBatchMapper settlementBatchMapper) {
        this.settlementBatchRepository = settlementBatchRepository;
        this.settlementBatchMapper = settlementBatchMapper;
    }

    /**
     * Return a {@link Page} of {@link SettlementBatchDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SettlementBatchDTO> findByCriteria(SettlementBatchCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<SettlementBatch> specification = createSpecification(criteria);
        return settlementBatchRepository.findAll(specification, page).map(settlementBatchMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SettlementBatchCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<SettlementBatch> specification = createSpecification(criteria);
        return settlementBatchRepository.count(specification);
    }

    /**
     * Function to convert {@link SettlementBatchCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<SettlementBatch> createSpecification(SettlementBatchCriteria criteria) {
        Specification<SettlementBatch> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), SettlementBatch_.id),
                buildRangeSpecification(criteria.getRefDate(), SettlementBatch_.refDate),
                buildSpecification(criteria.getKind(), SettlementBatch_.kind),
                buildSpecification(criteria.getStatus(), SettlementBatch_.status),
                buildStringSpecification(criteria.getRemarks(), SettlementBatch_.remarks),
                buildSpecification(criteria.getExchangeId(), root -> root.join(SettlementBatch_.exchange, JoinType.LEFT).get(Exchange_.id))
            );
        }
        return specification;
    }
}
