package com.rnexchange.service;

import com.rnexchange.domain.Contract;
import com.rnexchange.repository.ContractRepository;
import com.rnexchange.service.dto.ContractDTO;
import com.rnexchange.service.mapper.ContractMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.Contract}.
 */
@Service
@Transactional
public class ContractService {

    private static final Logger LOG = LoggerFactory.getLogger(ContractService.class);

    private final ContractRepository contractRepository;

    private final ContractMapper contractMapper;

    public ContractService(ContractRepository contractRepository, ContractMapper contractMapper) {
        this.contractRepository = contractRepository;
        this.contractMapper = contractMapper;
    }

    /**
     * Save a contract.
     *
     * @param contractDTO the entity to save.
     * @return the persisted entity.
     */
    public ContractDTO save(ContractDTO contractDTO) {
        LOG.debug("Request to save Contract : {}", contractDTO);
        Contract contract = contractMapper.toEntity(contractDTO);
        contract = contractRepository.save(contract);
        return contractMapper.toDto(contract);
    }

    /**
     * Update a contract.
     *
     * @param contractDTO the entity to save.
     * @return the persisted entity.
     */
    public ContractDTO update(ContractDTO contractDTO) {
        LOG.debug("Request to update Contract : {}", contractDTO);
        Contract contract = contractMapper.toEntity(contractDTO);
        contract = contractRepository.save(contract);
        return contractMapper.toDto(contract);
    }

    /**
     * Partially update a contract.
     *
     * @param contractDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ContractDTO> partialUpdate(ContractDTO contractDTO) {
        LOG.debug("Request to partially update Contract : {}", contractDTO);

        return contractRepository
            .findById(contractDTO.getId())
            .map(existingContract -> {
                contractMapper.partialUpdate(existingContract, contractDTO);

                return existingContract;
            })
            .map(contractRepository::save)
            .map(contractMapper::toDto);
    }

    /**
     * Get all the contracts with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ContractDTO> findAllWithEagerRelationships(Pageable pageable) {
        return contractRepository.findAllWithEagerRelationships(pageable).map(contractMapper::toDto);
    }

    /**
     * Get one contract by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ContractDTO> findOne(Long id) {
        LOG.debug("Request to get Contract : {}", id);
        return contractRepository.findOneWithEagerRelationships(id).map(contractMapper::toDto);
    }

    /**
     * Delete the contract by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Contract : {}", id);
        contractRepository.deleteById(id);
    }
}
