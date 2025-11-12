package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.service.criteria.TraderProfileCriteria;
import com.rnexchange.service.dto.TraderProfileDTO;
import com.rnexchange.service.mapper.TraderProfileMapper;
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
 * Service for executing complex queries for {@link TraderProfile} entities in the database.
 * The main input is a {@link TraderProfileCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link TraderProfileDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TraderProfileQueryService extends QueryService<TraderProfile> {

    private static final Logger LOG = LoggerFactory.getLogger(TraderProfileQueryService.class);

    private final TraderProfileRepository traderProfileRepository;

    private final TraderProfileMapper traderProfileMapper;

    public TraderProfileQueryService(TraderProfileRepository traderProfileRepository, TraderProfileMapper traderProfileMapper) {
        this.traderProfileRepository = traderProfileRepository;
        this.traderProfileMapper = traderProfileMapper;
    }

    /**
     * Return a {@link Page} of {@link TraderProfileDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<TraderProfileDTO> findByCriteria(TraderProfileCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<TraderProfile> specification = createSpecification(criteria);
        return traderProfileRepository.findAll(specification, page).map(traderProfileMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TraderProfileCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<TraderProfile> specification = createSpecification(criteria);
        return traderProfileRepository.count(specification);
    }

    /**
     * Function to convert {@link TraderProfileCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<TraderProfile> createSpecification(TraderProfileCriteria criteria) {
        Specification<TraderProfile> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), TraderProfile_.id),
                buildStringSpecification(criteria.getDisplayName(), TraderProfile_.displayName),
                buildStringSpecification(criteria.getEmail(), TraderProfile_.email),
                buildStringSpecification(criteria.getMobile(), TraderProfile_.mobile),
                buildSpecification(criteria.getKycStatus(), TraderProfile_.kycStatus),
                buildSpecification(criteria.getStatus(), TraderProfile_.status),
                buildSpecification(criteria.getUserId(), root -> root.join(TraderProfile_.user, JoinType.LEFT).get(User_.id))
            );
        }
        return specification;
    }
}
