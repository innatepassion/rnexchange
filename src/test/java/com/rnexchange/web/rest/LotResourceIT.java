package com.rnexchange.web.rest;

import static com.rnexchange.domain.LotAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static com.rnexchange.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Lot;
import com.rnexchange.domain.Position;
import com.rnexchange.repository.LotRepository;
import com.rnexchange.service.dto.LotDTO;
import com.rnexchange.service.mapper.LotMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link LotResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class LotResourceIT {

    private static final Instant DEFAULT_OPEN_TS = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_OPEN_TS = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final BigDecimal DEFAULT_OPEN_PX = new BigDecimal(1);
    private static final BigDecimal UPDATED_OPEN_PX = new BigDecimal(2);
    private static final BigDecimal SMALLER_OPEN_PX = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_QTY_OPEN = new BigDecimal(1);
    private static final BigDecimal UPDATED_QTY_OPEN = new BigDecimal(2);
    private static final BigDecimal SMALLER_QTY_OPEN = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_QTY_CLOSED = new BigDecimal(1);
    private static final BigDecimal UPDATED_QTY_CLOSED = new BigDecimal(2);
    private static final BigDecimal SMALLER_QTY_CLOSED = new BigDecimal(1 - 1);

    private static final String DEFAULT_METHOD = "AAAAAAAAAA";
    private static final String UPDATED_METHOD = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/lots";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private LotMapper lotMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restLotMockMvc;

    private Lot lot;

    private Lot insertedLot;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Lot createEntity() {
        return new Lot()
            .openTs(DEFAULT_OPEN_TS)
            .openPx(DEFAULT_OPEN_PX)
            .qtyOpen(DEFAULT_QTY_OPEN)
            .qtyClosed(DEFAULT_QTY_CLOSED)
            .method(DEFAULT_METHOD);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Lot createUpdatedEntity() {
        return new Lot()
            .openTs(UPDATED_OPEN_TS)
            .openPx(UPDATED_OPEN_PX)
            .qtyOpen(UPDATED_QTY_OPEN)
            .qtyClosed(UPDATED_QTY_CLOSED)
            .method(UPDATED_METHOD);
    }

    @BeforeEach
    void initTest() {
        lot = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedLot != null) {
            lotRepository.delete(insertedLot);
            insertedLot = null;
        }
    }

    @Test
    @Transactional
    void createLot() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Lot
        LotDTO lotDTO = lotMapper.toDto(lot);
        var returnedLotDTO = om.readValue(
            restLotMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(lotDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            LotDTO.class
        );

        // Validate the Lot in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedLot = lotMapper.toEntity(returnedLotDTO);
        assertLotUpdatableFieldsEquals(returnedLot, getPersistedLot(returnedLot));

        insertedLot = returnedLot;
    }

    @Test
    @Transactional
    void createLotWithExistingId() throws Exception {
        // Create the Lot with an existing ID
        lot.setId(1L);
        LotDTO lotDTO = lotMapper.toDto(lot);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restLotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(lotDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Lot in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkOpenTsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        lot.setOpenTs(null);

        // Create the Lot, which fails.
        LotDTO lotDTO = lotMapper.toDto(lot);

        restLotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(lotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOpenPxIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        lot.setOpenPx(null);

        // Create the Lot, which fails.
        LotDTO lotDTO = lotMapper.toDto(lot);

        restLotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(lotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkQtyOpenIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        lot.setQtyOpen(null);

        // Create the Lot, which fails.
        LotDTO lotDTO = lotMapper.toDto(lot);

        restLotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(lotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkQtyClosedIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        lot.setQtyClosed(null);

        // Create the Lot, which fails.
        LotDTO lotDTO = lotMapper.toDto(lot);

        restLotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(lotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllLots() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList
        restLotMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(lot.getId().intValue())))
            .andExpect(jsonPath("$.[*].openTs").value(hasItem(DEFAULT_OPEN_TS.toString())))
            .andExpect(jsonPath("$.[*].openPx").value(hasItem(sameNumber(DEFAULT_OPEN_PX))))
            .andExpect(jsonPath("$.[*].qtyOpen").value(hasItem(sameNumber(DEFAULT_QTY_OPEN))))
            .andExpect(jsonPath("$.[*].qtyClosed").value(hasItem(sameNumber(DEFAULT_QTY_CLOSED))))
            .andExpect(jsonPath("$.[*].method").value(hasItem(DEFAULT_METHOD)));
    }

    @Test
    @Transactional
    void getLot() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get the lot
        restLotMockMvc
            .perform(get(ENTITY_API_URL_ID, lot.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(lot.getId().intValue()))
            .andExpect(jsonPath("$.openTs").value(DEFAULT_OPEN_TS.toString()))
            .andExpect(jsonPath("$.openPx").value(sameNumber(DEFAULT_OPEN_PX)))
            .andExpect(jsonPath("$.qtyOpen").value(sameNumber(DEFAULT_QTY_OPEN)))
            .andExpect(jsonPath("$.qtyClosed").value(sameNumber(DEFAULT_QTY_CLOSED)))
            .andExpect(jsonPath("$.method").value(DEFAULT_METHOD));
    }

    @Test
    @Transactional
    void getLotsByIdFiltering() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        Long id = lot.getId();

        defaultLotFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultLotFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultLotFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllLotsByOpenTsIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where openTs equals to
        defaultLotFiltering("openTs.equals=" + DEFAULT_OPEN_TS, "openTs.equals=" + UPDATED_OPEN_TS);
    }

    @Test
    @Transactional
    void getAllLotsByOpenTsIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where openTs in
        defaultLotFiltering("openTs.in=" + DEFAULT_OPEN_TS + "," + UPDATED_OPEN_TS, "openTs.in=" + UPDATED_OPEN_TS);
    }

    @Test
    @Transactional
    void getAllLotsByOpenTsIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where openTs is not null
        defaultLotFiltering("openTs.specified=true", "openTs.specified=false");
    }

    @Test
    @Transactional
    void getAllLotsByOpenPxIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where openPx equals to
        defaultLotFiltering("openPx.equals=" + DEFAULT_OPEN_PX, "openPx.equals=" + UPDATED_OPEN_PX);
    }

    @Test
    @Transactional
    void getAllLotsByOpenPxIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where openPx in
        defaultLotFiltering("openPx.in=" + DEFAULT_OPEN_PX + "," + UPDATED_OPEN_PX, "openPx.in=" + UPDATED_OPEN_PX);
    }

    @Test
    @Transactional
    void getAllLotsByOpenPxIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where openPx is not null
        defaultLotFiltering("openPx.specified=true", "openPx.specified=false");
    }

    @Test
    @Transactional
    void getAllLotsByOpenPxIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where openPx is greater than or equal to
        defaultLotFiltering("openPx.greaterThanOrEqual=" + DEFAULT_OPEN_PX, "openPx.greaterThanOrEqual=" + UPDATED_OPEN_PX);
    }

    @Test
    @Transactional
    void getAllLotsByOpenPxIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where openPx is less than or equal to
        defaultLotFiltering("openPx.lessThanOrEqual=" + DEFAULT_OPEN_PX, "openPx.lessThanOrEqual=" + SMALLER_OPEN_PX);
    }

    @Test
    @Transactional
    void getAllLotsByOpenPxIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where openPx is less than
        defaultLotFiltering("openPx.lessThan=" + UPDATED_OPEN_PX, "openPx.lessThan=" + DEFAULT_OPEN_PX);
    }

    @Test
    @Transactional
    void getAllLotsByOpenPxIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where openPx is greater than
        defaultLotFiltering("openPx.greaterThan=" + SMALLER_OPEN_PX, "openPx.greaterThan=" + DEFAULT_OPEN_PX);
    }

    @Test
    @Transactional
    void getAllLotsByQtyOpenIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyOpen equals to
        defaultLotFiltering("qtyOpen.equals=" + DEFAULT_QTY_OPEN, "qtyOpen.equals=" + UPDATED_QTY_OPEN);
    }

    @Test
    @Transactional
    void getAllLotsByQtyOpenIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyOpen in
        defaultLotFiltering("qtyOpen.in=" + DEFAULT_QTY_OPEN + "," + UPDATED_QTY_OPEN, "qtyOpen.in=" + UPDATED_QTY_OPEN);
    }

    @Test
    @Transactional
    void getAllLotsByQtyOpenIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyOpen is not null
        defaultLotFiltering("qtyOpen.specified=true", "qtyOpen.specified=false");
    }

    @Test
    @Transactional
    void getAllLotsByQtyOpenIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyOpen is greater than or equal to
        defaultLotFiltering("qtyOpen.greaterThanOrEqual=" + DEFAULT_QTY_OPEN, "qtyOpen.greaterThanOrEqual=" + UPDATED_QTY_OPEN);
    }

    @Test
    @Transactional
    void getAllLotsByQtyOpenIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyOpen is less than or equal to
        defaultLotFiltering("qtyOpen.lessThanOrEqual=" + DEFAULT_QTY_OPEN, "qtyOpen.lessThanOrEqual=" + SMALLER_QTY_OPEN);
    }

    @Test
    @Transactional
    void getAllLotsByQtyOpenIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyOpen is less than
        defaultLotFiltering("qtyOpen.lessThan=" + UPDATED_QTY_OPEN, "qtyOpen.lessThan=" + DEFAULT_QTY_OPEN);
    }

    @Test
    @Transactional
    void getAllLotsByQtyOpenIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyOpen is greater than
        defaultLotFiltering("qtyOpen.greaterThan=" + SMALLER_QTY_OPEN, "qtyOpen.greaterThan=" + DEFAULT_QTY_OPEN);
    }

    @Test
    @Transactional
    void getAllLotsByQtyClosedIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyClosed equals to
        defaultLotFiltering("qtyClosed.equals=" + DEFAULT_QTY_CLOSED, "qtyClosed.equals=" + UPDATED_QTY_CLOSED);
    }

    @Test
    @Transactional
    void getAllLotsByQtyClosedIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyClosed in
        defaultLotFiltering("qtyClosed.in=" + DEFAULT_QTY_CLOSED + "," + UPDATED_QTY_CLOSED, "qtyClosed.in=" + UPDATED_QTY_CLOSED);
    }

    @Test
    @Transactional
    void getAllLotsByQtyClosedIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyClosed is not null
        defaultLotFiltering("qtyClosed.specified=true", "qtyClosed.specified=false");
    }

    @Test
    @Transactional
    void getAllLotsByQtyClosedIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyClosed is greater than or equal to
        defaultLotFiltering("qtyClosed.greaterThanOrEqual=" + DEFAULT_QTY_CLOSED, "qtyClosed.greaterThanOrEqual=" + UPDATED_QTY_CLOSED);
    }

    @Test
    @Transactional
    void getAllLotsByQtyClosedIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyClosed is less than or equal to
        defaultLotFiltering("qtyClosed.lessThanOrEqual=" + DEFAULT_QTY_CLOSED, "qtyClosed.lessThanOrEqual=" + SMALLER_QTY_CLOSED);
    }

    @Test
    @Transactional
    void getAllLotsByQtyClosedIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyClosed is less than
        defaultLotFiltering("qtyClosed.lessThan=" + UPDATED_QTY_CLOSED, "qtyClosed.lessThan=" + DEFAULT_QTY_CLOSED);
    }

    @Test
    @Transactional
    void getAllLotsByQtyClosedIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where qtyClosed is greater than
        defaultLotFiltering("qtyClosed.greaterThan=" + SMALLER_QTY_CLOSED, "qtyClosed.greaterThan=" + DEFAULT_QTY_CLOSED);
    }

    @Test
    @Transactional
    void getAllLotsByMethodIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where method equals to
        defaultLotFiltering("method.equals=" + DEFAULT_METHOD, "method.equals=" + UPDATED_METHOD);
    }

    @Test
    @Transactional
    void getAllLotsByMethodIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where method in
        defaultLotFiltering("method.in=" + DEFAULT_METHOD + "," + UPDATED_METHOD, "method.in=" + UPDATED_METHOD);
    }

    @Test
    @Transactional
    void getAllLotsByMethodIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where method is not null
        defaultLotFiltering("method.specified=true", "method.specified=false");
    }

    @Test
    @Transactional
    void getAllLotsByMethodContainsSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where method contains
        defaultLotFiltering("method.contains=" + DEFAULT_METHOD, "method.contains=" + UPDATED_METHOD);
    }

    @Test
    @Transactional
    void getAllLotsByMethodNotContainsSomething() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        // Get all the lotList where method does not contain
        defaultLotFiltering("method.doesNotContain=" + UPDATED_METHOD, "method.doesNotContain=" + DEFAULT_METHOD);
    }

    @Test
    @Transactional
    void getAllLotsByPositionIsEqualToSomething() throws Exception {
        Position position;
        if (TestUtil.findAll(em, Position.class).isEmpty()) {
            lotRepository.saveAndFlush(lot);
            position = PositionResourceIT.createEntity();
        } else {
            position = TestUtil.findAll(em, Position.class).get(0);
        }
        em.persist(position);
        em.flush();
        lot.setPosition(position);
        lotRepository.saveAndFlush(lot);
        Long positionId = position.getId();
        // Get all the lotList where position equals to positionId
        defaultLotShouldBeFound("positionId.equals=" + positionId);

        // Get all the lotList where position equals to (positionId + 1)
        defaultLotShouldNotBeFound("positionId.equals=" + (positionId + 1));
    }

    private void defaultLotFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultLotShouldBeFound(shouldBeFound);
        defaultLotShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultLotShouldBeFound(String filter) throws Exception {
        restLotMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(lot.getId().intValue())))
            .andExpect(jsonPath("$.[*].openTs").value(hasItem(DEFAULT_OPEN_TS.toString())))
            .andExpect(jsonPath("$.[*].openPx").value(hasItem(sameNumber(DEFAULT_OPEN_PX))))
            .andExpect(jsonPath("$.[*].qtyOpen").value(hasItem(sameNumber(DEFAULT_QTY_OPEN))))
            .andExpect(jsonPath("$.[*].qtyClosed").value(hasItem(sameNumber(DEFAULT_QTY_CLOSED))))
            .andExpect(jsonPath("$.[*].method").value(hasItem(DEFAULT_METHOD)));

        // Check, that the count call also returns 1
        restLotMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultLotShouldNotBeFound(String filter) throws Exception {
        restLotMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restLotMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingLot() throws Exception {
        // Get the lot
        restLotMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingLot() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the lot
        Lot updatedLot = lotRepository.findById(lot.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedLot are not directly saved in db
        em.detach(updatedLot);
        updatedLot
            .openTs(UPDATED_OPEN_TS)
            .openPx(UPDATED_OPEN_PX)
            .qtyOpen(UPDATED_QTY_OPEN)
            .qtyClosed(UPDATED_QTY_CLOSED)
            .method(UPDATED_METHOD);
        LotDTO lotDTO = lotMapper.toDto(updatedLot);

        restLotMockMvc
            .perform(put(ENTITY_API_URL_ID, lotDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(lotDTO)))
            .andExpect(status().isOk());

        // Validate the Lot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedLotToMatchAllProperties(updatedLot);
    }

    @Test
    @Transactional
    void putNonExistingLot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lot.setId(longCount.incrementAndGet());

        // Create the Lot
        LotDTO lotDTO = lotMapper.toDto(lot);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLotMockMvc
            .perform(put(ENTITY_API_URL_ID, lotDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(lotDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Lot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchLot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lot.setId(longCount.incrementAndGet());

        // Create the Lot
        LotDTO lotDTO = lotMapper.toDto(lot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLotMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(lotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Lot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamLot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lot.setId(longCount.incrementAndGet());

        // Create the Lot
        LotDTO lotDTO = lotMapper.toDto(lot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLotMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(lotDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Lot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateLotWithPatch() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the lot using partial update
        Lot partialUpdatedLot = new Lot();
        partialUpdatedLot.setId(lot.getId());

        partialUpdatedLot.openTs(UPDATED_OPEN_TS).qtyOpen(UPDATED_QTY_OPEN).qtyClosed(UPDATED_QTY_CLOSED);

        restLotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLot.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLot))
            )
            .andExpect(status().isOk());

        // Validate the Lot in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLotUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedLot, lot), getPersistedLot(lot));
    }

    @Test
    @Transactional
    void fullUpdateLotWithPatch() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the lot using partial update
        Lot partialUpdatedLot = new Lot();
        partialUpdatedLot.setId(lot.getId());

        partialUpdatedLot
            .openTs(UPDATED_OPEN_TS)
            .openPx(UPDATED_OPEN_PX)
            .qtyOpen(UPDATED_QTY_OPEN)
            .qtyClosed(UPDATED_QTY_CLOSED)
            .method(UPDATED_METHOD);

        restLotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLot.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLot))
            )
            .andExpect(status().isOk());

        // Validate the Lot in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLotUpdatableFieldsEquals(partialUpdatedLot, getPersistedLot(partialUpdatedLot));
    }

    @Test
    @Transactional
    void patchNonExistingLot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lot.setId(longCount.incrementAndGet());

        // Create the Lot
        LotDTO lotDTO = lotMapper.toDto(lot);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, lotDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(lotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Lot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchLot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lot.setId(longCount.incrementAndGet());

        // Create the Lot
        LotDTO lotDTO = lotMapper.toDto(lot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(lotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Lot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamLot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        lot.setId(longCount.incrementAndGet());

        // Create the Lot
        LotDTO lotDTO = lotMapper.toDto(lot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLotMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(lotDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Lot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteLot() throws Exception {
        // Initialize the database
        insertedLot = lotRepository.saveAndFlush(lot);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the lot
        restLotMockMvc.perform(delete(ENTITY_API_URL_ID, lot.getId()).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return lotRepository.count();
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

    protected Lot getPersistedLot(Lot lot) {
        return lotRepository.findById(lot.getId()).orElseThrow();
    }

    protected void assertPersistedLotToMatchAllProperties(Lot expectedLot) {
        assertLotAllPropertiesEquals(expectedLot, getPersistedLot(expectedLot));
    }

    protected void assertPersistedLotToMatchUpdatableProperties(Lot expectedLot) {
        assertLotAllUpdatablePropertiesEquals(expectedLot, getPersistedLot(expectedLot));
    }
}
