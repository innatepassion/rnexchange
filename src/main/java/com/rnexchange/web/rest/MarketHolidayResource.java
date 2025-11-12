package com.rnexchange.web.rest;

import com.rnexchange.repository.MarketHolidayRepository;
import com.rnexchange.service.MarketHolidayQueryService;
import com.rnexchange.service.MarketHolidayService;
import com.rnexchange.service.criteria.MarketHolidayCriteria;
import com.rnexchange.service.dto.MarketHolidayDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.MarketHoliday}.
 */
@RestController
@RequestMapping("/api/market-holidays")
public class MarketHolidayResource {

    private static final Logger LOG = LoggerFactory.getLogger(MarketHolidayResource.class);

    private static final String ENTITY_NAME = "marketHoliday";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MarketHolidayService marketHolidayService;

    private final MarketHolidayRepository marketHolidayRepository;

    private final MarketHolidayQueryService marketHolidayQueryService;

    public MarketHolidayResource(
        MarketHolidayService marketHolidayService,
        MarketHolidayRepository marketHolidayRepository,
        MarketHolidayQueryService marketHolidayQueryService
    ) {
        this.marketHolidayService = marketHolidayService;
        this.marketHolidayRepository = marketHolidayRepository;
        this.marketHolidayQueryService = marketHolidayQueryService;
    }

    /**
     * {@code POST  /market-holidays} : Create a new marketHoliday.
     *
     * @param marketHolidayDTO the marketHolidayDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new marketHolidayDTO, or with status {@code 400 (Bad Request)} if the marketHoliday has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MarketHolidayDTO> createMarketHoliday(@Valid @RequestBody MarketHolidayDTO marketHolidayDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save MarketHoliday : {}", marketHolidayDTO);
        if (marketHolidayDTO.getId() != null) {
            throw new BadRequestAlertException("A new marketHoliday cannot already have an ID", ENTITY_NAME, "idexists");
        }
        marketHolidayDTO = marketHolidayService.save(marketHolidayDTO);
        return ResponseEntity.created(new URI("/api/market-holidays/" + marketHolidayDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, marketHolidayDTO.getId().toString()))
            .body(marketHolidayDTO);
    }

    /**
     * {@code PUT  /market-holidays/:id} : Updates an existing marketHoliday.
     *
     * @param id the id of the marketHolidayDTO to save.
     * @param marketHolidayDTO the marketHolidayDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated marketHolidayDTO,
     * or with status {@code 400 (Bad Request)} if the marketHolidayDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the marketHolidayDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MarketHolidayDTO> updateMarketHoliday(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MarketHolidayDTO marketHolidayDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update MarketHoliday : {}, {}", id, marketHolidayDTO);
        if (marketHolidayDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, marketHolidayDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!marketHolidayRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        marketHolidayDTO = marketHolidayService.update(marketHolidayDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, marketHolidayDTO.getId().toString()))
            .body(marketHolidayDTO);
    }

    /**
     * {@code PATCH  /market-holidays/:id} : Partial updates given fields of an existing marketHoliday, field will ignore if it is null
     *
     * @param id the id of the marketHolidayDTO to save.
     * @param marketHolidayDTO the marketHolidayDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated marketHolidayDTO,
     * or with status {@code 400 (Bad Request)} if the marketHolidayDTO is not valid,
     * or with status {@code 404 (Not Found)} if the marketHolidayDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the marketHolidayDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MarketHolidayDTO> partialUpdateMarketHoliday(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody MarketHolidayDTO marketHolidayDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MarketHoliday partially : {}, {}", id, marketHolidayDTO);
        if (marketHolidayDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, marketHolidayDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!marketHolidayRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MarketHolidayDTO> result = marketHolidayService.partialUpdate(marketHolidayDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, marketHolidayDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /market-holidays} : get all the marketHolidays.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of marketHolidays in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MarketHolidayDTO>> getAllMarketHolidays(
        MarketHolidayCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get MarketHolidays by criteria: {}", criteria);

        Page<MarketHolidayDTO> page = marketHolidayQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /market-holidays/count} : count all the marketHolidays.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countMarketHolidays(MarketHolidayCriteria criteria) {
        LOG.debug("REST request to count MarketHolidays by criteria: {}", criteria);
        return ResponseEntity.ok().body(marketHolidayQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /market-holidays/:id} : get the "id" marketHoliday.
     *
     * @param id the id of the marketHolidayDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the marketHolidayDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MarketHolidayDTO> getMarketHoliday(@PathVariable("id") Long id) {
        LOG.debug("REST request to get MarketHoliday : {}", id);
        Optional<MarketHolidayDTO> marketHolidayDTO = marketHolidayService.findOne(id);
        return ResponseUtil.wrapOrNotFound(marketHolidayDTO);
    }

    /**
     * {@code DELETE  /market-holidays/:id} : delete the "id" marketHoliday.
     *
     * @param id the id of the marketHolidayDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMarketHoliday(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete MarketHoliday : {}", id);
        marketHolidayService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
