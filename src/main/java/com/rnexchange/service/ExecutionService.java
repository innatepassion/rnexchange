package com.rnexchange.service;

import com.rnexchange.domain.Execution;
import com.rnexchange.repository.ExecutionRepository;
import com.rnexchange.service.dto.ExecutionDTO;
import com.rnexchange.service.mapper.ExecutionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.Execution}.
 */
@Service
@Transactional
public class ExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionService.class);

    private final ExecutionRepository executionRepository;

    private final ExecutionMapper executionMapper;

    public ExecutionService(ExecutionRepository executionRepository, ExecutionMapper executionMapper) {
        this.executionRepository = executionRepository;
        this.executionMapper = executionMapper;
    }

    /**
     * Save a execution.
     *
     * @param executionDTO the entity to save.
     * @return the persisted entity.
     */
    public ExecutionDTO save(ExecutionDTO executionDTO) {
        LOG.debug("Request to save Execution : {}", executionDTO);
        Execution execution = executionMapper.toEntity(executionDTO);
        execution = executionRepository.save(execution);
        return executionMapper.toDto(execution);
    }

    /**
     * Update a execution.
     *
     * @param executionDTO the entity to save.
     * @return the persisted entity.
     */
    public ExecutionDTO update(ExecutionDTO executionDTO) {
        LOG.debug("Request to update Execution : {}", executionDTO);
        Execution execution = executionMapper.toEntity(executionDTO);
        execution = executionRepository.save(execution);
        return executionMapper.toDto(execution);
    }

    /**
     * Partially update a execution.
     *
     * @param executionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ExecutionDTO> partialUpdate(ExecutionDTO executionDTO) {
        LOG.debug("Request to partially update Execution : {}", executionDTO);

        return executionRepository
            .findById(executionDTO.getId())
            .map(existingExecution -> {
                executionMapper.partialUpdate(existingExecution, executionDTO);

                return existingExecution;
            })
            .map(executionRepository::save)
            .map(executionMapper::toDto);
    }

    /**
     * Get one execution by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ExecutionDTO> findOne(Long id) {
        LOG.debug("Request to get Execution : {}", id);
        return executionRepository.findById(id).map(executionMapper::toDto);
    }

    /**
     * Delete the execution by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Execution : {}", id);
        executionRepository.deleteById(id);
    }
}
