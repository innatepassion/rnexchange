package com.rnexchange.web.rest;

import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.Order;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.repository.OrderRepository;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.security.SecurityUtils;
import com.rnexchange.service.InsufficientMarginException;
import com.rnexchange.service.OrderQueryService;
import com.rnexchange.service.OrderService;
import com.rnexchange.service.TradingService;
import com.rnexchange.service.criteria.OrderCriteria;
import com.rnexchange.service.dto.OrderDTO;
import com.rnexchange.service.dto.TraderOrderRequest;
import com.rnexchange.service.dto.TraderOrderResult;
import com.rnexchange.service.mapper.OrderMapper;
import com.rnexchange.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
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
 * REST controller for managing {@link com.rnexchange.domain.Order}.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrderResource.class);

    private static final String ENTITY_NAME = "order";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final OrderService orderService;

    private final OrderRepository orderRepository;

    private final OrderQueryService orderQueryService;

    private final TradingService tradingService;

    private final TradingAccountRepository tradingAccountRepository;

    private final InstrumentRepository instrumentRepository;

    private final OrderMapper orderMapper;

    public OrderResource(
        OrderService orderService,
        OrderRepository orderRepository,
        OrderQueryService orderQueryService,
        TradingService tradingService,
        TradingAccountRepository tradingAccountRepository,
        InstrumentRepository instrumentRepository,
        OrderMapper orderMapper
    ) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderQueryService = orderQueryService;
        this.tradingService = tradingService;
        this.tradingAccountRepository = tradingAccountRepository;
        this.instrumentRepository = instrumentRepository;
        this.orderMapper = orderMapper;
    }

    /**
     * {@code POST  /orders} : Create a new order.
     *
     * @param orderDTO the orderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new orderDTO, or with status {@code 400 (Bad Request)} if the order has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) throws URISyntaxException {
        LOG.debug("REST request to save Order : {}", orderDTO);
        if (orderDTO.getId() != null) {
            throw new BadRequestAlertException("A new order cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.TRADER)) {
            Supplier<BadRequestAlertException> badRequest = () ->
                new BadRequestAlertException("Missing required trading details for trader order", ENTITY_NAME, "invalidpayload");
            String traderLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestAlertException("Unable to determine current trader", ENTITY_NAME, "nologin"));
            if (orderDTO.getInstrument() == null) {
                throw badRequest.get();
            }
            if (orderDTO.getLimitPx() == null) {
                throw new BadRequestAlertException("Limit price required for trader orders", ENTITY_NAME, "missingprice");
            }

            TraderOrderRequest request = TraderOrderRequest.builder()
                .traderLogin(traderLogin)
                .instrumentId(orderDTO.getInstrument().getId())
                .instrumentSymbol(orderDTO.getInstrument().getSymbol())
                .side(orderDTO.getSide())
                .type(orderDTO.getType())
                .tif(orderDTO.getTif())
                .quantity(orderDTO.getQty())
                .price(orderDTO.getLimitPx())
                .build();
            try {
                TraderOrderResult result = orderService.submitTraderOrder(request);
                orderDTO = result.order();
                HttpHeaders headers = HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, orderDTO.getId().toString());
                if (result.marginAssessment() != null) {
                    headers.add("X-RNExchange-Margin-Initial", result.marginAssessment().initialRequirement().toPlainString());
                    headers.add("X-RNExchange-Margin-Remaining", result.marginAssessment().remainingBalance().toPlainString());
                }
                return ResponseEntity.created(new URI("/api/orders/" + orderDTO.getId())).headers(headers).body(orderDTO);
            } catch (InsufficientMarginException ex) {
                throw new BadRequestAlertException(ex.getMessage(), ENTITY_NAME, "insufficientmargin");
            }
        }

        orderDTO = orderService.save(orderDTO);
        return ResponseEntity.created(new URI("/api/orders/" + orderDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, orderDTO.getId().toString()))
            .body(orderDTO);
    }

    /**
     * {@code PUT  /orders/:id} : Updates an existing order.
     *
     * @param id the id of the orderDTO to save.
     * @param orderDTO the orderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated orderDTO,
     * or with status {@code 400 (Bad Request)} if the orderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the orderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody OrderDTO orderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Order : {}, {}", id, orderDTO);
        if (orderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, orderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!orderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        orderDTO = orderService.update(orderDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, orderDTO.getId().toString()))
            .body(orderDTO);
    }

    /**
     * {@code PATCH  /orders/:id} : Partial updates given fields of an existing order, field will ignore if it is null
     *
     * @param id the id of the orderDTO to save.
     * @param orderDTO the orderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated orderDTO,
     * or with status {@code 400 (Bad Request)} if the orderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the orderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the orderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<OrderDTO> partialUpdateOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody OrderDTO orderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Order partially : {}, {}", id, orderDTO);
        if (orderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, orderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!orderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<OrderDTO> result = orderService.partialUpdate(orderDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, orderDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /orders} : get all the orders.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of orders in body.
     */
    @GetMapping("")
    public ResponseEntity<List<OrderDTO>> getAllOrders(
        OrderCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Orders by criteria: {}", criteria);

        Page<OrderDTO> page = orderQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /orders/count} : count all the orders.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countOrders(OrderCriteria criteria) {
        LOG.debug("REST request to count Orders by criteria: {}", criteria);
        return ResponseEntity.ok().body(orderQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /orders/:id} : get the "id" order.
     *
     * @param id the id of the orderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the orderDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Order : {}", id);
        Optional<OrderDTO> orderDTO = orderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(orderDTO);
    }

    /**
     * {@code DELETE  /orders/:id} : delete the "id" order.
     *
     * @param id the id of the orderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Order : {}", id);
        orderService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    // ============= PHASE 3 - USER STORY 1: Trading Endpoints (T011) =============

    /**
     * T011: POST /api/orders - Place a trading order (BUY/SELL).
     *
     * This endpoint handles trader order placement with immediate validation and matching.
     * For TRADERS: creates BUY/SELL orders that are immediately validated and matched.
     * For ADMINS: bypasses trader-specific logic.
     *
     * Flow:
     * 1. Resolve trading account and instrument
     * 2. Validate order parameters
     * 3. Route to appropriate service (TradingService for trader orders)
     * 4. Return filled/rejected order with execution price (if filled)
     *
     * @param orderDTO the order to place
     * @return OrderDTO with updated status and execution details
     */
    @PostMapping("/trading")
    public ResponseEntity<OrderDTO> placeTradingOrder(@Valid @RequestBody OrderDTO orderDTO) throws URISyntaxException {
        LOG.debug("REST request to place trading order: {}", orderDTO);

        if (orderDTO.getId() != null) {
            throw new BadRequestAlertException("A new order cannot already have an ID", ENTITY_NAME, "idexists");
        }

        // Validate required fields for trading order
        if (orderDTO.getInstrument() == null) {
            throw new BadRequestAlertException("Instrument is required for trading order", ENTITY_NAME, "missinginstrument");
        }
        if (orderDTO.getQty() == null) {
            throw new BadRequestAlertException("Quantity is required", ENTITY_NAME, "missingqty");
        }
        if (orderDTO.getSide() == null) {
            throw new BadRequestAlertException("Side (BUY/SELL) is required", ENTITY_NAME, "missingside");
        }
        if (orderDTO.getType() == null) {
            throw new BadRequestAlertException("Order type is required", ENTITY_NAME, "missingtype");
        }

        try {
            // Get current trader
            String traderLogin = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestAlertException("Unable to determine current trader", ENTITY_NAME, "nologin"));

            // Resolve trading account
            TradingAccount tradingAccount = tradingAccountRepository
                .findFirstByTrader_User_LoginOrderByIdAsc(traderLogin)
                .orElseThrow(() -> new BadRequestAlertException("Trading account not found for trader", ENTITY_NAME, "notradingaccount"));

            // Resolve instrument
            Instrument instrument = instrumentRepository
                .findById(orderDTO.getInstrument().getId())
                .orElseThrow(() -> new BadRequestAlertException("Instrument not found", ENTITY_NAME, "noinstrument"));

            // Create order entity
            Order order = new Order();
            order.setSide(orderDTO.getSide());
            order.setType(orderDTO.getType());
            order.setQty(orderDTO.getQty());
            order.setLimitPx(orderDTO.getLimitPx());
            order.setStopPx(orderDTO.getStopPx());
            order.setTif(orderDTO.getTif() != null ? orderDTO.getTif() : com.rnexchange.domain.enumeration.Tif.DAY);
            order.setStatus(com.rnexchange.domain.enumeration.OrderStatus.NEW);
            order.setVenue(instrument.getExchangeCode() != null ? instrument.getExchangeCode() : "NSE");
            order.setCreatedAt(java.time.Instant.now());
            order.setUpdatedAt(java.time.Instant.now());
            order.setTradingAccount(tradingAccount);
            order.setInstrument(instrument);

            // Save initial order
            order = orderRepository.save(order);

            // Process order through trading service
            Order processedOrder;
            if (orderDTO.getSide() == com.rnexchange.domain.enumeration.OrderSide.BUY) {
                processedOrder = tradingService.processBuyOrder(order, tradingAccount, instrument);
            } else {
                processedOrder = tradingService.processSellOrder(order, tradingAccount, instrument);
            }

            // Return processed order
            OrderDTO result = orderMapper.toDto(processedOrder);
            HttpHeaders headers = HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString());
            return ResponseEntity.created(new URI("/api/orders/" + result.getId())).headers(headers).body(result);
        } catch (BadRequestAlertException e) {
            throw e;
        } catch (UnsupportedOperationException e) {
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "notsupported");
        } catch (IllegalArgumentException e) {
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "invalidrequest");
        } catch (Exception e) {
            LOG.error("Error placing trading order", e);
            throw new BadRequestAlertException("Error processing order: " + e.getMessage(), ENTITY_NAME, "processerror");
        }
    }
}
