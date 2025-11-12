package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.ExchangeOperator;
import com.rnexchange.repository.ExchangeOperatorRepository;
import com.rnexchange.service.criteria.ExchangeOperatorCriteria;
import com.rnexchange.service.dto.ExchangeOperatorDTO;
import com.rnexchange.service.mapper.ExchangeOperatorMapper;
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
 * Service for executing complex queries for {@link ExchangeOperator} entities in the database.
 * The main input is a {@link ExchangeOperatorCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ExchangeOperatorDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ExchangeOperatorQueryService extends QueryService<ExchangeOperator> {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeOperatorQueryService.class);

    private final ExchangeOperatorRepository exchangeOperatorRepository;

    private final ExchangeOperatorMapper exchangeOperatorMapper;

    public ExchangeOperatorQueryService(
        ExchangeOperatorRepository exchangeOperatorRepository,
        ExchangeOperatorMapper exchangeOperatorMapper
    ) {
        this.exchangeOperatorRepository = exchangeOperatorRepository;
        this.exchangeOperatorMapper = exchangeOperatorMapper;
    }

    /**
     * Return a {@link Page} of {@link ExchangeOperatorDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ExchangeOperatorDTO> findByCriteria(ExchangeOperatorCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ExchangeOperator> specification = createSpecification(criteria);
        return exchangeOperatorRepository.findAll(specification, page).map(exchangeOperatorMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ExchangeOperatorCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<ExchangeOperator> specification = createSpecification(criteria);
        return exchangeOperatorRepository.count(specification);
    }

    /**
     * Function to convert {@link ExchangeOperatorCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ExchangeOperator> createSpecification(ExchangeOperatorCriteria criteria) {
        Specification<ExchangeOperator> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), ExchangeOperator_.id),
                buildStringSpecification(criteria.getName(), ExchangeOperator_.name),
                buildSpecification(criteria.getUserId(), root -> root.join(ExchangeOperator_.user, JoinType.LEFT).get(User_.id)),
                buildSpecification(criteria.getExchangeId(), root -> root.join(ExchangeOperator_.exchange, JoinType.LEFT).get(Exchange_.id))
            );
        }
        return specification;
    }
}
