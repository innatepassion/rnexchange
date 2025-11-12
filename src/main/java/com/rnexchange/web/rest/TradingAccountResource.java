package com.rnexchange.web.rest;

import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.service.TradingAccountQueryService;
import com.rnexchange.service.TradingAccountService;
import com.rnexchange.service.criteria.TradingAccountCriteria;
import com.rnexchange.service.dto.TradingAccountDTO;
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
 * REST controller for managing {@link com.rnexchange.domain.TradingAccount}.
 */
@RestController
@RequestMapping("/api/trading-accounts")
public class TradingAccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(TradingAccountResource.class);

    private static final String ENTITY_NAME = "tradingAccount";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TradingAccountService tradingAccountService;

    private final TradingAccountRepository tradingAccountRepository;

    private final TradingAccountQueryService tradingAccountQueryService;

    public TradingAccountResource(
        TradingAccountService tradingAccountService,
        TradingAccountRepository tradingAccountRepository,
        TradingAccountQueryService tradingAccountQueryService
    ) {
        this.tradingAccountService = tradingAccountService;
        this.tradingAccountRepository = tradingAccountRepository;
        this.tradingAccountQueryService = tradingAccountQueryService;
    }

    /**
     * {@code POST  /trading-accounts} : Create a new tradingAccount.
     *
     * @param tradingAccountDTO the tradingAccountDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new tradingAccountDTO, or with status {@code 400 (Bad Request)} if the tradingAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TradingAccountDTO> createTradingAccount(@Valid @RequestBody TradingAccountDTO tradingAccountDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save TradingAccount : {}", tradingAccountDTO);
        if (tradingAccountDTO.getId() != null) {
            throw new BadRequestAlertException("A new tradingAccount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        tradingAccountDTO = tradingAccountService.save(tradingAccountDTO);
        return ResponseEntity.created(new URI("/api/trading-accounts/" + tradingAccountDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, tradingAccountDTO.getId().toString()))
            .body(tradingAccountDTO);
    }

    /**
     * {@code PUT  /trading-accounts/:id} : Updates an existing tradingAccount.
     *
     * @param id the id of the tradingAccountDTO to save.
     * @param tradingAccountDTO the tradingAccountDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated tradingAccountDTO,
     * or with status {@code 400 (Bad Request)} if the tradingAccountDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the tradingAccountDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TradingAccountDTO> updateTradingAccount(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TradingAccountDTO tradingAccountDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TradingAccount : {}, {}", id, tradingAccountDTO);
        if (tradingAccountDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, tradingAccountDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!tradingAccountRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        tradingAccountDTO = tradingAccountService.update(tradingAccountDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, tradingAccountDTO.getId().toString()))
            .body(tradingAccountDTO);
    }

    /**
     * {@code PATCH  /trading-accounts/:id} : Partial updates given fields of an existing tradingAccount, field will ignore if it is null
     *
     * @param id the id of the tradingAccountDTO to save.
     * @param tradingAccountDTO the tradingAccountDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated tradingAccountDTO,
     * or with status {@code 400 (Bad Request)} if the tradingAccountDTO is not valid,
     * or with status {@code 404 (Not Found)} if the tradingAccountDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the tradingAccountDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TradingAccountDTO> partialUpdateTradingAccount(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TradingAccountDTO tradingAccountDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TradingAccount partially : {}, {}", id, tradingAccountDTO);
        if (tradingAccountDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, tradingAccountDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!tradingAccountRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TradingAccountDTO> result = tradingAccountService.partialUpdate(tradingAccountDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, tradingAccountDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /trading-accounts} : get all the tradingAccounts.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of tradingAccounts in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TradingAccountDTO>> getAllTradingAccounts(
        TradingAccountCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get TradingAccounts by criteria: {}", criteria);

        Page<TradingAccountDTO> page = tradingAccountQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /trading-accounts/count} : count all the tradingAccounts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countTradingAccounts(TradingAccountCriteria criteria) {
        LOG.debug("REST request to count TradingAccounts by criteria: {}", criteria);
        return ResponseEntity.ok().body(tradingAccountQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /trading-accounts/:id} : get the "id" tradingAccount.
     *
     * @param id the id of the tradingAccountDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the tradingAccountDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TradingAccountDTO> getTradingAccount(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TradingAccount : {}", id);
        Optional<TradingAccountDTO> tradingAccountDTO = tradingAccountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(tradingAccountDTO);
    }

    /**
     * {@code DELETE  /trading-accounts/:id} : delete the "id" tradingAccount.
     *
     * @param id the id of the tradingAccountDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTradingAccount(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete TradingAccount : {}", id);
        tradingAccountService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
