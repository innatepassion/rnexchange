package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.Exchange;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.service.criteria.ExchangeCriteria;
import com.rnexchange.service.dto.ExchangeDTO;
import com.rnexchange.service.mapper.ExchangeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Exchange} entities in the database.
 * The main input is a {@link ExchangeCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ExchangeDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ExchangeQueryService extends QueryService<Exchange> {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeQueryService.class);

    private final ExchangeRepository exchangeRepository;

    private final ExchangeMapper exchangeMapper;

    public ExchangeQueryService(ExchangeRepository exchangeRepository, ExchangeMapper exchangeMapper) {
        this.exchangeRepository = exchangeRepository;
        this.exchangeMapper = exchangeMapper;
    }

    /**
     * Return a {@link Page} of {@link ExchangeDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ExchangeDTO> findByCriteria(ExchangeCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Exchange> specification = createSpecification(criteria);
        return exchangeRepository.findAll(specification, page).map(exchangeMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ExchangeCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Exchange> specification = createSpecification(criteria);
        return exchangeRepository.count(specification);
    }

    /**
     * Function to convert {@link ExchangeCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Exchange> createSpecification(ExchangeCriteria criteria) {
        Specification<Exchange> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Exchange_.id),
                buildStringSpecification(criteria.getCode(), Exchange_.code),
                buildStringSpecification(criteria.getName(), Exchange_.name),
                buildStringSpecification(criteria.getTimezone(), Exchange_.timezone),
                buildSpecification(criteria.getStatus(), Exchange_.status)
            );
        }
        return specification;
    }
}
