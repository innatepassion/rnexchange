package com.rnexchange.service;

import com.rnexchange.domain.RiskAlert;
import com.rnexchange.repository.RiskAlertRepository;
import com.rnexchange.service.dto.RiskAlertDTO;
import com.rnexchange.service.mapper.RiskAlertMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.RiskAlert}.
 */
@Service
@Transactional
public class RiskAlertService {

    private static final Logger LOG = LoggerFactory.getLogger(RiskAlertService.class);

    private final RiskAlertRepository riskAlertRepository;

    private final RiskAlertMapper riskAlertMapper;

    public RiskAlertService(RiskAlertRepository riskAlertRepository, RiskAlertMapper riskAlertMapper) {
        this.riskAlertRepository = riskAlertRepository;
        this.riskAlertMapper = riskAlertMapper;
    }

    /**
     * Save a riskAlert.
     *
     * @param riskAlertDTO the entity to save.
     * @return the persisted entity.
     */
    public RiskAlertDTO save(RiskAlertDTO riskAlertDTO) {
        LOG.debug("Request to save RiskAlert : {}", riskAlertDTO);
        RiskAlert riskAlert = riskAlertMapper.toEntity(riskAlertDTO);
        riskAlert = riskAlertRepository.save(riskAlert);
        return riskAlertMapper.toDto(riskAlert);
    }

    /**
     * Update a riskAlert.
     *
     * @param riskAlertDTO the entity to save.
     * @return the persisted entity.
     */
    public RiskAlertDTO update(RiskAlertDTO riskAlertDTO) {
        LOG.debug("Request to update RiskAlert : {}", riskAlertDTO);
        RiskAlert riskAlert = riskAlertMapper.toEntity(riskAlertDTO);
        riskAlert = riskAlertRepository.save(riskAlert);
        return riskAlertMapper.toDto(riskAlert);
    }

    /**
     * Partially update a riskAlert.
     *
     * @param riskAlertDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<RiskAlertDTO> partialUpdate(RiskAlertDTO riskAlertDTO) {
        LOG.debug("Request to partially update RiskAlert : {}", riskAlertDTO);

        return riskAlertRepository
            .findById(riskAlertDTO.getId())
            .map(existingRiskAlert -> {
                riskAlertMapper.partialUpdate(existingRiskAlert, riskAlertDTO);

                return existingRiskAlert;
            })
            .map(riskAlertRepository::save)
            .map(riskAlertMapper::toDto);
    }

    /**
     * Get all the riskAlerts with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<RiskAlertDTO> findAllWithEagerRelationships(Pageable pageable) {
        return riskAlertRepository.findAllWithEagerRelationships(pageable).map(riskAlertMapper::toDto);
    }

    /**
     * Get one riskAlert by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RiskAlertDTO> findOne(Long id) {
        LOG.debug("Request to get RiskAlert : {}", id);
        return riskAlertRepository.findOneWithEagerRelationships(id).map(riskAlertMapper::toDto);
    }

    /**
     * Delete the riskAlert by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete RiskAlert : {}", id);
        riskAlertRepository.deleteById(id);
    }
}
