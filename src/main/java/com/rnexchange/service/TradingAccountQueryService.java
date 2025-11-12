package com.rnexchange.service;

import com.rnexchange.domain.*; // for static metamodels
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.service.criteria.TradingAccountCriteria;
import com.rnexchange.service.dto.TradingAccountDTO;
import com.rnexchange.service.mapper.TradingAccountMapper;
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
 * Service for executing complex queries for {@link TradingAccount} entities in the database.
 * The main input is a {@link TradingAccountCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link TradingAccountDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TradingAccountQueryService extends QueryService<TradingAccount> {

    private static final Logger LOG = LoggerFactory.getLogger(TradingAccountQueryService.class);

    private final TradingAccountRepository tradingAccountRepository;

    private final TradingAccountMapper tradingAccountMapper;

    public TradingAccountQueryService(TradingAccountRepository tradingAccountRepository, TradingAccountMapper tradingAccountMapper) {
        this.tradingAccountRepository = tradingAccountRepository;
        this.tradingAccountMapper = tradingAccountMapper;
    }

    /**
     * Return a {@link Page} of {@link TradingAccountDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<TradingAccountDTO> findByCriteria(TradingAccountCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<TradingAccount> specification = createSpecification(criteria);
        return tradingAccountRepository.findAll(specification, page).map(tradingAccountMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TradingAccountCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<TradingAccount> specification = createSpecification(criteria);
        return tradingAccountRepository.count(specification);
    }

    /**
     * Function to convert {@link TradingAccountCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<TradingAccount> createSpecification(TradingAccountCriteria criteria) {
        Specification<TradingAccount> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), TradingAccount_.id),
                buildSpecification(criteria.getType(), TradingAccount_.type),
                buildSpecification(criteria.getBaseCcy(), TradingAccount_.baseCcy),
                buildRangeSpecification(criteria.getBalance(), TradingAccount_.balance),
                buildSpecification(criteria.getStatus(), TradingAccount_.status),
                buildSpecification(criteria.getBrokerId(), root -> root.join(TradingAccount_.broker, JoinType.LEFT).get(Broker_.id)),
                buildSpecification(criteria.getTraderId(), root -> root.join(TradingAccount_.trader, JoinType.LEFT).get(TraderProfile_.id))
            );
        }
        return specification;
    }
}
