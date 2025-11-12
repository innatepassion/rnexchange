package com.rnexchange.service;

import com.rnexchange.domain.Position;
import com.rnexchange.repository.PositionRepository;
import com.rnexchange.service.dto.PositionDTO;
import com.rnexchange.service.mapper.PositionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.rnexchange.domain.Position}.
 */
@Service
@Transactional
public class PositionService {

    private static final Logger LOG = LoggerFactory.getLogger(PositionService.class);

    private final PositionRepository positionRepository;

    private final PositionMapper positionMapper;

    public PositionService(PositionRepository positionRepository, PositionMapper positionMapper) {
        this.positionRepository = positionRepository;
        this.positionMapper = positionMapper;
    }

    /**
     * Save a position.
     *
     * @param positionDTO the entity to save.
     * @return the persisted entity.
     */
    public PositionDTO save(PositionDTO positionDTO) {
        LOG.debug("Request to save Position : {}", positionDTO);
        Position position = positionMapper.toEntity(positionDTO);
        position = positionRepository.save(position);
        return positionMapper.toDto(position);
    }

    /**
     * Update a position.
     *
     * @param positionDTO the entity to save.
     * @return the persisted entity.
     */
    public PositionDTO update(PositionDTO positionDTO) {
        LOG.debug("Request to update Position : {}", positionDTO);
        Position position = positionMapper.toEntity(positionDTO);
        position = positionRepository.save(position);
        return positionMapper.toDto(position);
    }

    /**
     * Partially update a position.
     *
     * @param positionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PositionDTO> partialUpdate(PositionDTO positionDTO) {
        LOG.debug("Request to partially update Position : {}", positionDTO);

        return positionRepository
            .findById(positionDTO.getId())
            .map(existingPosition -> {
                positionMapper.partialUpdate(existingPosition, positionDTO);

                return existingPosition;
            })
            .map(positionRepository::save)
            .map(positionMapper::toDto);
    }

    /**
     * Get all the positions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<PositionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return positionRepository.findAllWithEagerRelationships(pageable).map(positionMapper::toDto);
    }

    /**
     * Get one position by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PositionDTO> findOne(Long id) {
        LOG.debug("Request to get Position : {}", id);
        return positionRepository.findOneWithEagerRelationships(id).map(positionMapper::toDto);
    }

    /**
     * Delete the position by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Position : {}", id);
        positionRepository.deleteById(id);
    }
}
