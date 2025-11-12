package com.rnexchange.web.rest;

import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.service.TraderProfileQueryService;
import com.rnexchange.service.TraderProfileService;
import com.rnexchange.service.criteria.TraderProfileCriteria;
import com.rnexchange.service.dto.TraderProfileDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.TraderProfile}.
 */
@RestController
@RequestMapping("/api/trader-profiles")
public class TraderProfileResource {

    private static final Logger LOG = LoggerFactory.getLogger(TraderProfileResource.class);

    private static final String ENTITY_NAME = "traderProfile";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TraderProfileService traderProfileService;

    private final TraderProfileRepository traderProfileRepository;

    private final TraderProfileQueryService traderProfileQueryService;

    public TraderProfileResource(
        TraderProfileService traderProfileService,
        TraderProfileRepository traderProfileRepository,
        TraderProfileQueryService traderProfileQueryService
    ) {
        this.traderProfileService = traderProfileService;
        this.traderProfileRepository = traderProfileRepository;
        this.traderProfileQueryService = traderProfileQueryService;
    }

    /**
     * {@code POST  /trader-profiles} : Create a new traderProfile.
     *
     * @param traderProfileDTO the traderProfileDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new traderProfileDTO, or with status {@code 400 (Bad Request)} if the traderProfile has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TraderProfileDTO> createTraderProfile(@Valid @RequestBody TraderProfileDTO traderProfileDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save TraderProfile : {}", traderProfileDTO);
        if (traderProfileDTO.getId() != null) {
            throw new BadRequestAlertException("A new traderProfile cannot already have an ID", ENTITY_NAME, "idexists");
        }
        traderProfileDTO = traderProfileService.save(traderProfileDTO);
        return ResponseEntity.created(new URI("/api/trader-profiles/" + traderProfileDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, traderProfileDTO.getId().toString()))
            .body(traderProfileDTO);
    }

    /**
     * {@code PUT  /trader-profiles/:id} : Updates an existing traderProfile.
     *
     * @param id the id of the traderProfileDTO to save.
     * @param traderProfileDTO the traderProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated traderProfileDTO,
     * or with status {@code 400 (Bad Request)} if the traderProfileDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the traderProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TraderProfileDTO> updateTraderProfile(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TraderProfileDTO traderProfileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TraderProfile : {}, {}", id, traderProfileDTO);
        if (traderProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, traderProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!traderProfileRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        traderProfileDTO = traderProfileService.update(traderProfileDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, traderProfileDTO.getId().toString()))
            .body(traderProfileDTO);
    }

    /**
     * {@code PATCH  /trader-profiles/:id} : Partial updates given fields of an existing traderProfile, field will ignore if it is null
     *
     * @param id the id of the traderProfileDTO to save.
     * @param traderProfileDTO the traderProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated traderProfileDTO,
     * or with status {@code 400 (Bad Request)} if the traderProfileDTO is not valid,
     * or with status {@code 404 (Not Found)} if the traderProfileDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the traderProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TraderProfileDTO> partialUpdateTraderProfile(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TraderProfileDTO traderProfileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TraderProfile partially : {}, {}", id, traderProfileDTO);
        if (traderProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, traderProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!traderProfileRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TraderProfileDTO> result = traderProfileService.partialUpdate(traderProfileDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, traderProfileDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /trader-profiles} : get all the traderProfiles.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of traderProfiles in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TraderProfileDTO>> getAllTraderProfiles(
        TraderProfileCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get TraderProfiles by criteria: {}", criteria);

        Page<TraderProfileDTO> page = traderProfileQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /trader-profiles/count} : count all the traderProfiles.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countTraderProfiles(TraderProfileCriteria criteria) {
        LOG.debug("REST request to count TraderProfiles by criteria: {}", criteria);
        return ResponseEntity.ok().body(traderProfileQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /trader-profiles/:id} : get the "id" traderProfile.
     *
     * @param id the id of the traderProfileDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the traderProfileDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TraderProfileDTO> getTraderProfile(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TraderProfile : {}", id);
        Optional<TraderProfileDTO> traderProfileDTO = traderProfileService.findOne(id);
        return ResponseUtil.wrapOrNotFound(traderProfileDTO);
    }

    /**
     * {@code DELETE  /trader-profiles/:id} : delete the "id" traderProfile.
     *
     * @param id the id of the traderProfileDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraderProfile(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete TraderProfile : {}", id);
        traderProfileService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
