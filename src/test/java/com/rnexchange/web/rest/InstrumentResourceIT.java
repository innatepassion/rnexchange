package com.rnexchange.web.rest;

import static com.rnexchange.domain.InstrumentAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static com.rnexchange.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.service.InstrumentService;
import com.rnexchange.service.dto.InstrumentDTO;
import com.rnexchange.service.mapper.InstrumentMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link InstrumentResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class InstrumentResourceIT {

    private static final String DEFAULT_SYMBOL = "AAAAAAAAAA";
    private static final String UPDATED_SYMBOL = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final AssetClass DEFAULT_ASSET_CLASS = AssetClass.EQUITY;
    private static final AssetClass UPDATED_ASSET_CLASS = AssetClass.FUTURE;

    private static final String DEFAULT_EXCHANGE_CODE = "AAAAAAAAAA";
    private static final String UPDATED_EXCHANGE_CODE = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_TICK_SIZE = new BigDecimal(1);
    private static final BigDecimal UPDATED_TICK_SIZE = new BigDecimal(2);
    private static final BigDecimal SMALLER_TICK_SIZE = new BigDecimal(1 - 1);

    private static final Long DEFAULT_LOT_SIZE = 1L;
    private static final Long UPDATED_LOT_SIZE = 2L;
    private static final Long SMALLER_LOT_SIZE = 1L - 1L;

    private static final Currency DEFAULT_CURRENCY = Currency.INR;
    private static final Currency UPDATED_CURRENCY = Currency.USD;

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/instruments";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Mock
    private InstrumentRepository instrumentRepositoryMock;

    @Autowired
    private InstrumentMapper instrumentMapper;

    @Mock
    private InstrumentService instrumentServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInstrumentMockMvc;

    private Instrument instrument;

    private Instrument insertedInstrument;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Instrument createEntity() {
        return new Instrument()
            .symbol(DEFAULT_SYMBOL)
            .name(DEFAULT_NAME)
            .assetClass(DEFAULT_ASSET_CLASS)
            .exchangeCode(DEFAULT_EXCHANGE_CODE)
            .tickSize(DEFAULT_TICK_SIZE)
            .lotSize(DEFAULT_LOT_SIZE)
            .currency(DEFAULT_CURRENCY)
            .status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Instrument createUpdatedEntity() {
        return new Instrument()
            .symbol(UPDATED_SYMBOL)
            .name(UPDATED_NAME)
            .assetClass(UPDATED_ASSET_CLASS)
            .exchangeCode(UPDATED_EXCHANGE_CODE)
            .tickSize(UPDATED_TICK_SIZE)
            .lotSize(UPDATED_LOT_SIZE)
            .currency(UPDATED_CURRENCY)
            .status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        instrument = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedInstrument != null) {
            instrumentRepository.delete(insertedInstrument);
            insertedInstrument = null;
        }
    }

    @Test
    @Transactional
    void createInstrument() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Instrument
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);
        var returnedInstrumentDTO = om.readValue(
            restInstrumentMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(instrumentDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            InstrumentDTO.class
        );

        // Validate the Instrument in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedInstrument = instrumentMapper.toEntity(returnedInstrumentDTO);
        assertInstrumentUpdatableFieldsEquals(returnedInstrument, getPersistedInstrument(returnedInstrument));

        insertedInstrument = returnedInstrument;
    }

    @Test
    @Transactional
    void createInstrumentWithExistingId() throws Exception {
        // Create the Instrument with an existing ID
        instrument.setId(1L);
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restInstrumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(instrumentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Instrument in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkSymbolIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        instrument.setSymbol(null);

        // Create the Instrument, which fails.
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        restInstrumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(instrumentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAssetClassIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        instrument.setAssetClass(null);

        // Create the Instrument, which fails.
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        restInstrumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(instrumentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkExchangeCodeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        instrument.setExchangeCode(null);

        // Create the Instrument, which fails.
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        restInstrumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(instrumentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTickSizeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        instrument.setTickSize(null);

        // Create the Instrument, which fails.
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        restInstrumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(instrumentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLotSizeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        instrument.setLotSize(null);

        // Create the Instrument, which fails.
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        restInstrumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(instrumentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        instrument.setCurrency(null);

        // Create the Instrument, which fails.
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        restInstrumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(instrumentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        instrument.setStatus(null);

        // Create the Instrument, which fails.
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        restInstrumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(instrumentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllInstruments() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList
        restInstrumentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(instrument.getId().intValue())))
            .andExpect(jsonPath("$.[*].symbol").value(hasItem(DEFAULT_SYMBOL)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].assetClass").value(hasItem(DEFAULT_ASSET_CLASS.toString())))
            .andExpect(jsonPath("$.[*].exchangeCode").value(hasItem(DEFAULT_EXCHANGE_CODE)))
            .andExpect(jsonPath("$.[*].tickSize").value(hasItem(sameNumber(DEFAULT_TICK_SIZE))))
            .andExpect(jsonPath("$.[*].lotSize").value(hasItem(DEFAULT_LOT_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllInstrumentsWithEagerRelationshipsIsEnabled() throws Exception {
        when(instrumentServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restInstrumentMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(instrumentServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllInstrumentsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(instrumentServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restInstrumentMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(instrumentRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getInstrument() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get the instrument
        restInstrumentMockMvc
            .perform(get(ENTITY_API_URL_ID, instrument.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(instrument.getId().intValue()))
            .andExpect(jsonPath("$.symbol").value(DEFAULT_SYMBOL))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.assetClass").value(DEFAULT_ASSET_CLASS.toString()))
            .andExpect(jsonPath("$.exchangeCode").value(DEFAULT_EXCHANGE_CODE))
            .andExpect(jsonPath("$.tickSize").value(sameNumber(DEFAULT_TICK_SIZE)))
            .andExpect(jsonPath("$.lotSize").value(DEFAULT_LOT_SIZE.intValue()))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS));
    }

    @Test
    @Transactional
    void getInstrumentsByIdFiltering() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        Long id = instrument.getId();

        defaultInstrumentFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultInstrumentFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultInstrumentFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllInstrumentsBySymbolIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where symbol equals to
        defaultInstrumentFiltering("symbol.equals=" + DEFAULT_SYMBOL, "symbol.equals=" + UPDATED_SYMBOL);
    }

    @Test
    @Transactional
    void getAllInstrumentsBySymbolIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where symbol in
        defaultInstrumentFiltering("symbol.in=" + DEFAULT_SYMBOL + "," + UPDATED_SYMBOL, "symbol.in=" + UPDATED_SYMBOL);
    }

    @Test
    @Transactional
    void getAllInstrumentsBySymbolIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where symbol is not null
        defaultInstrumentFiltering("symbol.specified=true", "symbol.specified=false");
    }

    @Test
    @Transactional
    void getAllInstrumentsBySymbolContainsSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where symbol contains
        defaultInstrumentFiltering("symbol.contains=" + DEFAULT_SYMBOL, "symbol.contains=" + UPDATED_SYMBOL);
    }

    @Test
    @Transactional
    void getAllInstrumentsBySymbolNotContainsSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where symbol does not contain
        defaultInstrumentFiltering("symbol.doesNotContain=" + UPDATED_SYMBOL, "symbol.doesNotContain=" + DEFAULT_SYMBOL);
    }

    @Test
    @Transactional
    void getAllInstrumentsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where name equals to
        defaultInstrumentFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllInstrumentsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where name in
        defaultInstrumentFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllInstrumentsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where name is not null
        defaultInstrumentFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllInstrumentsByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where name contains
        defaultInstrumentFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllInstrumentsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where name does not contain
        defaultInstrumentFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllInstrumentsByAssetClassIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where assetClass equals to
        defaultInstrumentFiltering("assetClass.equals=" + DEFAULT_ASSET_CLASS, "assetClass.equals=" + UPDATED_ASSET_CLASS);
    }

    @Test
    @Transactional
    void getAllInstrumentsByAssetClassIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where assetClass in
        defaultInstrumentFiltering(
            "assetClass.in=" + DEFAULT_ASSET_CLASS + "," + UPDATED_ASSET_CLASS,
            "assetClass.in=" + UPDATED_ASSET_CLASS
        );
    }

    @Test
    @Transactional
    void getAllInstrumentsByAssetClassIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where assetClass is not null
        defaultInstrumentFiltering("assetClass.specified=true", "assetClass.specified=false");
    }

    @Test
    @Transactional
    void getAllInstrumentsByExchangeCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where exchangeCode equals to
        defaultInstrumentFiltering("exchangeCode.equals=" + DEFAULT_EXCHANGE_CODE, "exchangeCode.equals=" + UPDATED_EXCHANGE_CODE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByExchangeCodeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where exchangeCode in
        defaultInstrumentFiltering(
            "exchangeCode.in=" + DEFAULT_EXCHANGE_CODE + "," + UPDATED_EXCHANGE_CODE,
            "exchangeCode.in=" + UPDATED_EXCHANGE_CODE
        );
    }

    @Test
    @Transactional
    void getAllInstrumentsByExchangeCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where exchangeCode is not null
        defaultInstrumentFiltering("exchangeCode.specified=true", "exchangeCode.specified=false");
    }

    @Test
    @Transactional
    void getAllInstrumentsByExchangeCodeContainsSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where exchangeCode contains
        defaultInstrumentFiltering("exchangeCode.contains=" + DEFAULT_EXCHANGE_CODE, "exchangeCode.contains=" + UPDATED_EXCHANGE_CODE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByExchangeCodeNotContainsSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where exchangeCode does not contain
        defaultInstrumentFiltering(
            "exchangeCode.doesNotContain=" + UPDATED_EXCHANGE_CODE,
            "exchangeCode.doesNotContain=" + DEFAULT_EXCHANGE_CODE
        );
    }

    @Test
    @Transactional
    void getAllInstrumentsByTickSizeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where tickSize equals to
        defaultInstrumentFiltering("tickSize.equals=" + DEFAULT_TICK_SIZE, "tickSize.equals=" + UPDATED_TICK_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByTickSizeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where tickSize in
        defaultInstrumentFiltering("tickSize.in=" + DEFAULT_TICK_SIZE + "," + UPDATED_TICK_SIZE, "tickSize.in=" + UPDATED_TICK_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByTickSizeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where tickSize is not null
        defaultInstrumentFiltering("tickSize.specified=true", "tickSize.specified=false");
    }

    @Test
    @Transactional
    void getAllInstrumentsByTickSizeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where tickSize is greater than or equal to
        defaultInstrumentFiltering("tickSize.greaterThanOrEqual=" + DEFAULT_TICK_SIZE, "tickSize.greaterThanOrEqual=" + UPDATED_TICK_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByTickSizeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where tickSize is less than or equal to
        defaultInstrumentFiltering("tickSize.lessThanOrEqual=" + DEFAULT_TICK_SIZE, "tickSize.lessThanOrEqual=" + SMALLER_TICK_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByTickSizeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where tickSize is less than
        defaultInstrumentFiltering("tickSize.lessThan=" + UPDATED_TICK_SIZE, "tickSize.lessThan=" + DEFAULT_TICK_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByTickSizeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where tickSize is greater than
        defaultInstrumentFiltering("tickSize.greaterThan=" + SMALLER_TICK_SIZE, "tickSize.greaterThan=" + DEFAULT_TICK_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByLotSizeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where lotSize equals to
        defaultInstrumentFiltering("lotSize.equals=" + DEFAULT_LOT_SIZE, "lotSize.equals=" + UPDATED_LOT_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByLotSizeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where lotSize in
        defaultInstrumentFiltering("lotSize.in=" + DEFAULT_LOT_SIZE + "," + UPDATED_LOT_SIZE, "lotSize.in=" + UPDATED_LOT_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByLotSizeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where lotSize is not null
        defaultInstrumentFiltering("lotSize.specified=true", "lotSize.specified=false");
    }

    @Test
    @Transactional
    void getAllInstrumentsByLotSizeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where lotSize is greater than or equal to
        defaultInstrumentFiltering("lotSize.greaterThanOrEqual=" + DEFAULT_LOT_SIZE, "lotSize.greaterThanOrEqual=" + UPDATED_LOT_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByLotSizeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where lotSize is less than or equal to
        defaultInstrumentFiltering("lotSize.lessThanOrEqual=" + DEFAULT_LOT_SIZE, "lotSize.lessThanOrEqual=" + SMALLER_LOT_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByLotSizeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where lotSize is less than
        defaultInstrumentFiltering("lotSize.lessThan=" + UPDATED_LOT_SIZE, "lotSize.lessThan=" + DEFAULT_LOT_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByLotSizeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where lotSize is greater than
        defaultInstrumentFiltering("lotSize.greaterThan=" + SMALLER_LOT_SIZE, "lotSize.greaterThan=" + DEFAULT_LOT_SIZE);
    }

    @Test
    @Transactional
    void getAllInstrumentsByCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where currency equals to
        defaultInstrumentFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllInstrumentsByCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where currency in
        defaultInstrumentFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllInstrumentsByCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where currency is not null
        defaultInstrumentFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    @Transactional
    void getAllInstrumentsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where status equals to
        defaultInstrumentFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllInstrumentsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where status in
        defaultInstrumentFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllInstrumentsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where status is not null
        defaultInstrumentFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllInstrumentsByStatusContainsSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where status contains
        defaultInstrumentFiltering("status.contains=" + DEFAULT_STATUS, "status.contains=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllInstrumentsByStatusNotContainsSomething() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        // Get all the instrumentList where status does not contain
        defaultInstrumentFiltering("status.doesNotContain=" + UPDATED_STATUS, "status.doesNotContain=" + DEFAULT_STATUS);
    }

    @Test
    @Transactional
    void getAllInstrumentsByExchangeIsEqualToSomething() throws Exception {
        Exchange exchange;
        if (TestUtil.findAll(em, Exchange.class).isEmpty()) {
            instrumentRepository.saveAndFlush(instrument);
            exchange = ExchangeResourceIT.createEntity();
        } else {
            exchange = TestUtil.findAll(em, Exchange.class).get(0);
        }
        em.persist(exchange);
        em.flush();
        instrument.setExchange(exchange);
        instrumentRepository.saveAndFlush(instrument);
        Long exchangeId = exchange.getId();
        // Get all the instrumentList where exchange equals to exchangeId
        defaultInstrumentShouldBeFound("exchangeId.equals=" + exchangeId);

        // Get all the instrumentList where exchange equals to (exchangeId + 1)
        defaultInstrumentShouldNotBeFound("exchangeId.equals=" + (exchangeId + 1));
    }

    private void defaultInstrumentFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultInstrumentShouldBeFound(shouldBeFound);
        defaultInstrumentShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultInstrumentShouldBeFound(String filter) throws Exception {
        restInstrumentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(instrument.getId().intValue())))
            .andExpect(jsonPath("$.[*].symbol").value(hasItem(DEFAULT_SYMBOL)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].assetClass").value(hasItem(DEFAULT_ASSET_CLASS.toString())))
            .andExpect(jsonPath("$.[*].exchangeCode").value(hasItem(DEFAULT_EXCHANGE_CODE)))
            .andExpect(jsonPath("$.[*].tickSize").value(hasItem(sameNumber(DEFAULT_TICK_SIZE))))
            .andExpect(jsonPath("$.[*].lotSize").value(hasItem(DEFAULT_LOT_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));

        // Check, that the count call also returns 1
        restInstrumentMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultInstrumentShouldNotBeFound(String filter) throws Exception {
        restInstrumentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restInstrumentMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingInstrument() throws Exception {
        // Get the instrument
        restInstrumentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingInstrument() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the instrument
        Instrument updatedInstrument = instrumentRepository.findById(instrument.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedInstrument are not directly saved in db
        em.detach(updatedInstrument);
        updatedInstrument
            .symbol(UPDATED_SYMBOL)
            .name(UPDATED_NAME)
            .assetClass(UPDATED_ASSET_CLASS)
            .exchangeCode(UPDATED_EXCHANGE_CODE)
            .tickSize(UPDATED_TICK_SIZE)
            .lotSize(UPDATED_LOT_SIZE)
            .currency(UPDATED_CURRENCY)
            .status(UPDATED_STATUS);
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(updatedInstrument);

        restInstrumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, instrumentDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(instrumentDTO))
            )
            .andExpect(status().isOk());

        // Validate the Instrument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedInstrumentToMatchAllProperties(updatedInstrument);
    }

    @Test
    @Transactional
    void putNonExistingInstrument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        instrument.setId(longCount.incrementAndGet());

        // Create the Instrument
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInstrumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, instrumentDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(instrumentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instrument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchInstrument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        instrument.setId(longCount.incrementAndGet());

        // Create the Instrument
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstrumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(instrumentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instrument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamInstrument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        instrument.setId(longCount.incrementAndGet());

        // Create the Instrument
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstrumentMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(instrumentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Instrument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateInstrumentWithPatch() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the instrument using partial update
        Instrument partialUpdatedInstrument = new Instrument();
        partialUpdatedInstrument.setId(instrument.getId());

        partialUpdatedInstrument.name(UPDATED_NAME).assetClass(UPDATED_ASSET_CLASS).tickSize(UPDATED_TICK_SIZE).lotSize(UPDATED_LOT_SIZE);

        restInstrumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInstrument.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedInstrument))
            )
            .andExpect(status().isOk());

        // Validate the Instrument in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertInstrumentUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedInstrument, instrument),
            getPersistedInstrument(instrument)
        );
    }

    @Test
    @Transactional
    void fullUpdateInstrumentWithPatch() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the instrument using partial update
        Instrument partialUpdatedInstrument = new Instrument();
        partialUpdatedInstrument.setId(instrument.getId());

        partialUpdatedInstrument
            .symbol(UPDATED_SYMBOL)
            .name(UPDATED_NAME)
            .assetClass(UPDATED_ASSET_CLASS)
            .exchangeCode(UPDATED_EXCHANGE_CODE)
            .tickSize(UPDATED_TICK_SIZE)
            .lotSize(UPDATED_LOT_SIZE)
            .currency(UPDATED_CURRENCY)
            .status(UPDATED_STATUS);

        restInstrumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInstrument.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedInstrument))
            )
            .andExpect(status().isOk());

        // Validate the Instrument in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertInstrumentUpdatableFieldsEquals(partialUpdatedInstrument, getPersistedInstrument(partialUpdatedInstrument));
    }

    @Test
    @Transactional
    void patchNonExistingInstrument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        instrument.setId(longCount.incrementAndGet());

        // Create the Instrument
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInstrumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, instrumentDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(instrumentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instrument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchInstrument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        instrument.setId(longCount.incrementAndGet());

        // Create the Instrument
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstrumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(instrumentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Instrument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamInstrument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        instrument.setId(longCount.incrementAndGet());

        // Create the Instrument
        InstrumentDTO instrumentDTO = instrumentMapper.toDto(instrument);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstrumentMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(instrumentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Instrument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteInstrument() throws Exception {
        // Initialize the database
        insertedInstrument = instrumentRepository.saveAndFlush(instrument);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the instrument
        restInstrumentMockMvc
            .perform(delete(ENTITY_API_URL_ID, instrument.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return instrumentRepository.count();
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

    protected Instrument getPersistedInstrument(Instrument instrument) {
        return instrumentRepository.findById(instrument.getId()).orElseThrow();
    }

    protected void assertPersistedInstrumentToMatchAllProperties(Instrument expectedInstrument) {
        assertInstrumentAllPropertiesEquals(expectedInstrument, getPersistedInstrument(expectedInstrument));
    }

    protected void assertPersistedInstrumentToMatchUpdatableProperties(Instrument expectedInstrument) {
        assertInstrumentAllUpdatablePropertiesEquals(expectedInstrument, getPersistedInstrument(expectedInstrument));
    }
}
