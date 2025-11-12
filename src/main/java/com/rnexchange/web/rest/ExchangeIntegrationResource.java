package com.rnexchange.web.rest;

import com.rnexchange.repository.ExchangeIntegrationRepository;
import com.rnexchange.service.ExchangeIntegrationQueryService;
import com.rnexchange.service.ExchangeIntegrationService;
import com.rnexchange.service.criteria.ExchangeIntegrationCriteria;
import com.rnexchange.service.dto.ExchangeIntegrationDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.ExchangeIntegration}.
 */
@RestController
@RequestMapping("/api/exchange-integrations")
public class ExchangeIntegrationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeIntegrationResource.class);

    private static final String ENTITY_NAME = "exchangeIntegration";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ExchangeIntegrationService exchangeIntegrationService;

    private final ExchangeIntegrationRepository exchangeIntegrationRepository;

    private final ExchangeIntegrationQueryService exchangeIntegrationQueryService;

    public ExchangeIntegrationResource(
        ExchangeIntegrationService exchangeIntegrationService,
        ExchangeIntegrationRepository exchangeIntegrationRepository,
        ExchangeIntegrationQueryService exchangeIntegrationQueryService
    ) {
        this.exchangeIntegrationService = exchangeIntegrationService;
        this.exchangeIntegrationRepository = exchangeIntegrationRepository;
        this.exchangeIntegrationQueryService = exchangeIntegrationQueryService;
    }

    /**
     * {@code POST  /exchange-integrations} : Create a new exchangeIntegration.
     *
     * @param exchangeIntegrationDTO the exchangeIntegrationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new exchangeIntegrationDTO, or with status {@code 400 (Bad Request)} if the exchangeIntegration has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ExchangeIntegrationDTO> createExchangeIntegration(
        @Valid @RequestBody ExchangeIntegrationDTO exchangeIntegrationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save ExchangeIntegration : {}", exchangeIntegrationDTO);
        if (exchangeIntegrationDTO.getId() != null) {
            throw new BadRequestAlertException("A new exchangeIntegration cannot already have an ID", ENTITY_NAME, "idexists");
        }
        exchangeIntegrationDTO = exchangeIntegrationService.save(exchangeIntegrationDTO);
        return ResponseEntity.created(new URI("/api/exchange-integrations/" + exchangeIntegrationDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, exchangeIntegrationDTO.getId().toString()))
            .body(exchangeIntegrationDTO);
    }

    /**
     * {@code PUT  /exchange-integrations/:id} : Updates an existing exchangeIntegration.
     *
     * @param id the id of the exchangeIntegrationDTO to save.
     * @param exchangeIntegrationDTO the exchangeIntegrationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated exchangeIntegrationDTO,
     * or with status {@code 400 (Bad Request)} if the exchangeIntegrationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the exchangeIntegrationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExchangeIntegrationDTO> updateExchangeIntegration(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ExchangeIntegrationDTO exchangeIntegrationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ExchangeIntegration : {}, {}", id, exchangeIntegrationDTO);
        if (exchangeIntegrationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, exchangeIntegrationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!exchangeIntegrationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        exchangeIntegrationDTO = exchangeIntegrationService.update(exchangeIntegrationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, exchangeIntegrationDTO.getId().toString()))
            .body(exchangeIntegrationDTO);
    }

    /**
     * {@code PATCH  /exchange-integrations/:id} : Partial updates given fields of an existing exchangeIntegration, field will ignore if it is null
     *
     * @param id the id of the exchangeIntegrationDTO to save.
     * @param exchangeIntegrationDTO the exchangeIntegrationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated exchangeIntegrationDTO,
     * or with status {@code 400 (Bad Request)} if the exchangeIntegrationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the exchangeIntegrationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the exchangeIntegrationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ExchangeIntegrationDTO> partialUpdateExchangeIntegration(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ExchangeIntegrationDTO exchangeIntegrationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ExchangeIntegration partially : {}, {}", id, exchangeIntegrationDTO);
        if (exchangeIntegrationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, exchangeIntegrationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!exchangeIntegrationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ExchangeIntegrationDTO> result = exchangeIntegrationService.partialUpdate(exchangeIntegrationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, exchangeIntegrationDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /exchange-integrations} : get all the exchangeIntegrations.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of exchangeIntegrations in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ExchangeIntegrationDTO>> getAllExchangeIntegrations(
        ExchangeIntegrationCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get ExchangeIntegrations by criteria: {}", criteria);

        Page<ExchangeIntegrationDTO> page = exchangeIntegrationQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /exchange-integrations/count} : count all the exchangeIntegrations.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countExchangeIntegrations(ExchangeIntegrationCriteria criteria) {
        LOG.debug("REST request to count ExchangeIntegrations by criteria: {}", criteria);
        return ResponseEntity.ok().body(exchangeIntegrationQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /exchange-integrations/:id} : get the "id" exchangeIntegration.
     *
     * @param id the id of the exchangeIntegrationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the exchangeIntegrationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExchangeIntegrationDTO> getExchangeIntegration(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ExchangeIntegration : {}", id);
        Optional<ExchangeIntegrationDTO> exchangeIntegrationDTO = exchangeIntegrationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(exchangeIntegrationDTO);
    }

    /**
     * {@code DELETE  /exchange-integrations/:id} : delete the "id" exchangeIntegration.
     *
     * @param id the id of the exchangeIntegrationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExchangeIntegration(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ExchangeIntegration : {}", id);
        exchangeIntegrationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
