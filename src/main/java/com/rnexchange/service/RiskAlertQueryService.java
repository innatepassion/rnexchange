package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.RiskAlert;
import com.rnexchange.repository.RiskAlertRepository;
import com.rnexchange.service.criteria.RiskAlertCriteria;
import com.rnexchange.service.dto.RiskAlertDTO;
import com.rnexchange.service.mapper.RiskAlertMapper;
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
 * Service for executing complex queries for {@link RiskAlert} entities in the database.
 * The main input is a {@link RiskAlertCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link RiskAlertDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class RiskAlertQueryService extends QueryService<RiskAlert> {

    private static final Logger LOG = LoggerFactory.getLogger(RiskAlertQueryService.class);

    private final RiskAlertRepository riskAlertRepository;

    private final RiskAlertMapper riskAlertMapper;

    public RiskAlertQueryService(RiskAlertRepository riskAlertRepository, RiskAlertMapper riskAlertMapper) {
        this.riskAlertRepository = riskAlertRepository;
        this.riskAlertMapper = riskAlertMapper;
    }

    /**
     * Return a {@link Page} of {@link RiskAlertDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<RiskAlertDTO> findByCriteria(RiskAlertCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<RiskAlert> specification = createSpecification(criteria);
        return riskAlertRepository.findAll(specification, page).map(riskAlertMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(RiskAlertCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<RiskAlert> specification = createSpecification(criteria);
        return riskAlertRepository.count(specification);
    }

    /**
     * Function to convert {@link RiskAlertCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<RiskAlert> createSpecification(RiskAlertCriteria criteria) {
        Specification<RiskAlert> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), RiskAlert_.id),
                buildSpecification(criteria.getAlertType(), RiskAlert_.alertType),
                buildStringSpecification(criteria.getDescription(), RiskAlert_.description),
                buildRangeSpecification(criteria.getCreatedAt(), RiskAlert_.createdAt),
                buildSpecification(criteria.getTradingAccountId(), root ->
                    root.join(RiskAlert_.tradingAccount, JoinType.LEFT).get(TradingAccount_.id)
                ),
                buildSpecification(criteria.getTraderId(), root -> root.join(RiskAlert_.trader, JoinType.LEFT).get(TraderProfile_.id))
            );
        }
        return specification;
    }
}
