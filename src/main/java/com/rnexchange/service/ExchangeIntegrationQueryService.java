package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.ExchangeIntegration;
import com.rnexchange.repository.ExchangeIntegrationRepository;
import com.rnexchange.service.criteria.ExchangeIntegrationCriteria;
import com.rnexchange.service.dto.ExchangeIntegrationDTO;
import com.rnexchange.service.mapper.ExchangeIntegrationMapper;
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
 * Service for executing complex queries for {@link ExchangeIntegration} entities in the database.
 * The main input is a {@link ExchangeIntegrationCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ExchangeIntegrationDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ExchangeIntegrationQueryService extends QueryService<ExchangeIntegration> {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeIntegrationQueryService.class);

    private final ExchangeIntegrationRepository exchangeIntegrationRepository;

    private final ExchangeIntegrationMapper exchangeIntegrationMapper;

    public ExchangeIntegrationQueryService(
        ExchangeIntegrationRepository exchangeIntegrationRepository,
        ExchangeIntegrationMapper exchangeIntegrationMapper
    ) {
        this.exchangeIntegrationRepository = exchangeIntegrationRepository;
        this.exchangeIntegrationMapper = exchangeIntegrationMapper;
    }

    /**
     * Return a {@link Page} of {@link ExchangeIntegrationDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ExchangeIntegrationDTO> findByCriteria(ExchangeIntegrationCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ExchangeIntegration> specification = createSpecification(criteria);
        return exchangeIntegrationRepository.findAll(specification, page).map(exchangeIntegrationMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ExchangeIntegrationCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<ExchangeIntegration> specification = createSpecification(criteria);
        return exchangeIntegrationRepository.count(specification);
    }

    /**
     * Function to convert {@link ExchangeIntegrationCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ExchangeIntegration> createSpecification(ExchangeIntegrationCriteria criteria) {
        Specification<ExchangeIntegration> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), ExchangeIntegration_.id),
                buildStringSpecification(criteria.getProvider(), ExchangeIntegration_.provider),
                buildStringSpecification(criteria.getApiKey(), ExchangeIntegration_.apiKey),
                buildStringSpecification(criteria.getApiSecret(), ExchangeIntegration_.apiSecret),
                buildSpecification(criteria.getStatus(), ExchangeIntegration_.status),
                buildRangeSpecification(criteria.getLastHeartbeat(), ExchangeIntegration_.lastHeartbeat),
                buildSpecification(criteria.getExchangeId(), root ->
                    root.join(ExchangeIntegration_.exchange, JoinType.LEFT).get(Exchange_.id)
                )
            );
        }
        return specification;
    }
}
