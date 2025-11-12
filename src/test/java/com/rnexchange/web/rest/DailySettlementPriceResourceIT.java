package com.rnexchange.web.rest;

import static com.rnexchange.domain.DailySettlementPriceAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static com.rnexchange.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.DailySettlementPrice;
import com.rnexchange.domain.Instrument;
import com.rnexchange.repository.DailySettlementPriceRepository;
import com.rnexchange.service.DailySettlementPriceService;
import com.rnexchange.service.dto.DailySettlementPriceDTO;
import com.rnexchange.service.mapper.DailySettlementPriceMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link DailySettlementPriceResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class DailySettlementPriceResourceIT {

    private static final LocalDate DEFAULT_REF_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_REF_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_REF_DATE = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_INSTRUMENT_SYMBOL = "AAAAAAAAAA";
    private static final String UPDATED_INSTRUMENT_SYMBOL = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_SETTLE_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_SETTLE_PRICE = new BigDecimal(2);
    private static final BigDecimal SMALLER_SETTLE_PRICE = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/daily-settlement-prices";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DailySettlementPriceRepository dailySettlementPriceRepository;

    @Mock
    private DailySettlementPriceRepository dailySettlementPriceRepositoryMock;

    @Autowired
    private DailySettlementPriceMapper dailySettlementPriceMapper;

    @Mock
    private DailySettlementPriceService dailySettlementPriceServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDailySettlementPriceMockMvc;

    private DailySettlementPrice dailySettlementPrice;

    private DailySettlementPrice insertedDailySettlementPrice;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DailySettlementPrice createEntity() {
        return new DailySettlementPrice()
            .refDate(DEFAULT_REF_DATE)
            .instrumentSymbol(DEFAULT_INSTRUMENT_SYMBOL)
            .settlePrice(DEFAULT_SETTLE_PRICE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DailySettlementPrice createUpdatedEntity() {
        return new DailySettlementPrice()
            .refDate(UPDATED_REF_DATE)
            .instrumentSymbol(UPDATED_INSTRUMENT_SYMBOL)
            .settlePrice(UPDATED_SETTLE_PRICE);
    }

    @BeforeEach
    void initTest() {
        dailySettlementPrice = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedDailySettlementPrice != null) {
            dailySettlementPriceRepository.delete(insertedDailySettlementPrice);
            insertedDailySettlementPrice = null;
        }
    }

    @Test
    @Transactional
    void createDailySettlementPrice() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DailySettlementPrice
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);
        var returnedDailySettlementPriceDTO = om.readValue(
            restDailySettlementPriceMockMvc
                .perform(
                    post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dailySettlementPriceDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DailySettlementPriceDTO.class
        );

        // Validate the DailySettlementPrice in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDailySettlementPrice = dailySettlementPriceMapper.toEntity(returnedDailySettlementPriceDTO);
        assertDailySettlementPriceUpdatableFieldsEquals(
            returnedDailySettlementPrice,
            getPersistedDailySettlementPrice(returnedDailySettlementPrice)
        );

        insertedDailySettlementPrice = returnedDailySettlementPrice;
    }

    @Test
    @Transactional
    void createDailySettlementPriceWithExistingId() throws Exception {
        // Create the DailySettlementPrice with an existing ID
        dailySettlementPrice.setId(1L);
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDailySettlementPriceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dailySettlementPriceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DailySettlementPrice in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRefDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dailySettlementPrice.setRefDate(null);

        // Create the DailySettlementPrice, which fails.
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);

        restDailySettlementPriceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dailySettlementPriceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkInstrumentSymbolIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dailySettlementPrice.setInstrumentSymbol(null);

        // Create the DailySettlementPrice, which fails.
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);

        restDailySettlementPriceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dailySettlementPriceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSettlePriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dailySettlementPrice.setSettlePrice(null);

        // Create the DailySettlementPrice, which fails.
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);

        restDailySettlementPriceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dailySettlementPriceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDailySettlementPrices() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList
        restDailySettlementPriceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dailySettlementPrice.getId().intValue())))
            .andExpect(jsonPath("$.[*].refDate").value(hasItem(DEFAULT_REF_DATE.toString())))
            .andExpect(jsonPath("$.[*].instrumentSymbol").value(hasItem(DEFAULT_INSTRUMENT_SYMBOL)))
            .andExpect(jsonPath("$.[*].settlePrice").value(hasItem(sameNumber(DEFAULT_SETTLE_PRICE))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDailySettlementPricesWithEagerRelationshipsIsEnabled() throws Exception {
        when(dailySettlementPriceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDailySettlementPriceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(dailySettlementPriceServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDailySettlementPricesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(dailySettlementPriceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDailySettlementPriceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(dailySettlementPriceRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getDailySettlementPrice() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get the dailySettlementPrice
        restDailySettlementPriceMockMvc
            .perform(get(ENTITY_API_URL_ID, dailySettlementPrice.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(dailySettlementPrice.getId().intValue()))
            .andExpect(jsonPath("$.refDate").value(DEFAULT_REF_DATE.toString()))
            .andExpect(jsonPath("$.instrumentSymbol").value(DEFAULT_INSTRUMENT_SYMBOL))
            .andExpect(jsonPath("$.settlePrice").value(sameNumber(DEFAULT_SETTLE_PRICE)));
    }

    @Test
    @Transactional
    void getDailySettlementPricesByIdFiltering() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        Long id = dailySettlementPrice.getId();

        defaultDailySettlementPriceFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultDailySettlementPriceFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultDailySettlementPriceFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByRefDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where refDate equals to
        defaultDailySettlementPriceFiltering("refDate.equals=" + DEFAULT_REF_DATE, "refDate.equals=" + UPDATED_REF_DATE);
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByRefDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where refDate in
        defaultDailySettlementPriceFiltering("refDate.in=" + DEFAULT_REF_DATE + "," + UPDATED_REF_DATE, "refDate.in=" + UPDATED_REF_DATE);
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByRefDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where refDate is not null
        defaultDailySettlementPriceFiltering("refDate.specified=true", "refDate.specified=false");
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByRefDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where refDate is greater than or equal to
        defaultDailySettlementPriceFiltering(
            "refDate.greaterThanOrEqual=" + DEFAULT_REF_DATE,
            "refDate.greaterThanOrEqual=" + UPDATED_REF_DATE
        );
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByRefDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where refDate is less than or equal to
        defaultDailySettlementPriceFiltering("refDate.lessThanOrEqual=" + DEFAULT_REF_DATE, "refDate.lessThanOrEqual=" + SMALLER_REF_DATE);
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByRefDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where refDate is less than
        defaultDailySettlementPriceFiltering("refDate.lessThan=" + UPDATED_REF_DATE, "refDate.lessThan=" + DEFAULT_REF_DATE);
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByRefDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where refDate is greater than
        defaultDailySettlementPriceFiltering("refDate.greaterThan=" + SMALLER_REF_DATE, "refDate.greaterThan=" + DEFAULT_REF_DATE);
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByInstrumentSymbolIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where instrumentSymbol equals to
        defaultDailySettlementPriceFiltering(
            "instrumentSymbol.equals=" + DEFAULT_INSTRUMENT_SYMBOL,
            "instrumentSymbol.equals=" + UPDATED_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByInstrumentSymbolIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where instrumentSymbol in
        defaultDailySettlementPriceFiltering(
            "instrumentSymbol.in=" + DEFAULT_INSTRUMENT_SYMBOL + "," + UPDATED_INSTRUMENT_SYMBOL,
            "instrumentSymbol.in=" + UPDATED_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByInstrumentSymbolIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where instrumentSymbol is not null
        defaultDailySettlementPriceFiltering("instrumentSymbol.specified=true", "instrumentSymbol.specified=false");
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByInstrumentSymbolContainsSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where instrumentSymbol contains
        defaultDailySettlementPriceFiltering(
            "instrumentSymbol.contains=" + DEFAULT_INSTRUMENT_SYMBOL,
            "instrumentSymbol.contains=" + UPDATED_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByInstrumentSymbolNotContainsSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where instrumentSymbol does not contain
        defaultDailySettlementPriceFiltering(
            "instrumentSymbol.doesNotContain=" + UPDATED_INSTRUMENT_SYMBOL,
            "instrumentSymbol.doesNotContain=" + DEFAULT_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesBySettlePriceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where settlePrice equals to
        defaultDailySettlementPriceFiltering("settlePrice.equals=" + DEFAULT_SETTLE_PRICE, "settlePrice.equals=" + UPDATED_SETTLE_PRICE);
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesBySettlePriceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where settlePrice in
        defaultDailySettlementPriceFiltering(
            "settlePrice.in=" + DEFAULT_SETTLE_PRICE + "," + UPDATED_SETTLE_PRICE,
            "settlePrice.in=" + UPDATED_SETTLE_PRICE
        );
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesBySettlePriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where settlePrice is not null
        defaultDailySettlementPriceFiltering("settlePrice.specified=true", "settlePrice.specified=false");
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesBySettlePriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where settlePrice is greater than or equal to
        defaultDailySettlementPriceFiltering(
            "settlePrice.greaterThanOrEqual=" + DEFAULT_SETTLE_PRICE,
            "settlePrice.greaterThanOrEqual=" + UPDATED_SETTLE_PRICE
        );
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesBySettlePriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where settlePrice is less than or equal to
        defaultDailySettlementPriceFiltering(
            "settlePrice.lessThanOrEqual=" + DEFAULT_SETTLE_PRICE,
            "settlePrice.lessThanOrEqual=" + SMALLER_SETTLE_PRICE
        );
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesBySettlePriceIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where settlePrice is less than
        defaultDailySettlementPriceFiltering(
            "settlePrice.lessThan=" + UPDATED_SETTLE_PRICE,
            "settlePrice.lessThan=" + DEFAULT_SETTLE_PRICE
        );
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesBySettlePriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        // Get all the dailySettlementPriceList where settlePrice is greater than
        defaultDailySettlementPriceFiltering(
            "settlePrice.greaterThan=" + SMALLER_SETTLE_PRICE,
            "settlePrice.greaterThan=" + DEFAULT_SETTLE_PRICE
        );
    }

    @Test
    @Transactional
    void getAllDailySettlementPricesByInstrumentIsEqualToSomething() throws Exception {
        Instrument instrument;
        if (TestUtil.findAll(em, Instrument.class).isEmpty()) {
            dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);
            instrument = InstrumentResourceIT.createEntity();
        } else {
            instrument = TestUtil.findAll(em, Instrument.class).get(0);
        }
        em.persist(instrument);
        em.flush();
        dailySettlementPrice.setInstrument(instrument);
        dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);
        Long instrumentId = instrument.getId();
        // Get all the dailySettlementPriceList where instrument equals to instrumentId
        defaultDailySettlementPriceShouldBeFound("instrumentId.equals=" + instrumentId);

        // Get all the dailySettlementPriceList where instrument equals to (instrumentId + 1)
        defaultDailySettlementPriceShouldNotBeFound("instrumentId.equals=" + (instrumentId + 1));
    }

    private void defaultDailySettlementPriceFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultDailySettlementPriceShouldBeFound(shouldBeFound);
        defaultDailySettlementPriceShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultDailySettlementPriceShouldBeFound(String filter) throws Exception {
        restDailySettlementPriceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dailySettlementPrice.getId().intValue())))
            .andExpect(jsonPath("$.[*].refDate").value(hasItem(DEFAULT_REF_DATE.toString())))
            .andExpect(jsonPath("$.[*].instrumentSymbol").value(hasItem(DEFAULT_INSTRUMENT_SYMBOL)))
            .andExpect(jsonPath("$.[*].settlePrice").value(hasItem(sameNumber(DEFAULT_SETTLE_PRICE))));

        // Check, that the count call also returns 1
        restDailySettlementPriceMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultDailySettlementPriceShouldNotBeFound(String filter) throws Exception {
        restDailySettlementPriceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDailySettlementPriceMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingDailySettlementPrice() throws Exception {
        // Get the dailySettlementPrice
        restDailySettlementPriceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDailySettlementPrice() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dailySettlementPrice
        DailySettlementPrice updatedDailySettlementPrice = dailySettlementPriceRepository
            .findById(dailySettlementPrice.getId())
            .orElseThrow();
        // Disconnect from session so that the updates on updatedDailySettlementPrice are not directly saved in db
        em.detach(updatedDailySettlementPrice);
        updatedDailySettlementPrice.refDate(UPDATED_REF_DATE).instrumentSymbol(UPDATED_INSTRUMENT_SYMBOL).settlePrice(UPDATED_SETTLE_PRICE);
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(updatedDailySettlementPrice);

        restDailySettlementPriceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dailySettlementPriceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dailySettlementPriceDTO))
            )
            .andExpect(status().isOk());

        // Validate the DailySettlementPrice in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDailySettlementPriceToMatchAllProperties(updatedDailySettlementPrice);
    }

    @Test
    @Transactional
    void putNonExistingDailySettlementPrice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dailySettlementPrice.setId(longCount.incrementAndGet());

        // Create the DailySettlementPrice
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDailySettlementPriceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dailySettlementPriceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dailySettlementPriceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DailySettlementPrice in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDailySettlementPrice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dailySettlementPrice.setId(longCount.incrementAndGet());

        // Create the DailySettlementPrice
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDailySettlementPriceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dailySettlementPriceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DailySettlementPrice in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDailySettlementPrice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dailySettlementPrice.setId(longCount.incrementAndGet());

        // Create the DailySettlementPrice
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDailySettlementPriceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dailySettlementPriceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DailySettlementPrice in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDailySettlementPriceWithPatch() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dailySettlementPrice using partial update
        DailySettlementPrice partialUpdatedDailySettlementPrice = new DailySettlementPrice();
        partialUpdatedDailySettlementPrice.setId(dailySettlementPrice.getId());

        restDailySettlementPriceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDailySettlementPrice.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDailySettlementPrice))
            )
            .andExpect(status().isOk());

        // Validate the DailySettlementPrice in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDailySettlementPriceUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDailySettlementPrice, dailySettlementPrice),
            getPersistedDailySettlementPrice(dailySettlementPrice)
        );
    }

    @Test
    @Transactional
    void fullUpdateDailySettlementPriceWithPatch() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dailySettlementPrice using partial update
        DailySettlementPrice partialUpdatedDailySettlementPrice = new DailySettlementPrice();
        partialUpdatedDailySettlementPrice.setId(dailySettlementPrice.getId());

        partialUpdatedDailySettlementPrice
            .refDate(UPDATED_REF_DATE)
            .instrumentSymbol(UPDATED_INSTRUMENT_SYMBOL)
            .settlePrice(UPDATED_SETTLE_PRICE);

        restDailySettlementPriceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDailySettlementPrice.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDailySettlementPrice))
            )
            .andExpect(status().isOk());

        // Validate the DailySettlementPrice in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDailySettlementPriceUpdatableFieldsEquals(
            partialUpdatedDailySettlementPrice,
            getPersistedDailySettlementPrice(partialUpdatedDailySettlementPrice)
        );
    }

    @Test
    @Transactional
    void patchNonExistingDailySettlementPrice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dailySettlementPrice.setId(longCount.incrementAndGet());

        // Create the DailySettlementPrice
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDailySettlementPriceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, dailySettlementPriceDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(dailySettlementPriceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DailySettlementPrice in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDailySettlementPrice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dailySettlementPrice.setId(longCount.incrementAndGet());

        // Create the DailySettlementPrice
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDailySettlementPriceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(dailySettlementPriceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DailySettlementPrice in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDailySettlementPrice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dailySettlementPrice.setId(longCount.incrementAndGet());

        // Create the DailySettlementPrice
        DailySettlementPriceDTO dailySettlementPriceDTO = dailySettlementPriceMapper.toDto(dailySettlementPrice);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDailySettlementPriceMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(dailySettlementPriceDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the DailySettlementPrice in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDailySettlementPrice() throws Exception {
        // Initialize the database
        insertedDailySettlementPrice = dailySettlementPriceRepository.saveAndFlush(dailySettlementPrice);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the dailySettlementPrice
        restDailySettlementPriceMockMvc
            .perform(delete(ENTITY_API_URL_ID, dailySettlementPrice.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return dailySettlementPriceRepository.count();
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

    protected DailySettlementPrice getPersistedDailySettlementPrice(DailySettlementPrice dailySettlementPrice) {
        return dailySettlementPriceRepository.findById(dailySettlementPrice.getId()).orElseThrow();
    }

    protected void assertPersistedDailySettlementPriceToMatchAllProperties(DailySettlementPrice expectedDailySettlementPrice) {
        assertDailySettlementPriceAllPropertiesEquals(
            expectedDailySettlementPrice,
            getPersistedDailySettlementPrice(expectedDailySettlementPrice)
        );
    }

    protected void assertPersistedDailySettlementPriceToMatchUpdatableProperties(DailySettlementPrice expectedDailySettlementPrice) {
        assertDailySettlementPriceAllUpdatablePropertiesEquals(
            expectedDailySettlementPrice,
            getPersistedDailySettlementPrice(expectedDailySettlementPrice)
        );
    }
}
