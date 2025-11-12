package com.rnexchange.web.rest;

import com.rnexchange.repository.CorporateActionRepository;
import com.rnexchange.service.CorporateActionQueryService;
import com.rnexchange.service.CorporateActionService;
import com.rnexchange.service.criteria.CorporateActionCriteria;
import com.rnexchange.service.dto.CorporateActionDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.CorporateAction}.
 */
@RestController
@RequestMapping("/api/corporate-actions")
public class CorporateActionResource {

    private static final Logger LOG = LoggerFactory.getLogger(CorporateActionResource.class);

    private static final String ENTITY_NAME = "corporateAction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CorporateActionService corporateActionService;

    private final CorporateActionRepository corporateActionRepository;

    private final CorporateActionQueryService corporateActionQueryService;

    public CorporateActionResource(
        CorporateActionService corporateActionService,
        CorporateActionRepository corporateActionRepository,
        CorporateActionQueryService corporateActionQueryService
    ) {
        this.corporateActionService = corporateActionService;
        this.corporateActionRepository = corporateActionRepository;
        this.corporateActionQueryService = corporateActionQueryService;
    }

    /**
     * {@code POST  /corporate-actions} : Create a new corporateAction.
     *
     * @param corporateActionDTO the corporateActionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new corporateActionDTO, or with status {@code 400 (Bad Request)} if the corporateAction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CorporateActionDTO> createCorporateAction(@Valid @RequestBody CorporateActionDTO corporateActionDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save CorporateAction : {}", corporateActionDTO);
        if (corporateActionDTO.getId() != null) {
            throw new BadRequestAlertException("A new corporateAction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        corporateActionDTO = corporateActionService.save(corporateActionDTO);
        return ResponseEntity.created(new URI("/api/corporate-actions/" + corporateActionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, corporateActionDTO.getId().toString()))
            .body(corporateActionDTO);
    }

    /**
     * {@code PUT  /corporate-actions/:id} : Updates an existing corporateAction.
     *
     * @param id the id of the corporateActionDTO to save.
     * @param corporateActionDTO the corporateActionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated corporateActionDTO,
     * or with status {@code 400 (Bad Request)} if the corporateActionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the corporateActionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CorporateActionDTO> updateCorporateAction(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CorporateActionDTO corporateActionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update CorporateAction : {}, {}", id, corporateActionDTO);
        if (corporateActionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, corporateActionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!corporateActionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        corporateActionDTO = corporateActionService.update(corporateActionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, corporateActionDTO.getId().toString()))
            .body(corporateActionDTO);
    }

    /**
     * {@code PATCH  /corporate-actions/:id} : Partial updates given fields of an existing corporateAction, field will ignore if it is null
     *
     * @param id the id of the corporateActionDTO to save.
     * @param corporateActionDTO the corporateActionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated corporateActionDTO,
     * or with status {@code 400 (Bad Request)} if the corporateActionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the corporateActionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the corporateActionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CorporateActionDTO> partialUpdateCorporateAction(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CorporateActionDTO corporateActionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update CorporateAction partially : {}, {}", id, corporateActionDTO);
        if (corporateActionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, corporateActionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!corporateActionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CorporateActionDTO> result = corporateActionService.partialUpdate(corporateActionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, corporateActionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /corporate-actions} : get all the corporateActions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of corporateActions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CorporateActionDTO>> getAllCorporateActions(
        CorporateActionCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get CorporateActions by criteria: {}", criteria);

        Page<CorporateActionDTO> page = corporateActionQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /corporate-actions/count} : count all the corporateActions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countCorporateActions(CorporateActionCriteria criteria) {
        LOG.debug("REST request to count CorporateActions by criteria: {}", criteria);
        return ResponseEntity.ok().body(corporateActionQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /corporate-actions/:id} : get the "id" corporateAction.
     *
     * @param id the id of the corporateActionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the corporateActionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CorporateActionDTO> getCorporateAction(@PathVariable("id") Long id) {
        LOG.debug("REST request to get CorporateAction : {}", id);
        Optional<CorporateActionDTO> corporateActionDTO = corporateActionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(corporateActionDTO);
    }

    /**
     * {@code DELETE  /corporate-actions/:id} : delete the "id" corporateAction.
     *
     * @param id the id of the corporateActionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCorporateAction(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete CorporateAction : {}", id);
        corporateActionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
