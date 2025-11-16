package com.rnexchange.service;

import com.rnexchange.domain.DailySettlementPrice;
import com.rnexchange.repository.DailySettlementPriceRepository;
import com.rnexchange.service.dto.DailySettlementPriceDTO;
import com.rnexchange.service.mapper.DailySettlementPriceMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.DailySettlementPrice}.
 */
@Service
@Transactional
public class DailySettlementPriceService {

    private static final Logger LOG = LoggerFactory.getLogger(DailySettlementPriceService.class);

    private final DailySettlementPriceRepository dailySettlementPriceRepository;

    private final DailySettlementPriceMapper dailySettlementPriceMapper;

    public DailySettlementPriceService(
        DailySettlementPriceRepository dailySettlementPriceRepository,
        DailySettlementPriceMapper dailySettlementPriceMapper
    ) {
        this.dailySettlementPriceRepository = dailySettlementPriceRepository;
        this.dailySettlementPriceMapper = dailySettlementPriceMapper;
    }

    /**
     * Save a dailySettlementPrice.
     *
     * @param dailySettlementPriceDTO the entity to save.
     * @return the persisted entity.
     */
    public DailySettlementPriceDTO save(DailySettlementPriceDTO dailySettlementPriceDTO) {
        LOG.debug("Request to save DailySettlementPrice : {}", dailySettlementPriceDTO);
        DailySettlementPrice dailySettlementPrice = dailySettlementPriceMapper.toEntity(dailySettlementPriceDTO);
        dailySettlementPrice = dailySettlementPriceRepository.save(dailySettlementPrice);
        return dailySettlementPriceMapper.toDto(dailySettlementPrice);
    }

    /**
     * Update a dailySettlementPrice.
     *
     * @param dailySettlementPriceDTO the entity to save.
     * @return the persisted entity.
     */
    public DailySettlementPriceDTO update(DailySettlementPriceDTO dailySettlementPriceDTO) {
        LOG.debug("Request to update DailySettlementPrice : {}", dailySettlementPriceDTO);
        DailySettlementPrice dailySettlementPrice = dailySettlementPriceMapper.toEntity(dailySettlementPriceDTO);
        dailySettlementPrice = dailySettlementPriceRepository.save(dailySettlementPrice);
        return dailySettlementPriceMapper.toDto(dailySettlementPrice);
    }

    /**
     * Partially update a dailySettlementPrice.
     *
     * @param dailySettlementPriceDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DailySettlementPriceDTO> partialUpdate(DailySettlementPriceDTO dailySettlementPriceDTO) {
        LOG.debug("Request to partially update DailySettlementPrice : {}", dailySettlementPriceDTO);

        return dailySettlementPriceRepository
            .findById(dailySettlementPriceDTO.getId())
            .map(existingDailySettlementPrice -> {
                dailySettlementPriceMapper.partialUpdate(existingDailySettlementPrice, dailySettlementPriceDTO);

                return existingDailySettlementPrice;
            })
            .map(dailySettlementPriceRepository::save)
            .map(dailySettlementPriceMapper::toDto);
    }

    /**
     * Get all the dailySettlementPrices with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<DailySettlementPriceDTO> findAllWithEagerRelationships(Pageable pageable) {
        return dailySettlementPriceRepository.findAllWithEagerRelationships(pageable).map(dailySettlementPriceMapper::toDto);
    }

    /**
     * Get the latest available daily settlement price for an instrument.
     *
     * <p>
     * This helper is primarily used by the trading matching engine (T010) to obtain
     * a single "last known price" for MARKET and LIMIT order evaluation.
     * </p>
     *
     * @param instrumentId    the JPA identifier of the instrument (may be {@code null})
     * @param instrumentSymbol fallback symbol to use when {@code instrumentId} is unavailable
     * @return optional DTO containing the most recent settlement price, if any
     */
    @Transactional(readOnly = true)
    public Optional<DailySettlementPriceDTO> findLatestByInstrument(Long instrumentId, String instrumentSymbol) {
        LOG.debug("Request to get latest DailySettlementPrice for instrument: id={}, symbol={}", instrumentId, instrumentSymbol);

        // Prefer lookup by instrument id when available
        if (instrumentId != null) {
            Optional<DailySettlementPriceDTO> byId = dailySettlementPriceRepository
                .findFirstByInstrument_IdOrderByRefDateDesc(instrumentId)
                .map(dailySettlementPriceMapper::toDto);
            if (byId.isPresent()) {
                return byId;
            }
        }

        // Fallback to symbol-based lookup (useful when seed data only populates instrument_symbol)
        if (instrumentSymbol != null && !instrumentSymbol.isBlank()) {
            return dailySettlementPriceRepository
                .findFirstByInstrumentSymbolOrderByRefDateDesc(instrumentSymbol)
                .map(dailySettlementPriceMapper::toDto);
        }

        return Optional.empty();
    }

    /**
     * Get one dailySettlementPrice by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DailySettlementPriceDTO> findOne(Long id) {
        LOG.debug("Request to get DailySettlementPrice : {}", id);
        return dailySettlementPriceRepository.findOneWithEagerRelationships(id).map(dailySettlementPriceMapper::toDto);
    }

    /**
     * Delete the dailySettlementPrice by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete DailySettlementPrice : {}", id);
        dailySettlementPriceRepository.deleteById(id);
    }
}
