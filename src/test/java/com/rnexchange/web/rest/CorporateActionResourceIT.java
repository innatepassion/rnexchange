package com.rnexchange.web.rest;

import static com.rnexchange.domain.CorporateActionAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static com.rnexchange.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.CorporateAction;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.enumeration.CorporateActionType;
import com.rnexchange.repository.CorporateActionRepository;
import com.rnexchange.service.CorporateActionService;
import com.rnexchange.service.dto.CorporateActionDTO;
import com.rnexchange.service.mapper.CorporateActionMapper;
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
 * Integration tests for the {@link CorporateActionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CorporateActionResourceIT {

    private static final CorporateActionType DEFAULT_TYPE = CorporateActionType.SPLIT;
    private static final CorporateActionType UPDATED_TYPE = CorporateActionType.DIVIDEND;

    private static final String DEFAULT_INSTRUMENT_SYMBOL = "AAAAAAAAAA";
    private static final String UPDATED_INSTRUMENT_SYMBOL = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_EX_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EX_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_EX_DATE = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_PAY_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_PAY_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_PAY_DATE = LocalDate.ofEpochDay(-1L);

    private static final BigDecimal DEFAULT_RATIO = new BigDecimal(1);
    private static final BigDecimal UPDATED_RATIO = new BigDecimal(2);
    private static final BigDecimal SMALLER_RATIO = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_CASH_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_CASH_AMOUNT = new BigDecimal(2);
    private static final BigDecimal SMALLER_CASH_AMOUNT = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/corporate-actions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CorporateActionRepository corporateActionRepository;

    @Mock
    private CorporateActionRepository corporateActionRepositoryMock;

    @Autowired
    private CorporateActionMapper corporateActionMapper;

    @Mock
    private CorporateActionService corporateActionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCorporateActionMockMvc;

    private CorporateAction corporateAction;

    private CorporateAction insertedCorporateAction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CorporateAction createEntity() {
        return new CorporateAction()
            .type(DEFAULT_TYPE)
            .instrumentSymbol(DEFAULT_INSTRUMENT_SYMBOL)
            .exDate(DEFAULT_EX_DATE)
            .payDate(DEFAULT_PAY_DATE)
            .ratio(DEFAULT_RATIO)
            .cashAmount(DEFAULT_CASH_AMOUNT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CorporateAction createUpdatedEntity() {
        return new CorporateAction()
            .type(UPDATED_TYPE)
            .instrumentSymbol(UPDATED_INSTRUMENT_SYMBOL)
            .exDate(UPDATED_EX_DATE)
            .payDate(UPDATED_PAY_DATE)
            .ratio(UPDATED_RATIO)
            .cashAmount(UPDATED_CASH_AMOUNT);
    }

    @BeforeEach
    void initTest() {
        corporateAction = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCorporateAction != null) {
            corporateActionRepository.delete(insertedCorporateAction);
            insertedCorporateAction = null;
        }
    }

    @Test
    @Transactional
    void createCorporateAction() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CorporateAction
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);
        var returnedCorporateActionDTO = om.readValue(
            restCorporateActionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(corporateActionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CorporateActionDTO.class
        );

        // Validate the CorporateAction in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCorporateAction = corporateActionMapper.toEntity(returnedCorporateActionDTO);
        assertCorporateActionUpdatableFieldsEquals(returnedCorporateAction, getPersistedCorporateAction(returnedCorporateAction));

        insertedCorporateAction = returnedCorporateAction;
    }

    @Test
    @Transactional
    void createCorporateActionWithExistingId() throws Exception {
        // Create the CorporateAction with an existing ID
        corporateAction.setId(1L);
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCorporateActionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(corporateActionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CorporateAction in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        corporateAction.setType(null);

        // Create the CorporateAction, which fails.
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);

        restCorporateActionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(corporateActionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkInstrumentSymbolIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        corporateAction.setInstrumentSymbol(null);

        // Create the CorporateAction, which fails.
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);

        restCorporateActionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(corporateActionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkExDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        corporateAction.setExDate(null);

        // Create the CorporateAction, which fails.
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);

        restCorporateActionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(corporateActionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCorporateActions() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList
        restCorporateActionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(corporateAction.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].instrumentSymbol").value(hasItem(DEFAULT_INSTRUMENT_SYMBOL)))
            .andExpect(jsonPath("$.[*].exDate").value(hasItem(DEFAULT_EX_DATE.toString())))
            .andExpect(jsonPath("$.[*].payDate").value(hasItem(DEFAULT_PAY_DATE.toString())))
            .andExpect(jsonPath("$.[*].ratio").value(hasItem(sameNumber(DEFAULT_RATIO))))
            .andExpect(jsonPath("$.[*].cashAmount").value(hasItem(sameNumber(DEFAULT_CASH_AMOUNT))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCorporateActionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(corporateActionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCorporateActionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(corporateActionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCorporateActionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(corporateActionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCorporateActionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(corporateActionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getCorporateAction() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get the corporateAction
        restCorporateActionMockMvc
            .perform(get(ENTITY_API_URL_ID, corporateAction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(corporateAction.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.instrumentSymbol").value(DEFAULT_INSTRUMENT_SYMBOL))
            .andExpect(jsonPath("$.exDate").value(DEFAULT_EX_DATE.toString()))
            .andExpect(jsonPath("$.payDate").value(DEFAULT_PAY_DATE.toString()))
            .andExpect(jsonPath("$.ratio").value(sameNumber(DEFAULT_RATIO)))
            .andExpect(jsonPath("$.cashAmount").value(sameNumber(DEFAULT_CASH_AMOUNT)));
    }

    @Test
    @Transactional
    void getCorporateActionsByIdFiltering() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        Long id = corporateAction.getId();

        defaultCorporateActionFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCorporateActionFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCorporateActionFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where type equals to
        defaultCorporateActionFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where type in
        defaultCorporateActionFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where type is not null
        defaultCorporateActionFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllCorporateActionsByInstrumentSymbolIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where instrumentSymbol equals to
        defaultCorporateActionFiltering(
            "instrumentSymbol.equals=" + DEFAULT_INSTRUMENT_SYMBOL,
            "instrumentSymbol.equals=" + UPDATED_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllCorporateActionsByInstrumentSymbolIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where instrumentSymbol in
        defaultCorporateActionFiltering(
            "instrumentSymbol.in=" + DEFAULT_INSTRUMENT_SYMBOL + "," + UPDATED_INSTRUMENT_SYMBOL,
            "instrumentSymbol.in=" + UPDATED_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllCorporateActionsByInstrumentSymbolIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where instrumentSymbol is not null
        defaultCorporateActionFiltering("instrumentSymbol.specified=true", "instrumentSymbol.specified=false");
    }

    @Test
    @Transactional
    void getAllCorporateActionsByInstrumentSymbolContainsSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where instrumentSymbol contains
        defaultCorporateActionFiltering(
            "instrumentSymbol.contains=" + DEFAULT_INSTRUMENT_SYMBOL,
            "instrumentSymbol.contains=" + UPDATED_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllCorporateActionsByInstrumentSymbolNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where instrumentSymbol does not contain
        defaultCorporateActionFiltering(
            "instrumentSymbol.doesNotContain=" + UPDATED_INSTRUMENT_SYMBOL,
            "instrumentSymbol.doesNotContain=" + DEFAULT_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllCorporateActionsByExDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where exDate equals to
        defaultCorporateActionFiltering("exDate.equals=" + DEFAULT_EX_DATE, "exDate.equals=" + UPDATED_EX_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByExDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where exDate in
        defaultCorporateActionFiltering("exDate.in=" + DEFAULT_EX_DATE + "," + UPDATED_EX_DATE, "exDate.in=" + UPDATED_EX_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByExDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where exDate is not null
        defaultCorporateActionFiltering("exDate.specified=true", "exDate.specified=false");
    }

    @Test
    @Transactional
    void getAllCorporateActionsByExDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where exDate is greater than or equal to
        defaultCorporateActionFiltering("exDate.greaterThanOrEqual=" + DEFAULT_EX_DATE, "exDate.greaterThanOrEqual=" + UPDATED_EX_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByExDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where exDate is less than or equal to
        defaultCorporateActionFiltering("exDate.lessThanOrEqual=" + DEFAULT_EX_DATE, "exDate.lessThanOrEqual=" + SMALLER_EX_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByExDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where exDate is less than
        defaultCorporateActionFiltering("exDate.lessThan=" + UPDATED_EX_DATE, "exDate.lessThan=" + DEFAULT_EX_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByExDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where exDate is greater than
        defaultCorporateActionFiltering("exDate.greaterThan=" + SMALLER_EX_DATE, "exDate.greaterThan=" + DEFAULT_EX_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByPayDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where payDate equals to
        defaultCorporateActionFiltering("payDate.equals=" + DEFAULT_PAY_DATE, "payDate.equals=" + UPDATED_PAY_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByPayDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where payDate in
        defaultCorporateActionFiltering("payDate.in=" + DEFAULT_PAY_DATE + "," + UPDATED_PAY_DATE, "payDate.in=" + UPDATED_PAY_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByPayDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where payDate is not null
        defaultCorporateActionFiltering("payDate.specified=true", "payDate.specified=false");
    }

    @Test
    @Transactional
    void getAllCorporateActionsByPayDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where payDate is greater than or equal to
        defaultCorporateActionFiltering("payDate.greaterThanOrEqual=" + DEFAULT_PAY_DATE, "payDate.greaterThanOrEqual=" + UPDATED_PAY_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByPayDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where payDate is less than or equal to
        defaultCorporateActionFiltering("payDate.lessThanOrEqual=" + DEFAULT_PAY_DATE, "payDate.lessThanOrEqual=" + SMALLER_PAY_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByPayDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where payDate is less than
        defaultCorporateActionFiltering("payDate.lessThan=" + UPDATED_PAY_DATE, "payDate.lessThan=" + DEFAULT_PAY_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByPayDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where payDate is greater than
        defaultCorporateActionFiltering("payDate.greaterThan=" + SMALLER_PAY_DATE, "payDate.greaterThan=" + DEFAULT_PAY_DATE);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where ratio equals to
        defaultCorporateActionFiltering("ratio.equals=" + DEFAULT_RATIO, "ratio.equals=" + UPDATED_RATIO);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByRatioIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where ratio in
        defaultCorporateActionFiltering("ratio.in=" + DEFAULT_RATIO + "," + UPDATED_RATIO, "ratio.in=" + UPDATED_RATIO);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where ratio is not null
        defaultCorporateActionFiltering("ratio.specified=true", "ratio.specified=false");
    }

    @Test
    @Transactional
    void getAllCorporateActionsByRatioIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where ratio is greater than or equal to
        defaultCorporateActionFiltering("ratio.greaterThanOrEqual=" + DEFAULT_RATIO, "ratio.greaterThanOrEqual=" + UPDATED_RATIO);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByRatioIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where ratio is less than or equal to
        defaultCorporateActionFiltering("ratio.lessThanOrEqual=" + DEFAULT_RATIO, "ratio.lessThanOrEqual=" + SMALLER_RATIO);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByRatioIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where ratio is less than
        defaultCorporateActionFiltering("ratio.lessThan=" + UPDATED_RATIO, "ratio.lessThan=" + DEFAULT_RATIO);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByRatioIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where ratio is greater than
        defaultCorporateActionFiltering("ratio.greaterThan=" + SMALLER_RATIO, "ratio.greaterThan=" + DEFAULT_RATIO);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByCashAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where cashAmount equals to
        defaultCorporateActionFiltering("cashAmount.equals=" + DEFAULT_CASH_AMOUNT, "cashAmount.equals=" + UPDATED_CASH_AMOUNT);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByCashAmountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where cashAmount in
        defaultCorporateActionFiltering(
            "cashAmount.in=" + DEFAULT_CASH_AMOUNT + "," + UPDATED_CASH_AMOUNT,
            "cashAmount.in=" + UPDATED_CASH_AMOUNT
        );
    }

    @Test
    @Transactional
    void getAllCorporateActionsByCashAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where cashAmount is not null
        defaultCorporateActionFiltering("cashAmount.specified=true", "cashAmount.specified=false");
    }

    @Test
    @Transactional
    void getAllCorporateActionsByCashAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where cashAmount is greater than or equal to
        defaultCorporateActionFiltering(
            "cashAmount.greaterThanOrEqual=" + DEFAULT_CASH_AMOUNT,
            "cashAmount.greaterThanOrEqual=" + UPDATED_CASH_AMOUNT
        );
    }

    @Test
    @Transactional
    void getAllCorporateActionsByCashAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where cashAmount is less than or equal to
        defaultCorporateActionFiltering(
            "cashAmount.lessThanOrEqual=" + DEFAULT_CASH_AMOUNT,
            "cashAmount.lessThanOrEqual=" + SMALLER_CASH_AMOUNT
        );
    }

    @Test
    @Transactional
    void getAllCorporateActionsByCashAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where cashAmount is less than
        defaultCorporateActionFiltering("cashAmount.lessThan=" + UPDATED_CASH_AMOUNT, "cashAmount.lessThan=" + DEFAULT_CASH_AMOUNT);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByCashAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        // Get all the corporateActionList where cashAmount is greater than
        defaultCorporateActionFiltering("cashAmount.greaterThan=" + SMALLER_CASH_AMOUNT, "cashAmount.greaterThan=" + DEFAULT_CASH_AMOUNT);
    }

    @Test
    @Transactional
    void getAllCorporateActionsByInstrumentIsEqualToSomething() throws Exception {
        Instrument instrument;
        if (TestUtil.findAll(em, Instrument.class).isEmpty()) {
            corporateActionRepository.saveAndFlush(corporateAction);
            instrument = InstrumentResourceIT.createEntity();
        } else {
            instrument = TestUtil.findAll(em, Instrument.class).get(0);
        }
        em.persist(instrument);
        em.flush();
        corporateAction.setInstrument(instrument);
        corporateActionRepository.saveAndFlush(corporateAction);
        Long instrumentId = instrument.getId();
        // Get all the corporateActionList where instrument equals to instrumentId
        defaultCorporateActionShouldBeFound("instrumentId.equals=" + instrumentId);

        // Get all the corporateActionList where instrument equals to (instrumentId + 1)
        defaultCorporateActionShouldNotBeFound("instrumentId.equals=" + (instrumentId + 1));
    }

    private void defaultCorporateActionFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCorporateActionShouldBeFound(shouldBeFound);
        defaultCorporateActionShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCorporateActionShouldBeFound(String filter) throws Exception {
        restCorporateActionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(corporateAction.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].instrumentSymbol").value(hasItem(DEFAULT_INSTRUMENT_SYMBOL)))
            .andExpect(jsonPath("$.[*].exDate").value(hasItem(DEFAULT_EX_DATE.toString())))
            .andExpect(jsonPath("$.[*].payDate").value(hasItem(DEFAULT_PAY_DATE.toString())))
            .andExpect(jsonPath("$.[*].ratio").value(hasItem(sameNumber(DEFAULT_RATIO))))
            .andExpect(jsonPath("$.[*].cashAmount").value(hasItem(sameNumber(DEFAULT_CASH_AMOUNT))));

        // Check, that the count call also returns 1
        restCorporateActionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCorporateActionShouldNotBeFound(String filter) throws Exception {
        restCorporateActionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCorporateActionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCorporateAction() throws Exception {
        // Get the corporateAction
        restCorporateActionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCorporateAction() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the corporateAction
        CorporateAction updatedCorporateAction = corporateActionRepository.findById(corporateAction.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCorporateAction are not directly saved in db
        em.detach(updatedCorporateAction);
        updatedCorporateAction
            .type(UPDATED_TYPE)
            .instrumentSymbol(UPDATED_INSTRUMENT_SYMBOL)
            .exDate(UPDATED_EX_DATE)
            .payDate(UPDATED_PAY_DATE)
            .ratio(UPDATED_RATIO)
            .cashAmount(UPDATED_CASH_AMOUNT);
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(updatedCorporateAction);

        restCorporateActionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, corporateActionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(corporateActionDTO))
            )
            .andExpect(status().isOk());

        // Validate the CorporateAction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCorporateActionToMatchAllProperties(updatedCorporateAction);
    }

    @Test
    @Transactional
    void putNonExistingCorporateAction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        corporateAction.setId(longCount.incrementAndGet());

        // Create the CorporateAction
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCorporateActionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, corporateActionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(corporateActionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CorporateAction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCorporateAction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        corporateAction.setId(longCount.incrementAndGet());

        // Create the CorporateAction
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCorporateActionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(corporateActionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CorporateAction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCorporateAction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        corporateAction.setId(longCount.incrementAndGet());

        // Create the CorporateAction
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCorporateActionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(corporateActionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CorporateAction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCorporateActionWithPatch() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the corporateAction using partial update
        CorporateAction partialUpdatedCorporateAction = new CorporateAction();
        partialUpdatedCorporateAction.setId(corporateAction.getId());

        partialUpdatedCorporateAction.exDate(UPDATED_EX_DATE).payDate(UPDATED_PAY_DATE);

        restCorporateActionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCorporateAction.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCorporateAction))
            )
            .andExpect(status().isOk());

        // Validate the CorporateAction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCorporateActionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCorporateAction, corporateAction),
            getPersistedCorporateAction(corporateAction)
        );
    }

    @Test
    @Transactional
    void fullUpdateCorporateActionWithPatch() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the corporateAction using partial update
        CorporateAction partialUpdatedCorporateAction = new CorporateAction();
        partialUpdatedCorporateAction.setId(corporateAction.getId());

        partialUpdatedCorporateAction
            .type(UPDATED_TYPE)
            .instrumentSymbol(UPDATED_INSTRUMENT_SYMBOL)
            .exDate(UPDATED_EX_DATE)
            .payDate(UPDATED_PAY_DATE)
            .ratio(UPDATED_RATIO)
            .cashAmount(UPDATED_CASH_AMOUNT);

        restCorporateActionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCorporateAction.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCorporateAction))
            )
            .andExpect(status().isOk());

        // Validate the CorporateAction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCorporateActionUpdatableFieldsEquals(
            partialUpdatedCorporateAction,
            getPersistedCorporateAction(partialUpdatedCorporateAction)
        );
    }

    @Test
    @Transactional
    void patchNonExistingCorporateAction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        corporateAction.setId(longCount.incrementAndGet());

        // Create the CorporateAction
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCorporateActionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, corporateActionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(corporateActionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CorporateAction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCorporateAction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        corporateAction.setId(longCount.incrementAndGet());

        // Create the CorporateAction
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCorporateActionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(corporateActionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CorporateAction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCorporateAction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        corporateAction.setId(longCount.incrementAndGet());

        // Create the CorporateAction
        CorporateActionDTO corporateActionDTO = corporateActionMapper.toDto(corporateAction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCorporateActionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(corporateActionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CorporateAction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCorporateAction() throws Exception {
        // Initialize the database
        insertedCorporateAction = corporateActionRepository.saveAndFlush(corporateAction);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the corporateAction
        restCorporateActionMockMvc
            .perform(delete(ENTITY_API_URL_ID, corporateAction.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return corporateActionRepository.count();
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

    protected CorporateAction getPersistedCorporateAction(CorporateAction corporateAction) {
        return corporateActionRepository.findById(corporateAction.getId()).orElseThrow();
    }

    protected void assertPersistedCorporateActionToMatchAllProperties(CorporateAction expectedCorporateAction) {
        assertCorporateActionAllPropertiesEquals(expectedCorporateAction, getPersistedCorporateAction(expectedCorporateAction));
    }

    protected void assertPersistedCorporateActionToMatchUpdatableProperties(CorporateAction expectedCorporateAction) {
        assertCorporateActionAllUpdatablePropertiesEquals(expectedCorporateAction, getPersistedCorporateAction(expectedCorporateAction));
    }
}
