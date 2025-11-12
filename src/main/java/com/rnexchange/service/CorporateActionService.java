package com.rnexchange.service;

import com.rnexchange.domain.CorporateAction;
import com.rnexchange.repository.CorporateActionRepository;
import com.rnexchange.service.dto.CorporateActionDTO;
import com.rnexchange.service.mapper.CorporateActionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.CorporateAction}.
 */
@Service
@Transactional
public class CorporateActionService {

    private static final Logger LOG = LoggerFactory.getLogger(CorporateActionService.class);

    private final CorporateActionRepository corporateActionRepository;

    private final CorporateActionMapper corporateActionMapper;

    public CorporateActionService(CorporateActionRepository corporateActionRepository, CorporateActionMapper corporateActionMapper) {
        this.corporateActionRepository = corporateActionRepository;
        this.corporateActionMapper = corporateActionMapper;
    }

    /**
     * Save a corporateAction.
     *
     * @param corporateActionDTO the entity to save.
     * @return the persisted entity.
     */
    public CorporateActionDTO save(CorporateActionDTO corporateActionDTO) {
        LOG.debug("Request to save CorporateAction : {}", corporateActionDTO);
        CorporateAction corporateAction = corporateActionMapper.toEntity(corporateActionDTO);
        corporateAction = corporateActionRepository.save(corporateAction);
        return corporateActionMapper.toDto(corporateAction);
    }

    /**
     * Update a corporateAction.
     *
     * @param corporateActionDTO the entity to save.
     * @return the persisted entity.
     */
    public CorporateActionDTO update(CorporateActionDTO corporateActionDTO) {
        LOG.debug("Request to update CorporateAction : {}", corporateActionDTO);
        CorporateAction corporateAction = corporateActionMapper.toEntity(corporateActionDTO);
        corporateAction = corporateActionRepository.save(corporateAction);
        return corporateActionMapper.toDto(corporateAction);
    }

    /**
     * Partially update a corporateAction.
     *
     * @param corporateActionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CorporateActionDTO> partialUpdate(CorporateActionDTO corporateActionDTO) {
        LOG.debug("Request to partially update CorporateAction : {}", corporateActionDTO);

        return corporateActionRepository
            .findById(corporateActionDTO.getId())
            .map(existingCorporateAction -> {
                corporateActionMapper.partialUpdate(existingCorporateAction, corporateActionDTO);

                return existingCorporateAction;
            })
            .map(corporateActionRepository::save)
            .map(corporateActionMapper::toDto);
    }

    /**
     * Get all the corporateActions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<CorporateActionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return corporateActionRepository.findAllWithEagerRelationships(pageable).map(corporateActionMapper::toDto);
    }

    /**
     * Get one corporateAction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CorporateActionDTO> findOne(Long id) {
        LOG.debug("Request to get CorporateAction : {}", id);
        return corporateActionRepository.findOneWithEagerRelationships(id).map(corporateActionMapper::toDto);
    }

    /**
     * Delete the corporateAction by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete CorporateAction : {}", id);
        corporateActionRepository.deleteById(id);
    }
}
