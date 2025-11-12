package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.BrokerDesk;
import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.service.criteria.BrokerDeskCriteria;
import com.rnexchange.service.dto.BrokerDeskDTO;
import com.rnexchange.service.mapper.BrokerDeskMapper;
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
 * Service for executing complex queries for {@link BrokerDesk} entities in the database.
 * The main input is a {@link BrokerDeskCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link BrokerDeskDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BrokerDeskQueryService extends QueryService<BrokerDesk> {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerDeskQueryService.class);

    private final BrokerDeskRepository brokerDeskRepository;

    private final BrokerDeskMapper brokerDeskMapper;

    public BrokerDeskQueryService(BrokerDeskRepository brokerDeskRepository, BrokerDeskMapper brokerDeskMapper) {
        this.brokerDeskRepository = brokerDeskRepository;
        this.brokerDeskMapper = brokerDeskMapper;
    }

    /**
     * Return a {@link Page} of {@link BrokerDeskDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<BrokerDeskDTO> findByCriteria(BrokerDeskCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<BrokerDesk> specification = createSpecification(criteria);
        return brokerDeskRepository.findAll(specification, page).map(brokerDeskMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BrokerDeskCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<BrokerDesk> specification = createSpecification(criteria);
        return brokerDeskRepository.count(specification);
    }

    /**
     * Function to convert {@link BrokerDeskCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<BrokerDesk> createSpecification(BrokerDeskCriteria criteria) {
        Specification<BrokerDesk> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), BrokerDesk_.id),
                buildStringSpecification(criteria.getName(), BrokerDesk_.name),
                buildSpecification(criteria.getUserId(), root -> root.join(BrokerDesk_.user, JoinType.LEFT).get(User_.id)),
                buildSpecification(criteria.getBrokerId(), root -> root.join(BrokerDesk_.broker, JoinType.LEFT).get(Broker_.id))
            );
        }
        return specification;
    }
}
