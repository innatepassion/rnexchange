package com.rnexchange.service;

import com.rnexchange.domain.MarginRule;
import com.rnexchange.repository.MarginRuleRepository;
import com.rnexchange.service.dto.MarginRuleDTO;
import com.rnexchange.service.mapper.MarginRuleMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.MarginRule}.
 */
@Service
@Transactional
public class MarginRuleService {

    private static final Logger LOG = LoggerFactory.getLogger(MarginRuleService.class);

    private final MarginRuleRepository marginRuleRepository;

    private final MarginRuleMapper marginRuleMapper;

    public MarginRuleService(MarginRuleRepository marginRuleRepository, MarginRuleMapper marginRuleMapper) {
        this.marginRuleRepository = marginRuleRepository;
        this.marginRuleMapper = marginRuleMapper;
    }

    /**
     * Save a marginRule.
     *
     * @param marginRuleDTO the entity to save.
     * @return the persisted entity.
     */
    public MarginRuleDTO save(MarginRuleDTO marginRuleDTO) {
        LOG.debug("Request to save MarginRule : {}", marginRuleDTO);
        MarginRule marginRule = marginRuleMapper.toEntity(marginRuleDTO);
        marginRule = marginRuleRepository.save(marginRule);
        return marginRuleMapper.toDto(marginRule);
    }

    /**
     * Update a marginRule.
     *
     * @param marginRuleDTO the entity to save.
     * @return the persisted entity.
     */
    public MarginRuleDTO update(MarginRuleDTO marginRuleDTO) {
        LOG.debug("Request to update MarginRule : {}", marginRuleDTO);
        MarginRule marginRule = marginRuleMapper.toEntity(marginRuleDTO);
        marginRule = marginRuleRepository.save(marginRule);
        return marginRuleMapper.toDto(marginRule);
    }

    /**
     * Partially update a marginRule.
     *
     * @param marginRuleDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MarginRuleDTO> partialUpdate(MarginRuleDTO marginRuleDTO) {
        LOG.debug("Request to partially update MarginRule : {}", marginRuleDTO);

        return marginRuleRepository
            .findById(marginRuleDTO.getId())
            .map(existingMarginRule -> {
                marginRuleMapper.partialUpdate(existingMarginRule, marginRuleDTO);

                return existingMarginRule;
            })
            .map(marginRuleRepository::save)
            .map(marginRuleMapper::toDto);
    }

    /**
     * Get all the marginRules with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<MarginRuleDTO> findAllWithEagerRelationships(Pageable pageable) {
        return marginRuleRepository.findAllWithEagerRelationships(pageable).map(marginRuleMapper::toDto);
    }

    /**
     * Get one marginRule by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MarginRuleDTO> findOne(Long id) {
        LOG.debug("Request to get MarginRule : {}", id);
        return marginRuleRepository.findOneWithEagerRelationships(id).map(marginRuleMapper::toDto);
    }

    /**
     * Delete the marginRule by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete MarginRule : {}", id);
        marginRuleRepository.deleteById(id);
    }
}
