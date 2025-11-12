package com.rnexchange.service;

import com.rnexchange.domain.Instrument;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.service.dto.InstrumentDTO;
import com.rnexchange.service.mapper.InstrumentMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.Instrument}.
 */
@Service
@Transactional
public class InstrumentService {

    private static final Logger LOG = LoggerFactory.getLogger(InstrumentService.class);

    private final InstrumentRepository instrumentRepository;

    private final InstrumentMapper instrumentMapper;

    public InstrumentService(InstrumentRepository instrumentRepository, InstrumentMapper instrumentMapper) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentMapper = instrumentMapper;
    }

    /**
     * Save a instrument.
     *
     * @param instrumentDTO the entity to save.
     * @return the persisted entity.
     */
    public InstrumentDTO save(InstrumentDTO instrumentDTO) {
        LOG.debug("Request to save Instrument : {}", instrumentDTO);
        Instrument instrument = instrumentMapper.toEntity(instrumentDTO);
        instrument = instrumentRepository.save(instrument);
        return instrumentMapper.toDto(instrument);
    }

    /**
     * Update a instrument.
     *
     * @param instrumentDTO the entity to save.
     * @return the persisted entity.
     */
    public InstrumentDTO update(InstrumentDTO instrumentDTO) {
        LOG.debug("Request to update Instrument : {}", instrumentDTO);
        Instrument instrument = instrumentMapper.toEntity(instrumentDTO);
        instrument = instrumentRepository.save(instrument);
        return instrumentMapper.toDto(instrument);
    }

    /**
     * Partially update a instrument.
     *
     * @param instrumentDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<InstrumentDTO> partialUpdate(InstrumentDTO instrumentDTO) {
        LOG.debug("Request to partially update Instrument : {}", instrumentDTO);

        return instrumentRepository
            .findById(instrumentDTO.getId())
            .map(existingInstrument -> {
                instrumentMapper.partialUpdate(existingInstrument, instrumentDTO);

                return existingInstrument;
            })
            .map(instrumentRepository::save)
            .map(instrumentMapper::toDto);
    }

    /**
     * Get all the instruments with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<InstrumentDTO> findAllWithEagerRelationships(Pageable pageable) {
        return instrumentRepository.findAllWithEagerRelationships(pageable).map(instrumentMapper::toDto);
    }

    /**
     * Get one instrument by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<InstrumentDTO> findOne(Long id) {
        LOG.debug("Request to get Instrument : {}", id);
        return instrumentRepository.findOneWithEagerRelationships(id).map(instrumentMapper::toDto);
    }

    /**
     * Delete the instrument by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Instrument : {}", id);
        instrumentRepository.deleteById(id);
    }
}
