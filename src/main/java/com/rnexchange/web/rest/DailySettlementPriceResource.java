package com.rnexchange.web.rest;

import com.rnexchange.repository.DailySettlementPriceRepository;
import com.rnexchange.service.DailySettlementPriceQueryService;
import com.rnexchange.service.DailySettlementPriceService;
import com.rnexchange.service.criteria.DailySettlementPriceCriteria;
import com.rnexchange.service.dto.DailySettlementPriceDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.DailySettlementPrice}.
 */
@RestController
@RequestMapping("/api/daily-settlement-prices")
public class DailySettlementPriceResource {

    private static final Logger LOG = LoggerFactory.getLogger(DailySettlementPriceResource.class);

    private static final String ENTITY_NAME = "dailySettlementPrice";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DailySettlementPriceService dailySettlementPriceService;

    private final DailySettlementPriceRepository dailySettlementPriceRepository;

    private final DailySettlementPriceQueryService dailySettlementPriceQueryService;

    public DailySettlementPriceResource(
        DailySettlementPriceService dailySettlementPriceService,
        DailySettlementPriceRepository dailySettlementPriceRepository,
        DailySettlementPriceQueryService dailySettlementPriceQueryService
    ) {
        this.dailySettlementPriceService = dailySettlementPriceService;
        this.dailySettlementPriceRepository = dailySettlementPriceRepository;
        this.dailySettlementPriceQueryService = dailySettlementPriceQueryService;
    }

    /**
     * {@code POST  /daily-settlement-prices} : Create a new dailySettlementPrice.
     *
     * @param dailySettlementPriceDTO the dailySettlementPriceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new dailySettlementPriceDTO, or with status {@code 400 (Bad Request)} if the dailySettlementPrice has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DailySettlementPriceDTO> createDailySettlementPrice(
        @Valid @RequestBody DailySettlementPriceDTO dailySettlementPriceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save DailySettlementPrice : {}", dailySettlementPriceDTO);
        if (dailySettlementPriceDTO.getId() != null) {
            throw new BadRequestAlertException("A new dailySettlementPrice cannot already have an ID", ENTITY_NAME, "idexists");
        }
        dailySettlementPriceDTO = dailySettlementPriceService.save(dailySettlementPriceDTO);
        return ResponseEntity.created(new URI("/api/daily-settlement-prices/" + dailySettlementPriceDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, dailySettlementPriceDTO.getId().toString()))
            .body(dailySettlementPriceDTO);
    }

    /**
     * {@code PUT  /daily-settlement-prices/:id} : Updates an existing dailySettlementPrice.
     *
     * @param id the id of the dailySettlementPriceDTO to save.
     * @param dailySettlementPriceDTO the dailySettlementPriceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dailySettlementPriceDTO,
     * or with status {@code 400 (Bad Request)} if the dailySettlementPriceDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the dailySettlementPriceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DailySettlementPriceDTO> updateDailySettlementPrice(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DailySettlementPriceDTO dailySettlementPriceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DailySettlementPrice : {}, {}", id, dailySettlementPriceDTO);
        if (dailySettlementPriceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dailySettlementPriceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dailySettlementPriceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        dailySettlementPriceDTO = dailySettlementPriceService.update(dailySettlementPriceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, dailySettlementPriceDTO.getId().toString()))
            .body(dailySettlementPriceDTO);
    }

    /**
     * {@code PATCH  /daily-settlement-prices/:id} : Partial updates given fields of an existing dailySettlementPrice, field will ignore if it is null
     *
     * @param id the id of the dailySettlementPriceDTO to save.
     * @param dailySettlementPriceDTO the dailySettlementPriceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dailySettlementPriceDTO,
     * or with status {@code 400 (Bad Request)} if the dailySettlementPriceDTO is not valid,
     * or with status {@code 404 (Not Found)} if the dailySettlementPriceDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the dailySettlementPriceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DailySettlementPriceDTO> partialUpdateDailySettlementPrice(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DailySettlementPriceDTO dailySettlementPriceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DailySettlementPrice partially : {}, {}", id, dailySettlementPriceDTO);
        if (dailySettlementPriceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dailySettlementPriceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dailySettlementPriceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DailySettlementPriceDTO> result = dailySettlementPriceService.partialUpdate(dailySettlementPriceDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, dailySettlementPriceDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /daily-settlement-prices} : get all the dailySettlementPrices.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of dailySettlementPrices in body.
     */
    @GetMapping("")
    public ResponseEntity<List<DailySettlementPriceDTO>> getAllDailySettlementPrices(
        DailySettlementPriceCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get DailySettlementPrices by criteria: {}", criteria);

        Page<DailySettlementPriceDTO> page = dailySettlementPriceQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /daily-settlement-prices/count} : count all the dailySettlementPrices.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countDailySettlementPrices(DailySettlementPriceCriteria criteria) {
        LOG.debug("REST request to count DailySettlementPrices by criteria: {}", criteria);
        return ResponseEntity.ok().body(dailySettlementPriceQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /daily-settlement-prices/:id} : get the "id" dailySettlementPrice.
     *
     * @param id the id of the dailySettlementPriceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the dailySettlementPriceDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DailySettlementPriceDTO> getDailySettlementPrice(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DailySettlementPrice : {}", id);
        Optional<DailySettlementPriceDTO> dailySettlementPriceDTO = dailySettlementPriceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dailySettlementPriceDTO);
    }

    /**
     * {@code DELETE  /daily-settlement-prices/:id} : delete the "id" dailySettlementPrice.
     *
     * @param id the id of the dailySettlementPriceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDailySettlementPrice(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DailySettlementPrice : {}", id);
        dailySettlementPriceService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
