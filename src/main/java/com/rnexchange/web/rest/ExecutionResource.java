package com.rnexchange.web.rest;

import com.rnexchange.repository.ExecutionRepository;
import com.rnexchange.service.ExecutionQueryService;
import com.rnexchange.service.ExecutionService;
import com.rnexchange.service.criteria.ExecutionCriteria;
import com.rnexchange.service.dto.ExecutionDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.Execution}.
 */
@RestController
@RequestMapping("/api/executions")
public class ExecutionResource {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionResource.class);

    private static final String ENTITY_NAME = "execution";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ExecutionService executionService;

    private final ExecutionRepository executionRepository;

    private final ExecutionQueryService executionQueryService;

    public ExecutionResource(
        ExecutionService executionService,
        ExecutionRepository executionRepository,
        ExecutionQueryService executionQueryService
    ) {
        this.executionService = executionService;
        this.executionRepository = executionRepository;
        this.executionQueryService = executionQueryService;
    }

    /**
     * {@code POST  /executions} : Create a new execution.
     *
     * @param executionDTO the executionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new executionDTO, or with status {@code 400 (Bad Request)} if the execution has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ExecutionDTO> createExecution(@Valid @RequestBody ExecutionDTO executionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Execution : {}", executionDTO);
        if (executionDTO.getId() != null) {
            throw new BadRequestAlertException("A new execution cannot already have an ID", ENTITY_NAME, "idexists");
        }
        executionDTO = executionService.save(executionDTO);
        return ResponseEntity.created(new URI("/api/executions/" + executionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, executionDTO.getId().toString()))
            .body(executionDTO);
    }

    /**
     * {@code PUT  /executions/:id} : Updates an existing execution.
     *
     * @param id the id of the executionDTO to save.
     * @param executionDTO the executionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated executionDTO,
     * or with status {@code 400 (Bad Request)} if the executionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the executionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExecutionDTO> updateExecution(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ExecutionDTO executionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Execution : {}, {}", id, executionDTO);
        if (executionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, executionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!executionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        executionDTO = executionService.update(executionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, executionDTO.getId().toString()))
            .body(executionDTO);
    }

    /**
     * {@code PATCH  /executions/:id} : Partial updates given fields of an existing execution, field will ignore if it is null
     *
     * @param id the id of the executionDTO to save.
     * @param executionDTO the executionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated executionDTO,
     * or with status {@code 400 (Bad Request)} if the executionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the executionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the executionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ExecutionDTO> partialUpdateExecution(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ExecutionDTO executionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Execution partially : {}, {}", id, executionDTO);
        if (executionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, executionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!executionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ExecutionDTO> result = executionService.partialUpdate(executionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, executionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /executions} : get all the executions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of executions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ExecutionDTO>> getAllExecutions(
        ExecutionCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Executions by criteria: {}", criteria);

        Page<ExecutionDTO> page = executionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /executions/count} : count all the executions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countExecutions(ExecutionCriteria criteria) {
        LOG.debug("REST request to count Executions by criteria: {}", criteria);
        return ResponseEntity.ok().body(executionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /executions/:id} : get the "id" execution.
     *
     * @param id the id of the executionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the executionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExecutionDTO> getExecution(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Execution : {}", id);
        Optional<ExecutionDTO> executionDTO = executionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(executionDTO);
    }

    /**
     * {@code DELETE  /executions/:id} : delete the "id" execution.
     *
     * @param id the id of the executionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExecution(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Execution : {}", id);
        executionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
