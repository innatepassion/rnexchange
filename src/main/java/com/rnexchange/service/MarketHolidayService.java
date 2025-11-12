package com.rnexchange.service;

import com.rnexchange.domain.MarketHoliday;
import com.rnexchange.repository.MarketHolidayRepository;
import com.rnexchange.service.dto.MarketHolidayDTO;
import com.rnexchange.service.mapper.MarketHolidayMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.MarketHoliday}.
 */
@Service
@Transactional
public class MarketHolidayService {

    private static final Logger LOG = LoggerFactory.getLogger(MarketHolidayService.class);

    private final MarketHolidayRepository marketHolidayRepository;

    private final MarketHolidayMapper marketHolidayMapper;

    public MarketHolidayService(MarketHolidayRepository marketHolidayRepository, MarketHolidayMapper marketHolidayMapper) {
        this.marketHolidayRepository = marketHolidayRepository;
        this.marketHolidayMapper = marketHolidayMapper;
    }

    /**
     * Save a marketHoliday.
     *
     * @param marketHolidayDTO the entity to save.
     * @return the persisted entity.
     */
    public MarketHolidayDTO save(MarketHolidayDTO marketHolidayDTO) {
        LOG.debug("Request to save MarketHoliday : {}", marketHolidayDTO);
        MarketHoliday marketHoliday = marketHolidayMapper.toEntity(marketHolidayDTO);
        marketHoliday = marketHolidayRepository.save(marketHoliday);
        return marketHolidayMapper.toDto(marketHoliday);
    }

    /**
     * Update a marketHoliday.
     *
     * @param marketHolidayDTO the entity to save.
     * @return the persisted entity.
     */
    public MarketHolidayDTO update(MarketHolidayDTO marketHolidayDTO) {
        LOG.debug("Request to update MarketHoliday : {}", marketHolidayDTO);
        MarketHoliday marketHoliday = marketHolidayMapper.toEntity(marketHolidayDTO);
        marketHoliday = marketHolidayRepository.save(marketHoliday);
        return marketHolidayMapper.toDto(marketHoliday);
    }

    /**
     * Partially update a marketHoliday.
     *
     * @param marketHolidayDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MarketHolidayDTO> partialUpdate(MarketHolidayDTO marketHolidayDTO) {
        LOG.debug("Request to partially update MarketHoliday : {}", marketHolidayDTO);

        return marketHolidayRepository
            .findById(marketHolidayDTO.getId())
            .map(existingMarketHoliday -> {
                marketHolidayMapper.partialUpdate(existingMarketHoliday, marketHolidayDTO);

                return existingMarketHoliday;
            })
            .map(marketHolidayRepository::save)
            .map(marketHolidayMapper::toDto);
    }

    /**
     * Get all the marketHolidays with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<MarketHolidayDTO> findAllWithEagerRelationships(Pageable pageable) {
        return marketHolidayRepository.findAllWithEagerRelationships(pageable).map(marketHolidayMapper::toDto);
    }

    /**
     * Get one marketHoliday by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MarketHolidayDTO> findOne(Long id) {
        LOG.debug("Request to get MarketHoliday : {}", id);
        return marketHolidayRepository.findOneWithEagerRelationships(id).map(marketHolidayMapper::toDto);
    }

    /**
     * Delete the marketHoliday by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete MarketHoliday : {}", id);
        marketHolidayRepository.deleteById(id);
    }
}
