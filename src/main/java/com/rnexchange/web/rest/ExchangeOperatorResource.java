package com.rnexchange.web.rest;

import com.rnexchange.repository.ExchangeOperatorRepository;
import com.rnexchange.service.ExchangeOperatorQueryService;
import com.rnexchange.service.ExchangeOperatorService;
import com.rnexchange.service.criteria.ExchangeOperatorCriteria;
import com.rnexchange.service.dto.ExchangeOperatorDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.ExchangeOperator}.
 */
@RestController
@RequestMapping("/api/exchange-operators")
public class ExchangeOperatorResource {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeOperatorResource.class);

    private static final String ENTITY_NAME = "exchangeOperator";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ExchangeOperatorService exchangeOperatorService;

    private final ExchangeOperatorRepository exchangeOperatorRepository;

    private final ExchangeOperatorQueryService exchangeOperatorQueryService;

    public ExchangeOperatorResource(
        ExchangeOperatorService exchangeOperatorService,
        ExchangeOperatorRepository exchangeOperatorRepository,
        ExchangeOperatorQueryService exchangeOperatorQueryService
    ) {
        this.exchangeOperatorService = exchangeOperatorService;
        this.exchangeOperatorRepository = exchangeOperatorRepository;
        this.exchangeOperatorQueryService = exchangeOperatorQueryService;
    }

    /**
     * {@code POST  /exchange-operators} : Create a new exchangeOperator.
     *
     * @param exchangeOperatorDTO the exchangeOperatorDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new exchangeOperatorDTO, or with status {@code 400 (Bad Request)} if the exchangeOperator has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ExchangeOperatorDTO> createExchangeOperator(@Valid @RequestBody ExchangeOperatorDTO exchangeOperatorDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save ExchangeOperator : {}", exchangeOperatorDTO);
        if (exchangeOperatorDTO.getId() != null) {
            throw new BadRequestAlertException("A new exchangeOperator cannot already have an ID", ENTITY_NAME, "idexists");
        }
        exchangeOperatorDTO = exchangeOperatorService.save(exchangeOperatorDTO);
        return ResponseEntity.created(new URI("/api/exchange-operators/" + exchangeOperatorDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, exchangeOperatorDTO.getId().toString()))
            .body(exchangeOperatorDTO);
    }

    /**
     * {@code PUT  /exchange-operators/:id} : Updates an existing exchangeOperator.
     *
     * @param id the id of the exchangeOperatorDTO to save.
     * @param exchangeOperatorDTO the exchangeOperatorDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated exchangeOperatorDTO,
     * or with status {@code 400 (Bad Request)} if the exchangeOperatorDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the exchangeOperatorDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExchangeOperatorDTO> updateExchangeOperator(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ExchangeOperatorDTO exchangeOperatorDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ExchangeOperator : {}, {}", id, exchangeOperatorDTO);
        if (exchangeOperatorDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, exchangeOperatorDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!exchangeOperatorRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        exchangeOperatorDTO = exchangeOperatorService.update(exchangeOperatorDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, exchangeOperatorDTO.getId().toString()))
            .body(exchangeOperatorDTO);
    }

    /**
     * {@code PATCH  /exchange-operators/:id} : Partial updates given fields of an existing exchangeOperator, field will ignore if it is null
     *
     * @param id the id of the exchangeOperatorDTO to save.
     * @param exchangeOperatorDTO the exchangeOperatorDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated exchangeOperatorDTO,
     * or with status {@code 400 (Bad Request)} if the exchangeOperatorDTO is not valid,
     * or with status {@code 404 (Not Found)} if the exchangeOperatorDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the exchangeOperatorDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ExchangeOperatorDTO> partialUpdateExchangeOperator(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ExchangeOperatorDTO exchangeOperatorDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ExchangeOperator partially : {}, {}", id, exchangeOperatorDTO);
        if (exchangeOperatorDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, exchangeOperatorDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!exchangeOperatorRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ExchangeOperatorDTO> result = exchangeOperatorService.partialUpdate(exchangeOperatorDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, exchangeOperatorDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /exchange-operators} : get all the exchangeOperators.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of exchangeOperators in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ExchangeOperatorDTO>> getAllExchangeOperators(
        ExchangeOperatorCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get ExchangeOperators by criteria: {}", criteria);

        Page<ExchangeOperatorDTO> page = exchangeOperatorQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /exchange-operators/count} : count all the exchangeOperators.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countExchangeOperators(ExchangeOperatorCriteria criteria) {
        LOG.debug("REST request to count ExchangeOperators by criteria: {}", criteria);
        return ResponseEntity.ok().body(exchangeOperatorQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /exchange-operators/:id} : get the "id" exchangeOperator.
     *
     * @param id the id of the exchangeOperatorDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the exchangeOperatorDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExchangeOperatorDTO> getExchangeOperator(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ExchangeOperator : {}", id);
        Optional<ExchangeOperatorDTO> exchangeOperatorDTO = exchangeOperatorService.findOne(id);
        return ResponseUtil.wrapOrNotFound(exchangeOperatorDTO);
    }

    /**
     * {@code DELETE  /exchange-operators/:id} : delete the "id" exchangeOperator.
     *
     * @param id the id of the exchangeOperatorDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExchangeOperator(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ExchangeOperator : {}", id);
        exchangeOperatorService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
