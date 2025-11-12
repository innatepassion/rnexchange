package com.rnexchange.web.rest;

import com.rnexchange.repository.SettlementBatchRepository;
import com.rnexchange.service.SettlementBatchQueryService;
import com.rnexchange.service.SettlementBatchService;
import com.rnexchange.service.criteria.SettlementBatchCriteria;
import com.rnexchange.service.dto.SettlementBatchDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.SettlementBatch}.
 */
@RestController
@RequestMapping("/api/settlement-batches")
public class SettlementBatchResource {

    private static final Logger LOG = LoggerFactory.getLogger(SettlementBatchResource.class);

    private static final String ENTITY_NAME = "settlementBatch";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SettlementBatchService settlementBatchService;

    private final SettlementBatchRepository settlementBatchRepository;

    private final SettlementBatchQueryService settlementBatchQueryService;

    public SettlementBatchResource(
        SettlementBatchService settlementBatchService,
        SettlementBatchRepository settlementBatchRepository,
        SettlementBatchQueryService settlementBatchQueryService
    ) {
        this.settlementBatchService = settlementBatchService;
        this.settlementBatchRepository = settlementBatchRepository;
        this.settlementBatchQueryService = settlementBatchQueryService;
    }

    /**
     * {@code POST  /settlement-batches} : Create a new settlementBatch.
     *
     * @param settlementBatchDTO the settlementBatchDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new settlementBatchDTO, or with status {@code 400 (Bad Request)} if the settlementBatch has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SettlementBatchDTO> createSettlementBatch(@Valid @RequestBody SettlementBatchDTO settlementBatchDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save SettlementBatch : {}", settlementBatchDTO);
        if (settlementBatchDTO.getId() != null) {
            throw new BadRequestAlertException("A new settlementBatch cannot already have an ID", ENTITY_NAME, "idexists");
        }
        settlementBatchDTO = settlementBatchService.save(settlementBatchDTO);
        return ResponseEntity.created(new URI("/api/settlement-batches/" + settlementBatchDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, settlementBatchDTO.getId().toString()))
            .body(settlementBatchDTO);
    }

    /**
     * {@code PUT  /settlement-batches/:id} : Updates an existing settlementBatch.
     *
     * @param id the id of the settlementBatchDTO to save.
     * @param settlementBatchDTO the settlementBatchDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated settlementBatchDTO,
     * or with status {@code 400 (Bad Request)} if the settlementBatchDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the settlementBatchDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SettlementBatchDTO> updateSettlementBatch(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SettlementBatchDTO settlementBatchDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update SettlementBatch : {}, {}", id, settlementBatchDTO);
        if (settlementBatchDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, settlementBatchDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!settlementBatchRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        settlementBatchDTO = settlementBatchService.update(settlementBatchDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, settlementBatchDTO.getId().toString()))
            .body(settlementBatchDTO);
    }

    /**
     * {@code PATCH  /settlement-batches/:id} : Partial updates given fields of an existing settlementBatch, field will ignore if it is null
     *
     * @param id the id of the settlementBatchDTO to save.
     * @param settlementBatchDTO the settlementBatchDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated settlementBatchDTO,
     * or with status {@code 400 (Bad Request)} if the settlementBatchDTO is not valid,
     * or with status {@code 404 (Not Found)} if the settlementBatchDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the settlementBatchDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SettlementBatchDTO> partialUpdateSettlementBatch(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SettlementBatchDTO settlementBatchDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update SettlementBatch partially : {}, {}", id, settlementBatchDTO);
        if (settlementBatchDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, settlementBatchDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!settlementBatchRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SettlementBatchDTO> result = settlementBatchService.partialUpdate(settlementBatchDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, settlementBatchDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /settlement-batches} : get all the settlementBatches.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of settlementBatches in body.
     */
    @GetMapping("")
    public ResponseEntity<List<SettlementBatchDTO>> getAllSettlementBatches(
        SettlementBatchCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get SettlementBatches by criteria: {}", criteria);

        Page<SettlementBatchDTO> page = settlementBatchQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /settlement-batches/count} : count all the settlementBatches.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSettlementBatches(SettlementBatchCriteria criteria) {
        LOG.debug("REST request to count SettlementBatches by criteria: {}", criteria);
        return ResponseEntity.ok().body(settlementBatchQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /settlement-batches/:id} : get the "id" settlementBatch.
     *
     * @param id the id of the settlementBatchDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the settlementBatchDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SettlementBatchDTO> getSettlementBatch(@PathVariable("id") Long id) {
        LOG.debug("REST request to get SettlementBatch : {}", id);
        Optional<SettlementBatchDTO> settlementBatchDTO = settlementBatchService.findOne(id);
        return ResponseUtil.wrapOrNotFound(settlementBatchDTO);
    }

    /**
     * {@code DELETE  /settlement-batches/:id} : delete the "id" settlementBatch.
     *
     * @param id the id of the settlementBatchDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSettlementBatch(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete SettlementBatch : {}", id);
        settlementBatchService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
