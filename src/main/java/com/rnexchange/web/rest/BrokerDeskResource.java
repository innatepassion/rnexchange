package com.rnexchange.web.rest;

import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.service.BrokerDeskQueryService;
import com.rnexchange.service.BrokerDeskService;
import com.rnexchange.service.criteria.BrokerDeskCriteria;
import com.rnexchange.service.dto.BrokerDeskDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.BrokerDesk}.
 */
@RestController
@RequestMapping("/api/broker-desks")
public class BrokerDeskResource {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerDeskResource.class);

    private static final String ENTITY_NAME = "brokerDesk";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BrokerDeskService brokerDeskService;

    private final BrokerDeskRepository brokerDeskRepository;

    private final BrokerDeskQueryService brokerDeskQueryService;

    public BrokerDeskResource(
        BrokerDeskService brokerDeskService,
        BrokerDeskRepository brokerDeskRepository,
        BrokerDeskQueryService brokerDeskQueryService
    ) {
        this.brokerDeskService = brokerDeskService;
        this.brokerDeskRepository = brokerDeskRepository;
        this.brokerDeskQueryService = brokerDeskQueryService;
    }

    /**
     * {@code POST  /broker-desks} : Create a new brokerDesk.
     *
     * @param brokerDeskDTO the brokerDeskDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new brokerDeskDTO, or with status {@code 400 (Bad Request)} if the brokerDesk has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BrokerDeskDTO> createBrokerDesk(@Valid @RequestBody BrokerDeskDTO brokerDeskDTO) throws URISyntaxException {
        LOG.debug("REST request to save BrokerDesk : {}", brokerDeskDTO);
        if (brokerDeskDTO.getId() != null) {
            throw new BadRequestAlertException("A new brokerDesk cannot already have an ID", ENTITY_NAME, "idexists");
        }
        brokerDeskDTO = brokerDeskService.save(brokerDeskDTO);
        return ResponseEntity.created(new URI("/api/broker-desks/" + brokerDeskDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, brokerDeskDTO.getId().toString()))
            .body(brokerDeskDTO);
    }

    /**
     * {@code PUT  /broker-desks/:id} : Updates an existing brokerDesk.
     *
     * @param id the id of the brokerDeskDTO to save.
     * @param brokerDeskDTO the brokerDeskDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated brokerDeskDTO,
     * or with status {@code 400 (Bad Request)} if the brokerDeskDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the brokerDeskDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BrokerDeskDTO> updateBrokerDesk(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BrokerDeskDTO brokerDeskDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update BrokerDesk : {}, {}", id, brokerDeskDTO);
        if (brokerDeskDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, brokerDeskDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!brokerDeskRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        brokerDeskDTO = brokerDeskService.update(brokerDeskDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, brokerDeskDTO.getId().toString()))
            .body(brokerDeskDTO);
    }

    /**
     * {@code PATCH  /broker-desks/:id} : Partial updates given fields of an existing brokerDesk, field will ignore if it is null
     *
     * @param id the id of the brokerDeskDTO to save.
     * @param brokerDeskDTO the brokerDeskDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated brokerDeskDTO,
     * or with status {@code 400 (Bad Request)} if the brokerDeskDTO is not valid,
     * or with status {@code 404 (Not Found)} if the brokerDeskDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the brokerDeskDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BrokerDeskDTO> partialUpdateBrokerDesk(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BrokerDeskDTO brokerDeskDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BrokerDesk partially : {}, {}", id, brokerDeskDTO);
        if (brokerDeskDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, brokerDeskDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!brokerDeskRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BrokerDeskDTO> result = brokerDeskService.partialUpdate(brokerDeskDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, brokerDeskDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /broker-desks} : get all the brokerDesks.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of brokerDesks in body.
     */
    @GetMapping("")
    public ResponseEntity<List<BrokerDeskDTO>> getAllBrokerDesks(
        BrokerDeskCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get BrokerDesks by criteria: {}", criteria);

        Page<BrokerDeskDTO> page = brokerDeskQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /broker-desks/count} : count all the brokerDesks.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countBrokerDesks(BrokerDeskCriteria criteria) {
        LOG.debug("REST request to count BrokerDesks by criteria: {}", criteria);
        return ResponseEntity.ok().body(brokerDeskQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /broker-desks/:id} : get the "id" brokerDesk.
     *
     * @param id the id of the brokerDeskDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the brokerDeskDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrokerDeskDTO> getBrokerDesk(@PathVariable("id") Long id) {
        LOG.debug("REST request to get BrokerDesk : {}", id);
        Optional<BrokerDeskDTO> brokerDeskDTO = brokerDeskService.findOne(id);
        return ResponseUtil.wrapOrNotFound(brokerDeskDTO);
    }

    /**
     * {@code DELETE  /broker-desks/:id} : delete the "id" brokerDesk.
     *
     * @param id the id of the brokerDeskDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrokerDesk(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete BrokerDesk : {}", id);
        brokerDeskService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
