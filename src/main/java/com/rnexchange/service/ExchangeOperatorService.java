package com.rnexchange.service;

import com.rnexchange.domain.ExchangeOperator;
import com.rnexchange.repository.ExchangeOperatorRepository;
import com.rnexchange.service.dto.ExchangeOperatorDTO;
import com.rnexchange.service.mapper.ExchangeOperatorMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.ExchangeOperator}.
 */
@Service
@Transactional
public class ExchangeOperatorService {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeOperatorService.class);

    private final ExchangeOperatorRepository exchangeOperatorRepository;

    private final ExchangeOperatorMapper exchangeOperatorMapper;

    public ExchangeOperatorService(ExchangeOperatorRepository exchangeOperatorRepository, ExchangeOperatorMapper exchangeOperatorMapper) {
        this.exchangeOperatorRepository = exchangeOperatorRepository;
        this.exchangeOperatorMapper = exchangeOperatorMapper;
    }

    /**
     * Save a exchangeOperator.
     *
     * @param exchangeOperatorDTO the entity to save.
     * @return the persisted entity.
     */
    public ExchangeOperatorDTO save(ExchangeOperatorDTO exchangeOperatorDTO) {
        LOG.debug("Request to save ExchangeOperator : {}", exchangeOperatorDTO);
        ExchangeOperator exchangeOperator = exchangeOperatorMapper.toEntity(exchangeOperatorDTO);
        exchangeOperator = exchangeOperatorRepository.save(exchangeOperator);
        return exchangeOperatorMapper.toDto(exchangeOperator);
    }

    /**
     * Update a exchangeOperator.
     *
     * @param exchangeOperatorDTO the entity to save.
     * @return the persisted entity.
     */
    public ExchangeOperatorDTO update(ExchangeOperatorDTO exchangeOperatorDTO) {
        LOG.debug("Request to update ExchangeOperator : {}", exchangeOperatorDTO);
        ExchangeOperator exchangeOperator = exchangeOperatorMapper.toEntity(exchangeOperatorDTO);
        exchangeOperator = exchangeOperatorRepository.save(exchangeOperator);
        return exchangeOperatorMapper.toDto(exchangeOperator);
    }

    /**
     * Partially update a exchangeOperator.
     *
     * @param exchangeOperatorDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ExchangeOperatorDTO> partialUpdate(ExchangeOperatorDTO exchangeOperatorDTO) {
        LOG.debug("Request to partially update ExchangeOperator : {}", exchangeOperatorDTO);

        return exchangeOperatorRepository
            .findById(exchangeOperatorDTO.getId())
            .map(existingExchangeOperator -> {
                exchangeOperatorMapper.partialUpdate(existingExchangeOperator, exchangeOperatorDTO);

                return existingExchangeOperator;
            })
            .map(exchangeOperatorRepository::save)
            .map(exchangeOperatorMapper::toDto);
    }

    /**
     * Get all the exchangeOperators with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ExchangeOperatorDTO> findAllWithEagerRelationships(Pageable pageable) {
        return exchangeOperatorRepository.findAllWithEagerRelationships(pageable).map(exchangeOperatorMapper::toDto);
    }

    /**
     * Get one exchangeOperator by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ExchangeOperatorDTO> findOne(Long id) {
        LOG.debug("Request to get ExchangeOperator : {}", id);
        return exchangeOperatorRepository.findOneWithEagerRelationships(id).map(exchangeOperatorMapper::toDto);
    }

    /**
     * Delete the exchangeOperator by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ExchangeOperator : {}", id);
        exchangeOperatorRepository.deleteById(id);
    }
}
