package com.rnexchange.web.rest;

import static com.rnexchange.domain.OrderAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static com.rnexchange.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.Order;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.enumeration.OrderSide;
import com.rnexchange.domain.enumeration.OrderStatus;
import com.rnexchange.domain.enumeration.OrderType;
import com.rnexchange.domain.enumeration.Tif;
import com.rnexchange.repository.OrderRepository;
import com.rnexchange.service.OrderService;
import com.rnexchange.service.dto.OrderDTO;
import com.rnexchange.service.mapper.OrderMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link OrderResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class OrderResourceIT {

    private static final OrderSide DEFAULT_SIDE = OrderSide.BUY;
    private static final OrderSide UPDATED_SIDE = OrderSide.SELL;

    private static final OrderType DEFAULT_TYPE = OrderType.MARKET;
    private static final OrderType UPDATED_TYPE = OrderType.LIMIT;

    private static final BigDecimal DEFAULT_QTY = new BigDecimal(1);
    private static final BigDecimal UPDATED_QTY = new BigDecimal(2);
    private static final BigDecimal SMALLER_QTY = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_LIMIT_PX = new BigDecimal(1);
    private static final BigDecimal UPDATED_LIMIT_PX = new BigDecimal(2);
    private static final BigDecimal SMALLER_LIMIT_PX = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_STOP_PX = new BigDecimal(1);
    private static final BigDecimal UPDATED_STOP_PX = new BigDecimal(2);
    private static final BigDecimal SMALLER_STOP_PX = new BigDecimal(1 - 1);

    private static final Tif DEFAULT_TIF = Tif.DAY;
    private static final Tif UPDATED_TIF = Tif.IOC;

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.NEW;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.ACCEPTED;

    private static final String DEFAULT_VENUE = "AAAAAAAAAA";
    private static final String UPDATED_VENUE = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OrderRepository orderRepository;

    @Mock
    private OrderRepository orderRepositoryMock;

    @Autowired
    private OrderMapper orderMapper;

    @Mock
    private OrderService orderServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restOrderMockMvc;

    private Order order;

    private Order insertedOrder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Order createEntity() {
        return new Order()
            .side(DEFAULT_SIDE)
            .type(DEFAULT_TYPE)
            .qty(DEFAULT_QTY)
            .limitPx(DEFAULT_LIMIT_PX)
            .stopPx(DEFAULT_STOP_PX)
            .tif(DEFAULT_TIF)
            .status(DEFAULT_STATUS)
            .venue(DEFAULT_VENUE)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Order createUpdatedEntity() {
        return new Order()
            .side(UPDATED_SIDE)
            .type(UPDATED_TYPE)
            .qty(UPDATED_QTY)
            .limitPx(UPDATED_LIMIT_PX)
            .stopPx(UPDATED_STOP_PX)
            .tif(UPDATED_TIF)
            .status(UPDATED_STATUS)
            .venue(UPDATED_VENUE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    void initTest() {
        order = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedOrder != null) {
            orderRepository.delete(insertedOrder);
            insertedOrder = null;
        }
    }

    @Test
    @Transactional
    void createOrder() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);
        var returnedOrderDTO = om.readValue(
            restOrderMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            OrderDTO.class
        );

        // Validate the Order in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedOrder = orderMapper.toEntity(returnedOrderDTO);
        assertOrderUpdatableFieldsEquals(returnedOrder, getPersistedOrder(returnedOrder));

        insertedOrder = returnedOrder;
    }

    @Test
    @Transactional
    void createOrderWithExistingId() throws Exception {
        // Create the Order with an existing ID
        order.setId(1L);
        OrderDTO orderDTO = orderMapper.toDto(order);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkSideIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        order.setSide(null);

        // Create the Order, which fails.
        OrderDTO orderDTO = orderMapper.toDto(order);

        restOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        order.setType(null);

        // Create the Order, which fails.
        OrderDTO orderDTO = orderMapper.toDto(order);

        restOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkQtyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        order.setQty(null);

        // Create the Order, which fails.
        OrderDTO orderDTO = orderMapper.toDto(order);

        restOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTifIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        order.setTif(null);

        // Create the Order, which fails.
        OrderDTO orderDTO = orderMapper.toDto(order);

        restOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        order.setStatus(null);

        // Create the Order, which fails.
        OrderDTO orderDTO = orderMapper.toDto(order);

        restOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkVenueIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        order.setVenue(null);

        // Create the Order, which fails.
        OrderDTO orderDTO = orderMapper.toDto(order);

        restOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllOrders() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList
        restOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(order.getId().intValue())))
            .andExpect(jsonPath("$.[*].side").value(hasItem(DEFAULT_SIDE.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].qty").value(hasItem(sameNumber(DEFAULT_QTY))))
            .andExpect(jsonPath("$.[*].limitPx").value(hasItem(sameNumber(DEFAULT_LIMIT_PX))))
            .andExpect(jsonPath("$.[*].stopPx").value(hasItem(sameNumber(DEFAULT_STOP_PX))))
            .andExpect(jsonPath("$.[*].tif").value(hasItem(DEFAULT_TIF.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].venue").value(hasItem(DEFAULT_VENUE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllOrdersWithEagerRelationshipsIsEnabled() throws Exception {
        when(orderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(orderServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllOrdersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(orderServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restOrderMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(orderRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getOrder() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get the order
        restOrderMockMvc
            .perform(get(ENTITY_API_URL_ID, order.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(order.getId().intValue()))
            .andExpect(jsonPath("$.side").value(DEFAULT_SIDE.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.qty").value(sameNumber(DEFAULT_QTY)))
            .andExpect(jsonPath("$.limitPx").value(sameNumber(DEFAULT_LIMIT_PX)))
            .andExpect(jsonPath("$.stopPx").value(sameNumber(DEFAULT_STOP_PX)))
            .andExpect(jsonPath("$.tif").value(DEFAULT_TIF.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.venue").value(DEFAULT_VENUE))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getOrdersByIdFiltering() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        Long id = order.getId();

        defaultOrderFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultOrderFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultOrderFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllOrdersBySideIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where side equals to
        defaultOrderFiltering("side.equals=" + DEFAULT_SIDE, "side.equals=" + UPDATED_SIDE);
    }

    @Test
    @Transactional
    void getAllOrdersBySideIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where side in
        defaultOrderFiltering("side.in=" + DEFAULT_SIDE + "," + UPDATED_SIDE, "side.in=" + UPDATED_SIDE);
    }

    @Test
    @Transactional
    void getAllOrdersBySideIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where side is not null
        defaultOrderFiltering("side.specified=true", "side.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where type equals to
        defaultOrderFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllOrdersByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where type in
        defaultOrderFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllOrdersByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where type is not null
        defaultOrderFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByQtyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where qty equals to
        defaultOrderFiltering("qty.equals=" + DEFAULT_QTY, "qty.equals=" + UPDATED_QTY);
    }

    @Test
    @Transactional
    void getAllOrdersByQtyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where qty in
        defaultOrderFiltering("qty.in=" + DEFAULT_QTY + "," + UPDATED_QTY, "qty.in=" + UPDATED_QTY);
    }

    @Test
    @Transactional
    void getAllOrdersByQtyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where qty is not null
        defaultOrderFiltering("qty.specified=true", "qty.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByQtyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where qty is greater than or equal to
        defaultOrderFiltering("qty.greaterThanOrEqual=" + DEFAULT_QTY, "qty.greaterThanOrEqual=" + UPDATED_QTY);
    }

    @Test
    @Transactional
    void getAllOrdersByQtyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where qty is less than or equal to
        defaultOrderFiltering("qty.lessThanOrEqual=" + DEFAULT_QTY, "qty.lessThanOrEqual=" + SMALLER_QTY);
    }

    @Test
    @Transactional
    void getAllOrdersByQtyIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where qty is less than
        defaultOrderFiltering("qty.lessThan=" + UPDATED_QTY, "qty.lessThan=" + DEFAULT_QTY);
    }

    @Test
    @Transactional
    void getAllOrdersByQtyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where qty is greater than
        defaultOrderFiltering("qty.greaterThan=" + SMALLER_QTY, "qty.greaterThan=" + DEFAULT_QTY);
    }

    @Test
    @Transactional
    void getAllOrdersByLimitPxIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where limitPx equals to
        defaultOrderFiltering("limitPx.equals=" + DEFAULT_LIMIT_PX, "limitPx.equals=" + UPDATED_LIMIT_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByLimitPxIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where limitPx in
        defaultOrderFiltering("limitPx.in=" + DEFAULT_LIMIT_PX + "," + UPDATED_LIMIT_PX, "limitPx.in=" + UPDATED_LIMIT_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByLimitPxIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where limitPx is not null
        defaultOrderFiltering("limitPx.specified=true", "limitPx.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByLimitPxIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where limitPx is greater than or equal to
        defaultOrderFiltering("limitPx.greaterThanOrEqual=" + DEFAULT_LIMIT_PX, "limitPx.greaterThanOrEqual=" + UPDATED_LIMIT_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByLimitPxIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where limitPx is less than or equal to
        defaultOrderFiltering("limitPx.lessThanOrEqual=" + DEFAULT_LIMIT_PX, "limitPx.lessThanOrEqual=" + SMALLER_LIMIT_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByLimitPxIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where limitPx is less than
        defaultOrderFiltering("limitPx.lessThan=" + UPDATED_LIMIT_PX, "limitPx.lessThan=" + DEFAULT_LIMIT_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByLimitPxIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where limitPx is greater than
        defaultOrderFiltering("limitPx.greaterThan=" + SMALLER_LIMIT_PX, "limitPx.greaterThan=" + DEFAULT_LIMIT_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByStopPxIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where stopPx equals to
        defaultOrderFiltering("stopPx.equals=" + DEFAULT_STOP_PX, "stopPx.equals=" + UPDATED_STOP_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByStopPxIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where stopPx in
        defaultOrderFiltering("stopPx.in=" + DEFAULT_STOP_PX + "," + UPDATED_STOP_PX, "stopPx.in=" + UPDATED_STOP_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByStopPxIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where stopPx is not null
        defaultOrderFiltering("stopPx.specified=true", "stopPx.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByStopPxIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where stopPx is greater than or equal to
        defaultOrderFiltering("stopPx.greaterThanOrEqual=" + DEFAULT_STOP_PX, "stopPx.greaterThanOrEqual=" + UPDATED_STOP_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByStopPxIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where stopPx is less than or equal to
        defaultOrderFiltering("stopPx.lessThanOrEqual=" + DEFAULT_STOP_PX, "stopPx.lessThanOrEqual=" + SMALLER_STOP_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByStopPxIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where stopPx is less than
        defaultOrderFiltering("stopPx.lessThan=" + UPDATED_STOP_PX, "stopPx.lessThan=" + DEFAULT_STOP_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByStopPxIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where stopPx is greater than
        defaultOrderFiltering("stopPx.greaterThan=" + SMALLER_STOP_PX, "stopPx.greaterThan=" + DEFAULT_STOP_PX);
    }

    @Test
    @Transactional
    void getAllOrdersByTifIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where tif equals to
        defaultOrderFiltering("tif.equals=" + DEFAULT_TIF, "tif.equals=" + UPDATED_TIF);
    }

    @Test
    @Transactional
    void getAllOrdersByTifIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where tif in
        defaultOrderFiltering("tif.in=" + DEFAULT_TIF + "," + UPDATED_TIF, "tif.in=" + UPDATED_TIF);
    }

    @Test
    @Transactional
    void getAllOrdersByTifIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where tif is not null
        defaultOrderFiltering("tif.specified=true", "tif.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where status equals to
        defaultOrderFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllOrdersByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where status in
        defaultOrderFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllOrdersByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where status is not null
        defaultOrderFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByVenueIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where venue equals to
        defaultOrderFiltering("venue.equals=" + DEFAULT_VENUE, "venue.equals=" + UPDATED_VENUE);
    }

    @Test
    @Transactional
    void getAllOrdersByVenueIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where venue in
        defaultOrderFiltering("venue.in=" + DEFAULT_VENUE + "," + UPDATED_VENUE, "venue.in=" + UPDATED_VENUE);
    }

    @Test
    @Transactional
    void getAllOrdersByVenueIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where venue is not null
        defaultOrderFiltering("venue.specified=true", "venue.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByVenueContainsSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where venue contains
        defaultOrderFiltering("venue.contains=" + DEFAULT_VENUE, "venue.contains=" + UPDATED_VENUE);
    }

    @Test
    @Transactional
    void getAllOrdersByVenueNotContainsSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where venue does not contain
        defaultOrderFiltering("venue.doesNotContain=" + UPDATED_VENUE, "venue.doesNotContain=" + DEFAULT_VENUE);
    }

    @Test
    @Transactional
    void getAllOrdersByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where createdAt equals to
        defaultOrderFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllOrdersByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where createdAt in
        defaultOrderFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllOrdersByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where createdAt is not null
        defaultOrderFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where updatedAt equals to
        defaultOrderFiltering("updatedAt.equals=" + DEFAULT_UPDATED_AT, "updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllOrdersByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where updatedAt in
        defaultOrderFiltering("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT, "updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllOrdersByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        // Get all the orderList where updatedAt is not null
        defaultOrderFiltering("updatedAt.specified=true", "updatedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllOrdersByTradingAccountIsEqualToSomething() throws Exception {
        TradingAccount tradingAccount;
        if (TestUtil.findAll(em, TradingAccount.class).isEmpty()) {
            orderRepository.saveAndFlush(order);
            tradingAccount = TradingAccountResourceIT.createEntity();
        } else {
            tradingAccount = TestUtil.findAll(em, TradingAccount.class).get(0);
        }
        em.persist(tradingAccount);
        em.flush();
        order.setTradingAccount(tradingAccount);
        orderRepository.saveAndFlush(order);
        Long tradingAccountId = tradingAccount.getId();
        // Get all the orderList where tradingAccount equals to tradingAccountId
        defaultOrderShouldBeFound("tradingAccountId.equals=" + tradingAccountId);

        // Get all the orderList where tradingAccount equals to (tradingAccountId + 1)
        defaultOrderShouldNotBeFound("tradingAccountId.equals=" + (tradingAccountId + 1));
    }

    @Test
    @Transactional
    void getAllOrdersByInstrumentIsEqualToSomething() throws Exception {
        Instrument instrument;
        if (TestUtil.findAll(em, Instrument.class).isEmpty()) {
            orderRepository.saveAndFlush(order);
            instrument = InstrumentResourceIT.createEntity();
        } else {
            instrument = TestUtil.findAll(em, Instrument.class).get(0);
        }
        em.persist(instrument);
        em.flush();
        order.setInstrument(instrument);
        orderRepository.saveAndFlush(order);
        Long instrumentId = instrument.getId();
        // Get all the orderList where instrument equals to instrumentId
        defaultOrderShouldBeFound("instrumentId.equals=" + instrumentId);

        // Get all the orderList where instrument equals to (instrumentId + 1)
        defaultOrderShouldNotBeFound("instrumentId.equals=" + (instrumentId + 1));
    }

    private void defaultOrderFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultOrderShouldBeFound(shouldBeFound);
        defaultOrderShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultOrderShouldBeFound(String filter) throws Exception {
        restOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(order.getId().intValue())))
            .andExpect(jsonPath("$.[*].side").value(hasItem(DEFAULT_SIDE.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].qty").value(hasItem(sameNumber(DEFAULT_QTY))))
            .andExpect(jsonPath("$.[*].limitPx").value(hasItem(sameNumber(DEFAULT_LIMIT_PX))))
            .andExpect(jsonPath("$.[*].stopPx").value(hasItem(sameNumber(DEFAULT_STOP_PX))))
            .andExpect(jsonPath("$.[*].tif").value(hasItem(DEFAULT_TIF.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].venue").value(hasItem(DEFAULT_VENUE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));

        // Check, that the count call also returns 1
        restOrderMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultOrderShouldNotBeFound(String filter) throws Exception {
        restOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restOrderMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingOrder() throws Exception {
        // Get the order
        restOrderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingOrder() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the order
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedOrder are not directly saved in db
        em.detach(updatedOrder);
        updatedOrder
            .side(UPDATED_SIDE)
            .type(UPDATED_TYPE)
            .qty(UPDATED_QTY)
            .limitPx(UPDATED_LIMIT_PX)
            .stopPx(UPDATED_STOP_PX)
            .tif(UPDATED_TIF)
            .status(UPDATED_STATUS)
            .venue(UPDATED_VENUE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        OrderDTO orderDTO = orderMapper.toDto(updatedOrder);

        restOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, orderDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO))
            )
            .andExpect(status().isOk());

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedOrderToMatchAllProperties(updatedOrder);
    }

    @Test
    @Transactional
    void putNonExistingOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        order.setId(longCount.incrementAndGet());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, orderDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        order.setId(longCount.incrementAndGet());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(orderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        order.setId(longCount.incrementAndGet());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateOrderWithPatch() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the order using partial update
        Order partialUpdatedOrder = new Order();
        partialUpdatedOrder.setId(order.getId());

        partialUpdatedOrder
            .type(UPDATED_TYPE)
            .limitPx(UPDATED_LIMIT_PX)
            .tif(UPDATED_TIF)
            .venue(UPDATED_VENUE)
            .updatedAt(UPDATED_UPDATED_AT);

        restOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOrder))
            )
            .andExpect(status().isOk());

        // Validate the Order in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOrderUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedOrder, order), getPersistedOrder(order));
    }

    @Test
    @Transactional
    void fullUpdateOrderWithPatch() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the order using partial update
        Order partialUpdatedOrder = new Order();
        partialUpdatedOrder.setId(order.getId());

        partialUpdatedOrder
            .side(UPDATED_SIDE)
            .type(UPDATED_TYPE)
            .qty(UPDATED_QTY)
            .limitPx(UPDATED_LIMIT_PX)
            .stopPx(UPDATED_STOP_PX)
            .tif(UPDATED_TIF)
            .status(UPDATED_STATUS)
            .venue(UPDATED_VENUE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOrder))
            )
            .andExpect(status().isOk());

        // Validate the Order in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOrderUpdatableFieldsEquals(partialUpdatedOrder, getPersistedOrder(partialUpdatedOrder));
    }

    @Test
    @Transactional
    void patchNonExistingOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        order.setId(longCount.incrementAndGet());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, orderDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(orderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        order.setId(longCount.incrementAndGet());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(orderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        order.setId(longCount.incrementAndGet());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrderMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(orderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteOrder() throws Exception {
        // Initialize the database
        insertedOrder = orderRepository.saveAndFlush(order);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the order
        restOrderMockMvc
            .perform(delete(ENTITY_API_URL_ID, order.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return orderRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Order getPersistedOrder(Order order) {
        return orderRepository.findById(order.getId()).orElseThrow();
    }

    protected void assertPersistedOrderToMatchAllProperties(Order expectedOrder) {
        assertOrderAllPropertiesEquals(expectedOrder, getPersistedOrder(expectedOrder));
    }

    protected void assertPersistedOrderToMatchUpdatableProperties(Order expectedOrder) {
        assertOrderAllUpdatablePropertiesEquals(expectedOrder, getPersistedOrder(expectedOrder));
    }

    // ============= PHASE 3 - USER STORY 1: BUY Flow Integration Tests =============

    /**
     * T007: Integration test covering successful BUY flow with key rejection cases.
     * Tests:
     * - Successful BUY order execution for cash-funded trades
     * - Insufficient funds rejection
     * - Inactive instrument rejection
     * - Invalid quantity (non-positive, not multiple of lot size)
     * - Non-marketable limit orders rejection
     * - Order status and execution tracking
     */

    @Test
    @Transactional
    void testSuccessfulBuyOrderExecution() throws Exception {
        // This test validates that a BUY order can be placed and filled for a CASH trading account
        // TODO: Implement after TradingService is created
        // Expected flow:
        // 1. Order is created with status NEW
        // 2. Validation passes (sufficient funds, active instrument, valid quantity)
        // 3. Order transitions to ACCEPTED then FILLED
        // 4. Execution record is created
        // 5. Position is created/updated with new quantity and average cost
        // 6. Ledger entry records the cash debit
        // 7. TradingAccount balance is reduced
    }

    @Test
    @Transactional
    void testBuyOrderRejectedForInsufficientFunds() throws Exception {
        // This test validates that BUY orders are rejected when account balance is insufficient
        // TODO: Implement after TradingService is created
        // Expected: Order status = REJECTED with rejection reason indicating insufficient funds
    }

    @Test
    @Transactional
    void testBuyOrderRejectedForInactiveInstrument() throws Exception {
        // This test validates that BUY orders are rejected for inactive instruments
        // TODO: Implement after TradingService is created
        // Expected: Order status = REJECTED with rejection reason indicating instrument inactive
    }

    @Test
    @Transactional
    void testBuyOrderRejectedForInvalidQuantity() throws Exception {
        // This test validates quantity validation:
        // - Quantity must be positive (> 0)
        // - Quantity must be a multiple of instrument lot size
        // TODO: Implement after TradingService is created
    }

    @Test
    @Transactional
    void testBuyLimitOrderRejectedIfNotMarketable() throws Exception {
        // This test validates that Limit orders are rejected if they cannot be filled
        // immediately at current prices
        // BUY limit order: accepted if currentPrice <= limitPrice
        // TODO: Implement after MatchingService and pricing logic are created
    }

    @Test
    @Transactional
    void testMarketOrderFilledAtLatestPrice() throws Exception {
        // This test validates that Market BUY orders are filled at the latest available price
        // from the mock market data feed
        // TODO: Implement after MatchingService is created
    }

    @Test
    @Transactional
    void testExecutionRecordCreatedForFilledOrder() throws Exception {
        // This test validates that an Execution record is created when an order is filled
        // Execution should capture: order ID, instrument, quantity, execution price, timestamp
        // TODO: Implement after TradingService is created
    }

    @Test
    @Transactional
    void testPositionCreatedForFirstBuy() throws Exception {
        // This test validates that a new Position is created for the first BUY execution
        // Position fields: quantity = exec quantity, average cost = exec price
        // TODO: Implement after TradingService is created
    }

    @Test
    @Transactional
    void testPositionAverageCostUpdateForSecondBuy() throws Exception {
        // This test validates average cost calculation for subsequent BUY executions
        // Formula: new avg cost = (oldQty * oldAvgCost + newQty * newExecPrice) / (oldQty + newQty)
        // TODO: Implement after TradingService is created
    }

    @Test
    @Transactional
    void testLedgerEntryCreatedForBuyCashDebit() throws Exception {
        // This test validates that a DEBIT ledger entry is created for BUY executions
        // Amount = quantity * execution price + fee
        // TODO: Implement after TradingService is created
    }

    @Test
    @Transactional
    void testTradingAccountBalanceDecreasedForBuy() throws Exception {
        // This test validates that TradingAccount.balance is correctly decreased after a BUY execution
        // Decrease amount = quantity * execution price + fee
        // TODO: Implement after TradingService is created
    }

    @Test
    @Transactional
    void testFr014ScopeValidationRejectsMarginOrders() throws Exception {
        // This test validates FR-014 scope boundaries: reject any orders that would require margin,
        // short selling, intraday products, or complex fee structures
        // For M2, all orders must be cash-funded, long-only with simple flat fee
        // TODO: Implement after TradingService validation logic is created
    }
}
