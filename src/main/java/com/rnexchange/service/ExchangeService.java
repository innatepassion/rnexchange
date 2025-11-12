package com.rnexchange.service;

import com.rnexchange.domain.Exchange;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.service.dto.ExchangeDTO;
import com.rnexchange.service.mapper.ExchangeMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.Exchange}.
 */
@Service
@Transactional
public class ExchangeService {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeService.class);

    private final ExchangeRepository exchangeRepository;

    private final ExchangeMapper exchangeMapper;

    public ExchangeService(ExchangeRepository exchangeRepository, ExchangeMapper exchangeMapper) {
        this.exchangeRepository = exchangeRepository;
        this.exchangeMapper = exchangeMapper;
    }

    /**
     * Save a exchange.
     *
     * @param exchangeDTO the entity to save.
     * @return the persisted entity.
     */
    public ExchangeDTO save(ExchangeDTO exchangeDTO) {
        LOG.debug("Request to save Exchange : {}", exchangeDTO);
        Exchange exchange = exchangeMapper.toEntity(exchangeDTO);
        exchange = exchangeRepository.save(exchange);
        return exchangeMapper.toDto(exchange);
    }

    /**
     * Update a exchange.
     *
     * @param exchangeDTO the entity to save.
     * @return the persisted entity.
     */
    public ExchangeDTO update(ExchangeDTO exchangeDTO) {
        LOG.debug("Request to update Exchange : {}", exchangeDTO);
        Exchange exchange = exchangeMapper.toEntity(exchangeDTO);
        exchange = exchangeRepository.save(exchange);
        return exchangeMapper.toDto(exchange);
    }

    /**
     * Partially update a exchange.
     *
     * @param exchangeDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ExchangeDTO> partialUpdate(ExchangeDTO exchangeDTO) {
        LOG.debug("Request to partially update Exchange : {}", exchangeDTO);

        return exchangeRepository
            .findById(exchangeDTO.getId())
            .map(existingExchange -> {
                exchangeMapper.partialUpdate(existingExchange, exchangeDTO);

                return existingExchange;
            })
            .map(exchangeRepository::save)
            .map(exchangeMapper::toDto);
    }

    /**
     * Get one exchange by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ExchangeDTO> findOne(Long id) {
        LOG.debug("Request to get Exchange : {}", id);
        return exchangeRepository.findById(id).map(exchangeMapper::toDto);
    }

    /**
     * Delete the exchange by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Exchange : {}", id);
        exchangeRepository.deleteById(id);
    }
}
