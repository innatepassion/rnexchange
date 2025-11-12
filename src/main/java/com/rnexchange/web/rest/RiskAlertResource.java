package com.rnexchange.web.rest;

import com.rnexchange.repository.RiskAlertRepository;
import com.rnexchange.service.RiskAlertQueryService;
import com.rnexchange.service.RiskAlertService;
import com.rnexchange.service.criteria.RiskAlertCriteria;
import com.rnexchange.service.dto.RiskAlertDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.RiskAlert}.
 */
@RestController
@RequestMapping("/api/risk-alerts")
public class RiskAlertResource {

    private static final Logger LOG = LoggerFactory.getLogger(RiskAlertResource.class);

    private static final String ENTITY_NAME = "riskAlert";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RiskAlertService riskAlertService;

    private final RiskAlertRepository riskAlertRepository;

    private final RiskAlertQueryService riskAlertQueryService;

    public RiskAlertResource(
        RiskAlertService riskAlertService,
        RiskAlertRepository riskAlertRepository,
        RiskAlertQueryService riskAlertQueryService
    ) {
        this.riskAlertService = riskAlertService;
        this.riskAlertRepository = riskAlertRepository;
        this.riskAlertQueryService = riskAlertQueryService;
    }

    /**
     * {@code POST  /risk-alerts} : Create a new riskAlert.
     *
     * @param riskAlertDTO the riskAlertDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new riskAlertDTO, or with status {@code 400 (Bad Request)} if the riskAlert has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<RiskAlertDTO> createRiskAlert(@Valid @RequestBody RiskAlertDTO riskAlertDTO) throws URISyntaxException {
        LOG.debug("REST request to save RiskAlert : {}", riskAlertDTO);
        if (riskAlertDTO.getId() != null) {
            throw new BadRequestAlertException("A new riskAlert cannot already have an ID", ENTITY_NAME, "idexists");
        }
        riskAlertDTO = riskAlertService.save(riskAlertDTO);
        return ResponseEntity.created(new URI("/api/risk-alerts/" + riskAlertDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, riskAlertDTO.getId().toString()))
            .body(riskAlertDTO);
    }

    /**
     * {@code PUT  /risk-alerts/:id} : Updates an existing riskAlert.
     *
     * @param id the id of the riskAlertDTO to save.
     * @param riskAlertDTO the riskAlertDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated riskAlertDTO,
     * or with status {@code 400 (Bad Request)} if the riskAlertDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the riskAlertDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RiskAlertDTO> updateRiskAlert(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody RiskAlertDTO riskAlertDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update RiskAlert : {}, {}", id, riskAlertDTO);
        if (riskAlertDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, riskAlertDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!riskAlertRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        riskAlertDTO = riskAlertService.update(riskAlertDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, riskAlertDTO.getId().toString()))
            .body(riskAlertDTO);
    }

    /**
     * {@code PATCH  /risk-alerts/:id} : Partial updates given fields of an existing riskAlert, field will ignore if it is null
     *
     * @param id the id of the riskAlertDTO to save.
     * @param riskAlertDTO the riskAlertDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated riskAlertDTO,
     * or with status {@code 400 (Bad Request)} if the riskAlertDTO is not valid,
     * or with status {@code 404 (Not Found)} if the riskAlertDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the riskAlertDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<RiskAlertDTO> partialUpdateRiskAlert(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody RiskAlertDTO riskAlertDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update RiskAlert partially : {}, {}", id, riskAlertDTO);
        if (riskAlertDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, riskAlertDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!riskAlertRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<RiskAlertDTO> result = riskAlertService.partialUpdate(riskAlertDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, riskAlertDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /risk-alerts} : get all the riskAlerts.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of riskAlerts in body.
     */
    @GetMapping("")
    public ResponseEntity<List<RiskAlertDTO>> getAllRiskAlerts(
        RiskAlertCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get RiskAlerts by criteria: {}", criteria);

        Page<RiskAlertDTO> page = riskAlertQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /risk-alerts/count} : count all the riskAlerts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countRiskAlerts(RiskAlertCriteria criteria) {
        LOG.debug("REST request to count RiskAlerts by criteria: {}", criteria);
        return ResponseEntity.ok().body(riskAlertQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /risk-alerts/:id} : get the "id" riskAlert.
     *
     * @param id the id of the riskAlertDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the riskAlertDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RiskAlertDTO> getRiskAlert(@PathVariable("id") Long id) {
        LOG.debug("REST request to get RiskAlert : {}", id);
        Optional<RiskAlertDTO> riskAlertDTO = riskAlertService.findOne(id);
        return ResponseUtil.wrapOrNotFound(riskAlertDTO);
    }

    /**
     * {@code DELETE  /risk-alerts/:id} : delete the "id" riskAlert.
     *
     * @param id the id of the riskAlertDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRiskAlert(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete RiskAlert : {}", id);
        riskAlertService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
