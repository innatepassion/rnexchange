package com.rnexchange.service;

import com.rnexchange.domain.Lot;
import com.rnexchange.repository.LotRepository;
import com.rnexchange.service.dto.LotDTO;
import com.rnexchange.service.mapper.LotMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.Lot}.
 */
@Service
@Transactional
public class LotService {

    private static final Logger LOG = LoggerFactory.getLogger(LotService.class);

    private final LotRepository lotRepository;

    private final LotMapper lotMapper;

    public LotService(LotRepository lotRepository, LotMapper lotMapper) {
        this.lotRepository = lotRepository;
        this.lotMapper = lotMapper;
    }

    /**
     * Save a lot.
     *
     * @param lotDTO the entity to save.
     * @return the persisted entity.
     */
    public LotDTO save(LotDTO lotDTO) {
        LOG.debug("Request to save Lot : {}", lotDTO);
        Lot lot = lotMapper.toEntity(lotDTO);
        lot = lotRepository.save(lot);
        return lotMapper.toDto(lot);
    }

    /**
     * Update a lot.
     *
     * @param lotDTO the entity to save.
     * @return the persisted entity.
     */
    public LotDTO update(LotDTO lotDTO) {
        LOG.debug("Request to update Lot : {}", lotDTO);
        Lot lot = lotMapper.toEntity(lotDTO);
        lot = lotRepository.save(lot);
        return lotMapper.toDto(lot);
    }

    /**
     * Partially update a lot.
     *
     * @param lotDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<LotDTO> partialUpdate(LotDTO lotDTO) {
        LOG.debug("Request to partially update Lot : {}", lotDTO);

        return lotRepository
            .findById(lotDTO.getId())
            .map(existingLot -> {
                lotMapper.partialUpdate(existingLot, lotDTO);

                return existingLot;
            })
            .map(lotRepository::save)
            .map(lotMapper::toDto);
    }

    /**
     * Get one lot by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<LotDTO> findOne(Long id) {
        LOG.debug("Request to get Lot : {}", id);
        return lotRepository.findById(id).map(lotMapper::toDto);
    }

    /**
     * Delete the lot by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Lot : {}", id);
        lotRepository.deleteById(id);
    }
}
