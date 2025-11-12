package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.Instrument;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.service.criteria.InstrumentCriteria;
import com.rnexchange.service.dto.InstrumentDTO;
import com.rnexchange.service.mapper.InstrumentMapper;
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
 * Service for executing complex queries for {@link Instrument} entities in the database.
 * The main input is a {@link InstrumentCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link InstrumentDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class InstrumentQueryService extends QueryService<Instrument> {

    private static final Logger LOG = LoggerFactory.getLogger(InstrumentQueryService.class);

    private final InstrumentRepository instrumentRepository;

    private final InstrumentMapper instrumentMapper;

    public InstrumentQueryService(InstrumentRepository instrumentRepository, InstrumentMapper instrumentMapper) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentMapper = instrumentMapper;
    }

    /**
     * Return a {@link Page} of {@link InstrumentDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<InstrumentDTO> findByCriteria(InstrumentCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Instrument> specification = createSpecification(criteria);
        return instrumentRepository.findAll(specification, page).map(instrumentMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(InstrumentCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Instrument> specification = createSpecification(criteria);
        return instrumentRepository.count(specification);
    }

    /**
     * Function to convert {@link InstrumentCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Instrument> createSpecification(InstrumentCriteria criteria) {
        Specification<Instrument> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Instrument_.id),
                buildStringSpecification(criteria.getSymbol(), Instrument_.symbol),
                buildStringSpecification(criteria.getName(), Instrument_.name),
                buildSpecification(criteria.getAssetClass(), Instrument_.assetClass),
                buildStringSpecification(criteria.getExchangeCode(), Instrument_.exchangeCode),
                buildRangeSpecification(criteria.getTickSize(), Instrument_.tickSize),
                buildRangeSpecification(criteria.getLotSize(), Instrument_.lotSize),
                buildSpecification(criteria.getCurrency(), Instrument_.currency),
                buildStringSpecification(criteria.getStatus(), Instrument_.status),
                buildSpecification(criteria.getExchangeId(), root -> root.join(Instrument_.exchange, JoinType.LEFT).get(Exchange_.id))
            );
        }
        return specification;
    }
}
