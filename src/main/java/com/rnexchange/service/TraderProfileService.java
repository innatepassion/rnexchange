package com.rnexchange.service;

import com.rnexchange.domain.TraderProfile;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.service.dto.TraderProfileDTO;
import com.rnexchange.service.mapper.TraderProfileMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.TraderProfile}.
 */
@Service
@Transactional
public class TraderProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(TraderProfileService.class);

    private final TraderProfileRepository traderProfileRepository;

    private final TraderProfileMapper traderProfileMapper;

    public TraderProfileService(TraderProfileRepository traderProfileRepository, TraderProfileMapper traderProfileMapper) {
        this.traderProfileRepository = traderProfileRepository;
        this.traderProfileMapper = traderProfileMapper;
    }

    /**
     * Save a traderProfile.
     *
     * @param traderProfileDTO the entity to save.
     * @return the persisted entity.
     */
    public TraderProfileDTO save(TraderProfileDTO traderProfileDTO) {
        LOG.debug("Request to save TraderProfile : {}", traderProfileDTO);
        TraderProfile traderProfile = traderProfileMapper.toEntity(traderProfileDTO);
        traderProfile = traderProfileRepository.save(traderProfile);
        return traderProfileMapper.toDto(traderProfile);
    }

    /**
     * Update a traderProfile.
     *
     * @param traderProfileDTO the entity to save.
     * @return the persisted entity.
     */
    public TraderProfileDTO update(TraderProfileDTO traderProfileDTO) {
        LOG.debug("Request to update TraderProfile : {}", traderProfileDTO);
        TraderProfile traderProfile = traderProfileMapper.toEntity(traderProfileDTO);
        traderProfile = traderProfileRepository.save(traderProfile);
        return traderProfileMapper.toDto(traderProfile);
    }

    /**
     * Partially update a traderProfile.
     *
     * @param traderProfileDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<TraderProfileDTO> partialUpdate(TraderProfileDTO traderProfileDTO) {
        LOG.debug("Request to partially update TraderProfile : {}", traderProfileDTO);

        return traderProfileRepository
            .findById(traderProfileDTO.getId())
            .map(existingTraderProfile -> {
                traderProfileMapper.partialUpdate(existingTraderProfile, traderProfileDTO);

                return existingTraderProfile;
            })
            .map(traderProfileRepository::save)
            .map(traderProfileMapper::toDto);
    }

    /**
     * Get all the traderProfiles with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<TraderProfileDTO> findAllWithEagerRelationships(Pageable pageable) {
        return traderProfileRepository.findAllWithEagerRelationships(pageable).map(traderProfileMapper::toDto);
    }

    /**
     * Get one traderProfile by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TraderProfileDTO> findOne(Long id) {
        LOG.debug("Request to get TraderProfile : {}", id);
        return traderProfileRepository.findOneWithEagerRelationships(id).map(traderProfileMapper::toDto);
    }

    /**
     * Delete the traderProfile by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete TraderProfile : {}", id);
        traderProfileRepository.deleteById(id);
    }
}
