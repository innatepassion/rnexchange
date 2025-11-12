package com.rnexchange.service;

import com.rnexchange.domain.ExchangeIntegration;
import com.rnexchange.repository.ExchangeIntegrationRepository;
import com.rnexchange.service.dto.ExchangeIntegrationDTO;
import com.rnexchange.service.mapper.ExchangeIntegrationMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.ExchangeIntegration}.
 */
@Service
@Transactional
public class ExchangeIntegrationService {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeIntegrationService.class);

    private final ExchangeIntegrationRepository exchangeIntegrationRepository;

    private final ExchangeIntegrationMapper exchangeIntegrationMapper;

    public ExchangeIntegrationService(
        ExchangeIntegrationRepository exchangeIntegrationRepository,
        ExchangeIntegrationMapper exchangeIntegrationMapper
    ) {
        this.exchangeIntegrationRepository = exchangeIntegrationRepository;
        this.exchangeIntegrationMapper = exchangeIntegrationMapper;
    }

    /**
     * Save a exchangeIntegration.
     *
     * @param exchangeIntegrationDTO the entity to save.
     * @return the persisted entity.
     */
    public ExchangeIntegrationDTO save(ExchangeIntegrationDTO exchangeIntegrationDTO) {
        LOG.debug("Request to save ExchangeIntegration : {}", exchangeIntegrationDTO);
        ExchangeIntegration exchangeIntegration = exchangeIntegrationMapper.toEntity(exchangeIntegrationDTO);
        exchangeIntegration = exchangeIntegrationRepository.save(exchangeIntegration);
        return exchangeIntegrationMapper.toDto(exchangeIntegration);
    }

    /**
     * Update a exchangeIntegration.
     *
     * @param exchangeIntegrationDTO the entity to save.
     * @return the persisted entity.
     */
    public ExchangeIntegrationDTO update(ExchangeIntegrationDTO exchangeIntegrationDTO) {
        LOG.debug("Request to update ExchangeIntegration : {}", exchangeIntegrationDTO);
        ExchangeIntegration exchangeIntegration = exchangeIntegrationMapper.toEntity(exchangeIntegrationDTO);
        exchangeIntegration = exchangeIntegrationRepository.save(exchangeIntegration);
        return exchangeIntegrationMapper.toDto(exchangeIntegration);
    }

    /**
     * Partially update a exchangeIntegration.
     *
     * @param exchangeIntegrationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ExchangeIntegrationDTO> partialUpdate(ExchangeIntegrationDTO exchangeIntegrationDTO) {
        LOG.debug("Request to partially update ExchangeIntegration : {}", exchangeIntegrationDTO);

        return exchangeIntegrationRepository
            .findById(exchangeIntegrationDTO.getId())
            .map(existingExchangeIntegration -> {
                exchangeIntegrationMapper.partialUpdate(existingExchangeIntegration, exchangeIntegrationDTO);

                return existingExchangeIntegration;
            })
            .map(exchangeIntegrationRepository::save)
            .map(exchangeIntegrationMapper::toDto);
    }

    /**
     * Get all the exchangeIntegrations with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ExchangeIntegrationDTO> findAllWithEagerRelationships(Pageable pageable) {
        return exchangeIntegrationRepository.findAllWithEagerRelationships(pageable).map(exchangeIntegrationMapper::toDto);
    }

    /**
     * Get one exchangeIntegration by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ExchangeIntegrationDTO> findOne(Long id) {
        LOG.debug("Request to get ExchangeIntegration : {}", id);
        return exchangeIntegrationRepository.findOneWithEagerRelationships(id).map(exchangeIntegrationMapper::toDto);
    }

    /**
     * Delete the exchangeIntegration by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ExchangeIntegration : {}", id);
        exchangeIntegrationRepository.deleteById(id);
    }
}
