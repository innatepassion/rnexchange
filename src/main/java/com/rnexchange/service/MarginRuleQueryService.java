package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.MarginRule;
import com.rnexchange.repository.MarginRuleRepository;
import com.rnexchange.service.criteria.MarginRuleCriteria;
import com.rnexchange.service.dto.MarginRuleDTO;
import com.rnexchange.service.mapper.MarginRuleMapper;
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
 * Service for executing complex queries for {@link MarginRule} entities in the database.
 * The main input is a {@link MarginRuleCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link MarginRuleDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MarginRuleQueryService extends QueryService<MarginRule> {

    private static final Logger LOG = LoggerFactory.getLogger(MarginRuleQueryService.class);

    private final MarginRuleRepository marginRuleRepository;

    private final MarginRuleMapper marginRuleMapper;

    public MarginRuleQueryService(MarginRuleRepository marginRuleRepository, MarginRuleMapper marginRuleMapper) {
        this.marginRuleRepository = marginRuleRepository;
        this.marginRuleMapper = marginRuleMapper;
    }

    /**
     * Return a {@link Page} of {@link MarginRuleDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<MarginRuleDTO> findByCriteria(MarginRuleCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<MarginRule> specification = createSpecification(criteria);
        return marginRuleRepository.findAll(specification, page).map(marginRuleMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(MarginRuleCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<MarginRule> specification = createSpecification(criteria);
        return marginRuleRepository.count(specification);
    }

    /**
     * Function to convert {@link MarginRuleCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<MarginRule> createSpecification(MarginRuleCriteria criteria) {
        Specification<MarginRule> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), MarginRule_.id),
                buildStringSpecification(criteria.getScope(), MarginRule_.scope),
                buildRangeSpecification(criteria.getInitialPct(), MarginRule_.initialPct),
                buildRangeSpecification(criteria.getMaintPct(), MarginRule_.maintPct),
                buildSpecification(criteria.getExchangeId(), root -> root.join(MarginRule_.exchange, JoinType.LEFT).get(Exchange_.id))
            );
        }
        return specification;
    }
}
