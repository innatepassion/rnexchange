package com.rnexchange.service;

import com.rnexchange.domain.SettlementBatch;
import com.rnexchange.repository.SettlementBatchRepository;
import com.rnexchange.service.dto.SettlementBatchDTO;
import com.rnexchange.service.mapper.SettlementBatchMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.SettlementBatch}.
 */
@Service
@Transactional
public class SettlementBatchService {

    private static final Logger LOG = LoggerFactory.getLogger(SettlementBatchService.class);

    private final SettlementBatchRepository settlementBatchRepository;

    private final SettlementBatchMapper settlementBatchMapper;

    public SettlementBatchService(SettlementBatchRepository settlementBatchRepository, SettlementBatchMapper settlementBatchMapper) {
        this.settlementBatchRepository = settlementBatchRepository;
        this.settlementBatchMapper = settlementBatchMapper;
    }

    /**
     * Save a settlementBatch.
     *
     * @param settlementBatchDTO the entity to save.
     * @return the persisted entity.
     */
    public SettlementBatchDTO save(SettlementBatchDTO settlementBatchDTO) {
        LOG.debug("Request to save SettlementBatch : {}", settlementBatchDTO);
        SettlementBatch settlementBatch = settlementBatchMapper.toEntity(settlementBatchDTO);
        settlementBatch = settlementBatchRepository.save(settlementBatch);
        return settlementBatchMapper.toDto(settlementBatch);
    }

    /**
     * Update a settlementBatch.
     *
     * @param settlementBatchDTO the entity to save.
     * @return the persisted entity.
     */
    public SettlementBatchDTO update(SettlementBatchDTO settlementBatchDTO) {
        LOG.debug("Request to update SettlementBatch : {}", settlementBatchDTO);
        SettlementBatch settlementBatch = settlementBatchMapper.toEntity(settlementBatchDTO);
        settlementBatch = settlementBatchRepository.save(settlementBatch);
        return settlementBatchMapper.toDto(settlementBatch);
    }

    /**
     * Partially update a settlementBatch.
     *
     * @param settlementBatchDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SettlementBatchDTO> partialUpdate(SettlementBatchDTO settlementBatchDTO) {
        LOG.debug("Request to partially update SettlementBatch : {}", settlementBatchDTO);

        return settlementBatchRepository
            .findById(settlementBatchDTO.getId())
            .map(existingSettlementBatch -> {
                settlementBatchMapper.partialUpdate(existingSettlementBatch, settlementBatchDTO);

                return existingSettlementBatch;
            })
            .map(settlementBatchRepository::save)
            .map(settlementBatchMapper::toDto);
    }

    /**
     * Get all the settlementBatches with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<SettlementBatchDTO> findAllWithEagerRelationships(Pageable pageable) {
        return settlementBatchRepository.findAllWithEagerRelationships(pageable).map(settlementBatchMapper::toDto);
    }

    /**
     * Get one settlementBatch by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SettlementBatchDTO> findOne(Long id) {
        LOG.debug("Request to get SettlementBatch : {}", id);
        return settlementBatchRepository.findOneWithEagerRelationships(id).map(settlementBatchMapper::toDto);
    }

    /**
     * Delete the settlementBatch by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete SettlementBatch : {}", id);
        settlementBatchRepository.deleteById(id);
    }
}
