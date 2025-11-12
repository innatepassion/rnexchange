package com.rnexchange.web.rest;

import com.rnexchange.repository.MarginRuleRepository;
import com.rnexchange.service.MarginRuleQueryService;
import com.rnexchange.service.MarginRuleService;
import com.rnexchange.service.criteria.MarginRuleCriteria;
import com.rnexchange.service.dto.MarginRuleDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.MarginRule}.
 */
@RestController
@RequestMapping("/api/margin-rules")
public class MarginRuleResource {

    private static final Logger LOG = LoggerFactory.getLogger(MarginRuleResource.class);

    private static final String ENTITY_NAME = "marginRule";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MarginRuleService marginRuleService;

    private final MarginRuleRepository marginRuleRepository;

    private final MarginRuleQueryService marginRuleQueryService;

    public MarginRuleResource(
        MarginRuleService marginRuleService,
        MarginRuleRepository marginRuleRepository,
        MarginRuleQueryService marginRuleQueryService
    ) {
        this.marginRuleService = marginRuleService;
        this.marginRuleRepository = marginRuleRepository;
        this.marginRuleQueryService = marginRuleQueryService;
    }

    /**
     * {@code POST  /margin-rules} : Create a new marginRule.
     *
     * @param marginRuleDTO the marginRuleDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new marginRuleDTO, or with status {@code 400 (Bad Request)} if the marginRule has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MarginRuleDTO> createMarginRule(@Valid @RequestBody MarginRuleDTO marginRuleDTO) throws URISyntaxException {
        LOG.debug("REST request to save MarginRule : {}", marginRuleDTO);
        if (marginRuleDTO.getId() != null) {
            throw new BadRequestAlertException("A new marginRule cannot already have an ID", ENTITY_NAME, "idexists");
        }
        marginRuleDTO = marginRuleService.save(marginRuleDTO);
        return ResponseEntity.created(new URI("/api/margin-rules/" + marginRuleDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, marginRuleDTO.getId().toString()))
            .body(marginRuleDTO);
    }

    /**
     * {@code PUT  /margin-rules/:id} : Updates an existing marginRule.
     *
     * @param id the id of the marginRuleDTO to save.
     * @param marginRuleDTO the marginRuleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated marginRuleDTO,
     * or with status {@code 400 (Bad Request)} if the marginRuleDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the marginRuleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MarginRuleDTO> updateMarginRule(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MarginRuleDTO marginRuleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update MarginRule : {}, {}", id, marginRuleDTO);
        if (marginRuleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, marginRuleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!marginRuleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        marginRuleDTO = marginRuleService.update(marginRuleDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, marginRuleDTO.getId().toString()))
            .body(marginRuleDTO);
    }

    /**
     * {@code PATCH  /margin-rules/:id} : Partial updates given fields of an existing marginRule, field will ignore if it is null
     *
     * @param id the id of the marginRuleDTO to save.
     * @param marginRuleDTO the marginRuleDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated marginRuleDTO,
     * or with status {@code 400 (Bad Request)} if the marginRuleDTO is not valid,
     * or with status {@code 404 (Not Found)} if the marginRuleDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the marginRuleDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MarginRuleDTO> partialUpdateMarginRule(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody MarginRuleDTO marginRuleDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MarginRule partially : {}, {}", id, marginRuleDTO);
        if (marginRuleDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, marginRuleDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!marginRuleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MarginRuleDTO> result = marginRuleService.partialUpdate(marginRuleDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, marginRuleDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /margin-rules} : get all the marginRules.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of marginRules in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MarginRuleDTO>> getAllMarginRules(
        MarginRuleCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get MarginRules by criteria: {}", criteria);

        Page<MarginRuleDTO> page = marginRuleQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /margin-rules/count} : count all the marginRules.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countMarginRules(MarginRuleCriteria criteria) {
        LOG.debug("REST request to count MarginRules by criteria: {}", criteria);
        return ResponseEntity.ok().body(marginRuleQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /margin-rules/:id} : get the "id" marginRule.
     *
     * @param id the id of the marginRuleDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the marginRuleDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MarginRuleDTO> getMarginRule(@PathVariable("id") Long id) {
        LOG.debug("REST request to get MarginRule : {}", id);
        Optional<MarginRuleDTO> marginRuleDTO = marginRuleService.findOne(id);
        return ResponseUtil.wrapOrNotFound(marginRuleDTO);
    }

    /**
     * {@code DELETE  /margin-rules/:id} : delete the "id" marginRule.
     *
     * @param id the id of the marginRuleDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMarginRule(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete MarginRule : {}", id);
        marginRuleService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
