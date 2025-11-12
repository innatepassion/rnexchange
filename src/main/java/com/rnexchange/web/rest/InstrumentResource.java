package com.rnexchange.web.rest;

import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.service.InstrumentQueryService;
import com.rnexchange.service.InstrumentService;
import com.rnexchange.service.criteria.InstrumentCriteria;
import com.rnexchange.service.dto.InstrumentDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.Instrument}.
 */
@RestController
@RequestMapping("/api/instruments")
public class InstrumentResource {

    private static final Logger LOG = LoggerFactory.getLogger(InstrumentResource.class);

    private static final String ENTITY_NAME = "instrument";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InstrumentService instrumentService;

    private final InstrumentRepository instrumentRepository;

    private final InstrumentQueryService instrumentQueryService;

    public InstrumentResource(
        InstrumentService instrumentService,
        InstrumentRepository instrumentRepository,
        InstrumentQueryService instrumentQueryService
    ) {
        this.instrumentService = instrumentService;
        this.instrumentRepository = instrumentRepository;
        this.instrumentQueryService = instrumentQueryService;
    }

    /**
     * {@code POST  /instruments} : Create a new instrument.
     *
     * @param instrumentDTO the instrumentDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new instrumentDTO, or with status {@code 400 (Bad Request)} if the instrument has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<InstrumentDTO> createInstrument(@Valid @RequestBody InstrumentDTO instrumentDTO) throws URISyntaxException {
        LOG.debug("REST request to save Instrument : {}", instrumentDTO);
        if (instrumentDTO.getId() != null) {
            throw new BadRequestAlertException("A new instrument cannot already have an ID", ENTITY_NAME, "idexists");
        }
        instrumentDTO = instrumentService.save(instrumentDTO);
        return ResponseEntity.created(new URI("/api/instruments/" + instrumentDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, instrumentDTO.getId().toString()))
            .body(instrumentDTO);
    }

    /**
     * {@code PUT  /instruments/:id} : Updates an existing instrument.
     *
     * @param id the id of the instrumentDTO to save.
     * @param instrumentDTO the instrumentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated instrumentDTO,
     * or with status {@code 400 (Bad Request)} if the instrumentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the instrumentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InstrumentDTO> updateInstrument(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody InstrumentDTO instrumentDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Instrument : {}, {}", id, instrumentDTO);
        if (instrumentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, instrumentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!instrumentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        instrumentDTO = instrumentService.update(instrumentDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, instrumentDTO.getId().toString()))
            .body(instrumentDTO);
    }

    /**
     * {@code PATCH  /instruments/:id} : Partial updates given fields of an existing instrument, field will ignore if it is null
     *
     * @param id the id of the instrumentDTO to save.
     * @param instrumentDTO the instrumentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated instrumentDTO,
     * or with status {@code 400 (Bad Request)} if the instrumentDTO is not valid,
     * or with status {@code 404 (Not Found)} if the instrumentDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the instrumentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<InstrumentDTO> partialUpdateInstrument(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody InstrumentDTO instrumentDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Instrument partially : {}, {}", id, instrumentDTO);
        if (instrumentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, instrumentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!instrumentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<InstrumentDTO> result = instrumentService.partialUpdate(instrumentDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, instrumentDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /instruments} : get all the instruments.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of instruments in body.
     */
    @GetMapping("")
    public ResponseEntity<List<InstrumentDTO>> getAllInstruments(
        InstrumentCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Instruments by criteria: {}", criteria);

        Page<InstrumentDTO> page = instrumentQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /instruments/count} : count all the instruments.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countInstruments(InstrumentCriteria criteria) {
        LOG.debug("REST request to count Instruments by criteria: {}", criteria);
        return ResponseEntity.ok().body(instrumentQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /instruments/:id} : get the "id" instrument.
     *
     * @param id the id of the instrumentDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the instrumentDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstrumentDTO> getInstrument(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Instrument : {}", id);
        Optional<InstrumentDTO> instrumentDTO = instrumentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(instrumentDTO);
    }

    /**
     * {@code DELETE  /instruments/:id} : delete the "id" instrument.
     *
     * @param id the id of the instrumentDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstrument(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Instrument : {}", id);
        instrumentService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
