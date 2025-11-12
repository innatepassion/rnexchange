package com.rnexchange.service;

import com.rnexchange.domain.BrokerDesk;
import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.service.dto.BrokerDeskDTO;
import com.rnexchange.service.mapper.BrokerDeskMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.BrokerDesk}.
 */
@Service
@Transactional
public class BrokerDeskService {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerDeskService.class);

    private final BrokerDeskRepository brokerDeskRepository;

    private final BrokerDeskMapper brokerDeskMapper;

    public BrokerDeskService(BrokerDeskRepository brokerDeskRepository, BrokerDeskMapper brokerDeskMapper) {
        this.brokerDeskRepository = brokerDeskRepository;
        this.brokerDeskMapper = brokerDeskMapper;
    }

    /**
     * Save a brokerDesk.
     *
     * @param brokerDeskDTO the entity to save.
     * @return the persisted entity.
     */
    public BrokerDeskDTO save(BrokerDeskDTO brokerDeskDTO) {
        LOG.debug("Request to save BrokerDesk : {}", brokerDeskDTO);
        BrokerDesk brokerDesk = brokerDeskMapper.toEntity(brokerDeskDTO);
        brokerDesk = brokerDeskRepository.save(brokerDesk);
        return brokerDeskMapper.toDto(brokerDesk);
    }

    /**
     * Update a brokerDesk.
     *
     * @param brokerDeskDTO the entity to save.
     * @return the persisted entity.
     */
    public BrokerDeskDTO update(BrokerDeskDTO brokerDeskDTO) {
        LOG.debug("Request to update BrokerDesk : {}", brokerDeskDTO);
        BrokerDesk brokerDesk = brokerDeskMapper.toEntity(brokerDeskDTO);
        brokerDesk = brokerDeskRepository.save(brokerDesk);
        return brokerDeskMapper.toDto(brokerDesk);
    }

    /**
     * Partially update a brokerDesk.
     *
     * @param brokerDeskDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<BrokerDeskDTO> partialUpdate(BrokerDeskDTO brokerDeskDTO) {
        LOG.debug("Request to partially update BrokerDesk : {}", brokerDeskDTO);

        return brokerDeskRepository
            .findById(brokerDeskDTO.getId())
            .map(existingBrokerDesk -> {
                brokerDeskMapper.partialUpdate(existingBrokerDesk, brokerDeskDTO);

                return existingBrokerDesk;
            })
            .map(brokerDeskRepository::save)
            .map(brokerDeskMapper::toDto);
    }

    /**
     * Get all the brokerDesks with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<BrokerDeskDTO> findAllWithEagerRelationships(Pageable pageable) {
        return brokerDeskRepository.findAllWithEagerRelationships(pageable).map(brokerDeskMapper::toDto);
    }

    /**
     * Get one brokerDesk by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<BrokerDeskDTO> findOne(Long id) {
        LOG.debug("Request to get BrokerDesk : {}", id);
        return brokerDeskRepository.findOneWithEagerRelationships(id).map(brokerDeskMapper::toDto);
    }

    /**
     * Delete the brokerDesk by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete BrokerDesk : {}", id);
        brokerDeskRepository.deleteById(id);
    }
}
