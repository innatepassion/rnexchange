package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.CorporateAction;
import com.rnexchange.repository.CorporateActionRepository;
import com.rnexchange.service.criteria.CorporateActionCriteria;
import com.rnexchange.service.dto.CorporateActionDTO;
import com.rnexchange.service.mapper.CorporateActionMapper;
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
 * Service for executing complex queries for {@link CorporateAction} entities in the database.
 * The main input is a {@link CorporateActionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link CorporateActionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CorporateActionQueryService extends QueryService<CorporateAction> {

    private static final Logger LOG = LoggerFactory.getLogger(CorporateActionQueryService.class);

    private final CorporateActionRepository corporateActionRepository;

    private final CorporateActionMapper corporateActionMapper;

    public CorporateActionQueryService(CorporateActionRepository corporateActionRepository, CorporateActionMapper corporateActionMapper) {
        this.corporateActionRepository = corporateActionRepository;
        this.corporateActionMapper = corporateActionMapper;
    }

    /**
     * Return a {@link Page} of {@link CorporateActionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CorporateActionDTO> findByCriteria(CorporateActionCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CorporateAction> specification = createSpecification(criteria);
        return corporateActionRepository.findAll(specification, page).map(corporateActionMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CorporateActionCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<CorporateAction> specification = createSpecification(criteria);
        return corporateActionRepository.count(specification);
    }

    /**
     * Function to convert {@link CorporateActionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CorporateAction> createSpecification(CorporateActionCriteria criteria) {
        Specification<CorporateAction> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), CorporateAction_.id),
                buildSpecification(criteria.getType(), CorporateAction_.type),
                buildStringSpecification(criteria.getInstrumentSymbol(), CorporateAction_.instrumentSymbol),
                buildRangeSpecification(criteria.getExDate(), CorporateAction_.exDate),
                buildRangeSpecification(criteria.getPayDate(), CorporateAction_.payDate),
                buildRangeSpecification(criteria.getRatio(), CorporateAction_.ratio),
                buildRangeSpecification(criteria.getCashAmount(), CorporateAction_.cashAmount),
                buildSpecification(criteria.getInstrumentId(), root ->
                    root.join(CorporateAction_.instrument, JoinType.LEFT).get(Instrument_.id)
                )
            );
        }
        return specification;
    }
}
