package com.rnexchange.web.rest;

import com.rnexchange.repository.PositionRepository;
import com.rnexchange.service.PositionQueryService;
import com.rnexchange.service.PositionService;
import com.rnexchange.service.criteria.PositionCriteria;
import com.rnexchange.service.dto.PositionDTO;
import com.rnexchange.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.rnexchange.domain.Position}.
 */
@RestController
@RequestMapping("/api/positions")
public class PositionResource {

    private static final Logger LOG = LoggerFactory.getLogger(PositionResource.class);

    private static final String ENTITY_NAME = "position";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PositionService positionService;

    private final PositionRepository positionRepository;

    private final PositionQueryService positionQueryService;

    public PositionResource(
        PositionService positionService,
        PositionRepository positionRepository,
        PositionQueryService positionQueryService
    ) {
        this.positionService = positionService;
        this.positionRepository = positionRepository;
        this.positionQueryService = positionQueryService;
    }

    /**
     * {@code POST  /positions} : Create a new position.
     *
     * @param positionDTO the positionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new positionDTO, or with status {@code 400 (Bad Request)} if the position has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<PositionDTO> createPosition(@Valid @RequestBody PositionDTO positionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Position : {}", positionDTO);
        if (positionDTO.getId() != null) {
            throw new BadRequestAlertException("A new position cannot already have an ID", ENTITY_NAME, "idexists");
        }
        positionDTO = positionService.save(positionDTO);
        return ResponseEntity.created(new URI("/api/positions/" + positionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, positionDTO.getId().toString()))
            .body(positionDTO);
    }

    /**
     * {@code PUT  /positions/:id} : Updates an existing position.
     *
     * @param id the id of the positionDTO to save.
     * @param positionDTO the positionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated positionDTO,
     * or with status {@code 400 (Bad Request)} if the positionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the positionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PositionDTO> updatePosition(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody PositionDTO positionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Position : {}, {}", id, positionDTO);
        if (positionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, positionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!positionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        positionDTO = positionService.update(positionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, positionDTO.getId().toString()))
            .body(positionDTO);
    }

    /**
     * {@code PATCH  /positions/:id} : Partial updates given fields of an existing position, field will ignore if it is null
     *
     * @param id the id of the positionDTO to save.
     * @param positionDTO the positionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated positionDTO,
     * or with status {@code 400 (Bad Request)} if the positionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the positionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the positionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PositionDTO> partialUpdatePosition(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody PositionDTO positionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Position partially : {}, {}", id, positionDTO);
        if (positionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, positionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!positionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PositionDTO> result = positionService.partialUpdate(positionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, positionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /positions} : get all the positions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of positions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<PositionDTO>> getAllPositions(
        PositionCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Positions by criteria: {}", criteria);

        Page<PositionDTO> page = positionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /positions/count} : count all the positions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countPositions(PositionCriteria criteria) {
        LOG.debug("REST request to count Positions by criteria: {}", criteria);
        return ResponseEntity.ok().body(positionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /positions/:id} : get the "id" position.
     *
     * @param id the id of the positionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the positionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PositionDTO> getPosition(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Position : {}", id);
        Optional<PositionDTO> positionDTO = positionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(positionDTO);
    }

    /**
     * {@code DELETE  /positions/:id} : delete the "id" position.
     *
     * @param id the id of the positionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Position : {}", id);
        positionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
