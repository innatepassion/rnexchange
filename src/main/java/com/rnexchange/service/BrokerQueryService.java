package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.Broker;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.service.criteria.BrokerCriteria;
import com.rnexchange.service.dto.BrokerDTO;
import com.rnexchange.service.mapper.BrokerMapper;
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
 * Service for executing complex queries for {@link Broker} entities in the database.
 * The main input is a {@link BrokerCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link BrokerDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BrokerQueryService extends QueryService<Broker> {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerQueryService.class);

    private final BrokerRepository brokerRepository;

    private final BrokerMapper brokerMapper;

    public BrokerQueryService(BrokerRepository brokerRepository, BrokerMapper brokerMapper) {
        this.brokerRepository = brokerRepository;
        this.brokerMapper = brokerMapper;
    }

    /**
     * Return a {@link Page} of {@link BrokerDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<BrokerDTO> findByCriteria(BrokerCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Broker> specification = createSpecification(criteria);
        return brokerRepository.findAll(specification, page).map(brokerMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BrokerCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Broker> specification = createSpecification(criteria);
        return brokerRepository.count(specification);
    }

    /**
     * Function to convert {@link BrokerCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Broker> createSpecification(BrokerCriteria criteria) {
        Specification<Broker> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Broker_.id),
                buildStringSpecification(criteria.getCode(), Broker_.code),
                buildStringSpecification(criteria.getName(), Broker_.name),
                buildStringSpecification(criteria.getStatus(), Broker_.status),
                buildRangeSpecification(criteria.getCreatedDate(), Broker_.createdDate),
                buildSpecification(criteria.getExchangeId(), root -> root.join(Broker_.exchange, JoinType.LEFT).get(Exchange_.id))
            );
        }
        return specification;
    }
}
