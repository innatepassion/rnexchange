package com.rnexchange.web.rest;

import static com.rnexchange.domain.PositionAsserts.*;
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
import com.rnexchange.domain.Position;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.PositionRepository;
import com.rnexchange.service.PositionService;
import com.rnexchange.service.dto.PositionDTO;
import com.rnexchange.service.mapper.PositionMapper;
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
 * Integration tests for the {@link PositionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PositionResourceIT {

    private static final BigDecimal DEFAULT_QTY = new BigDecimal(1);
    private static final BigDecimal UPDATED_QTY = new BigDecimal(2);
    private static final BigDecimal SMALLER_QTY = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_AVG_COST = new BigDecimal(1);
    private static final BigDecimal UPDATED_AVG_COST = new BigDecimal(2);
    private static final BigDecimal SMALLER_AVG_COST = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_LAST_PX = new BigDecimal(1);
    private static final BigDecimal UPDATED_LAST_PX = new BigDecimal(2);
    private static final BigDecimal SMALLER_LAST_PX = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_UNREALIZED_PNL = new BigDecimal(1);
    private static final BigDecimal UPDATED_UNREALIZED_PNL = new BigDecimal(2);
    private static final BigDecimal SMALLER_UNREALIZED_PNL = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_REALIZED_PNL = new BigDecimal(1);
    private static final BigDecimal UPDATED_REALIZED_PNL = new BigDecimal(2);
    private static final BigDecimal SMALLER_REALIZED_PNL = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/positions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PositionRepository positionRepository;

    @Mock
    private PositionRepository positionRepositoryMock;

    @Autowired
    private PositionMapper positionMapper;

    @Mock
    private PositionService positionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPositionMockMvc;

    private Position position;

    private Position insertedPosition;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Position createEntity() {
        return new Position()
            .qty(DEFAULT_QTY)
            .avgCost(DEFAULT_AVG_COST)
            .lastPx(DEFAULT_LAST_PX)
            .unrealizedPnl(DEFAULT_UNREALIZED_PNL)
            .realizedPnl(DEFAULT_REALIZED_PNL);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Position createUpdatedEntity() {
        return new Position()
            .qty(UPDATED_QTY)
            .avgCost(UPDATED_AVG_COST)
            .lastPx(UPDATED_LAST_PX)
            .unrealizedPnl(UPDATED_UNREALIZED_PNL)
            .realizedPnl(UPDATED_REALIZED_PNL);
    }

    @BeforeEach
    void initTest() {
        position = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPosition != null) {
            positionRepository.delete(insertedPosition);
            insertedPosition = null;
        }
    }

    @Test
    @Transactional
    void createPosition() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);
        var returnedPositionDTO = om.readValue(
            restPositionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PositionDTO.class
        );

        // Validate the Position in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPosition = positionMapper.toEntity(returnedPositionDTO);
        assertPositionUpdatableFieldsEquals(returnedPosition, getPersistedPosition(returnedPosition));

        insertedPosition = returnedPosition;
    }

    @Test
    @Transactional
    void createPositionWithExistingId() throws Exception {
        // Create the Position with an existing ID
        position.setId(1L);
        PositionDTO positionDTO = positionMapper.toDto(position);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPositionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkQtyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setQty(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        restPositionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAvgCostIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setAvgCost(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        restPositionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPositions() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList
        restPositionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(position.getId().intValue())))
            .andExpect(jsonPath("$.[*].qty").value(hasItem(sameNumber(DEFAULT_QTY))))
            .andExpect(jsonPath("$.[*].avgCost").value(hasItem(sameNumber(DEFAULT_AVG_COST))))
            .andExpect(jsonPath("$.[*].lastPx").value(hasItem(sameNumber(DEFAULT_LAST_PX))))
            .andExpect(jsonPath("$.[*].unrealizedPnl").value(hasItem(sameNumber(DEFAULT_UNREALIZED_PNL))))
            .andExpect(jsonPath("$.[*].realizedPnl").value(hasItem(sameNumber(DEFAULT_REALIZED_PNL))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPositionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(positionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPositionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(positionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPositionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(positionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPositionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(positionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPosition() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get the position
        restPositionMockMvc
            .perform(get(ENTITY_API_URL_ID, position.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(position.getId().intValue()))
            .andExpect(jsonPath("$.qty").value(sameNumber(DEFAULT_QTY)))
            .andExpect(jsonPath("$.avgCost").value(sameNumber(DEFAULT_AVG_COST)))
            .andExpect(jsonPath("$.lastPx").value(sameNumber(DEFAULT_LAST_PX)))
            .andExpect(jsonPath("$.unrealizedPnl").value(sameNumber(DEFAULT_UNREALIZED_PNL)))
            .andExpect(jsonPath("$.realizedPnl").value(sameNumber(DEFAULT_REALIZED_PNL)));
    }

    @Test
    @Transactional
    void getPositionsByIdFiltering() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        Long id = position.getId();

        defaultPositionFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultPositionFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultPositionFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllPositionsByQtyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where qty equals to
        defaultPositionFiltering("qty.equals=" + DEFAULT_QTY, "qty.equals=" + UPDATED_QTY);
    }

    @Test
    @Transactional
    void getAllPositionsByQtyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where qty in
        defaultPositionFiltering("qty.in=" + DEFAULT_QTY + "," + UPDATED_QTY, "qty.in=" + UPDATED_QTY);
    }

    @Test
    @Transactional
    void getAllPositionsByQtyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where qty is not null
        defaultPositionFiltering("qty.specified=true", "qty.specified=false");
    }

    @Test
    @Transactional
    void getAllPositionsByQtyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where qty is greater than or equal to
        defaultPositionFiltering("qty.greaterThanOrEqual=" + DEFAULT_QTY, "qty.greaterThanOrEqual=" + UPDATED_QTY);
    }

    @Test
    @Transactional
    void getAllPositionsByQtyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where qty is less than or equal to
        defaultPositionFiltering("qty.lessThanOrEqual=" + DEFAULT_QTY, "qty.lessThanOrEqual=" + SMALLER_QTY);
    }

    @Test
    @Transactional
    void getAllPositionsByQtyIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where qty is less than
        defaultPositionFiltering("qty.lessThan=" + UPDATED_QTY, "qty.lessThan=" + DEFAULT_QTY);
    }

    @Test
    @Transactional
    void getAllPositionsByQtyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where qty is greater than
        defaultPositionFiltering("qty.greaterThan=" + SMALLER_QTY, "qty.greaterThan=" + DEFAULT_QTY);
    }

    @Test
    @Transactional
    void getAllPositionsByAvgCostIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where avgCost equals to
        defaultPositionFiltering("avgCost.equals=" + DEFAULT_AVG_COST, "avgCost.equals=" + UPDATED_AVG_COST);
    }

    @Test
    @Transactional
    void getAllPositionsByAvgCostIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where avgCost in
        defaultPositionFiltering("avgCost.in=" + DEFAULT_AVG_COST + "," + UPDATED_AVG_COST, "avgCost.in=" + UPDATED_AVG_COST);
    }

    @Test
    @Transactional
    void getAllPositionsByAvgCostIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where avgCost is not null
        defaultPositionFiltering("avgCost.specified=true", "avgCost.specified=false");
    }

    @Test
    @Transactional
    void getAllPositionsByAvgCostIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where avgCost is greater than or equal to
        defaultPositionFiltering("avgCost.greaterThanOrEqual=" + DEFAULT_AVG_COST, "avgCost.greaterThanOrEqual=" + UPDATED_AVG_COST);
    }

    @Test
    @Transactional
    void getAllPositionsByAvgCostIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where avgCost is less than or equal to
        defaultPositionFiltering("avgCost.lessThanOrEqual=" + DEFAULT_AVG_COST, "avgCost.lessThanOrEqual=" + SMALLER_AVG_COST);
    }

    @Test
    @Transactional
    void getAllPositionsByAvgCostIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where avgCost is less than
        defaultPositionFiltering("avgCost.lessThan=" + UPDATED_AVG_COST, "avgCost.lessThan=" + DEFAULT_AVG_COST);
    }

    @Test
    @Transactional
    void getAllPositionsByAvgCostIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where avgCost is greater than
        defaultPositionFiltering("avgCost.greaterThan=" + SMALLER_AVG_COST, "avgCost.greaterThan=" + DEFAULT_AVG_COST);
    }

    @Test
    @Transactional
    void getAllPositionsByLastPxIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where lastPx equals to
        defaultPositionFiltering("lastPx.equals=" + DEFAULT_LAST_PX, "lastPx.equals=" + UPDATED_LAST_PX);
    }

    @Test
    @Transactional
    void getAllPositionsByLastPxIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where lastPx in
        defaultPositionFiltering("lastPx.in=" + DEFAULT_LAST_PX + "," + UPDATED_LAST_PX, "lastPx.in=" + UPDATED_LAST_PX);
    }

    @Test
    @Transactional
    void getAllPositionsByLastPxIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where lastPx is not null
        defaultPositionFiltering("lastPx.specified=true", "lastPx.specified=false");
    }

    @Test
    @Transactional
    void getAllPositionsByLastPxIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where lastPx is greater than or equal to
        defaultPositionFiltering("lastPx.greaterThanOrEqual=" + DEFAULT_LAST_PX, "lastPx.greaterThanOrEqual=" + UPDATED_LAST_PX);
    }

    @Test
    @Transactional
    void getAllPositionsByLastPxIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where lastPx is less than or equal to
        defaultPositionFiltering("lastPx.lessThanOrEqual=" + DEFAULT_LAST_PX, "lastPx.lessThanOrEqual=" + SMALLER_LAST_PX);
    }

    @Test
    @Transactional
    void getAllPositionsByLastPxIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where lastPx is less than
        defaultPositionFiltering("lastPx.lessThan=" + UPDATED_LAST_PX, "lastPx.lessThan=" + DEFAULT_LAST_PX);
    }

    @Test
    @Transactional
    void getAllPositionsByLastPxIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where lastPx is greater than
        defaultPositionFiltering("lastPx.greaterThan=" + SMALLER_LAST_PX, "lastPx.greaterThan=" + DEFAULT_LAST_PX);
    }

    @Test
    @Transactional
    void getAllPositionsByUnrealizedPnlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where unrealizedPnl equals to
        defaultPositionFiltering("unrealizedPnl.equals=" + DEFAULT_UNREALIZED_PNL, "unrealizedPnl.equals=" + UPDATED_UNREALIZED_PNL);
    }

    @Test
    @Transactional
    void getAllPositionsByUnrealizedPnlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where unrealizedPnl in
        defaultPositionFiltering(
            "unrealizedPnl.in=" + DEFAULT_UNREALIZED_PNL + "," + UPDATED_UNREALIZED_PNL,
            "unrealizedPnl.in=" + UPDATED_UNREALIZED_PNL
        );
    }

    @Test
    @Transactional
    void getAllPositionsByUnrealizedPnlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where unrealizedPnl is not null
        defaultPositionFiltering("unrealizedPnl.specified=true", "unrealizedPnl.specified=false");
    }

    @Test
    @Transactional
    void getAllPositionsByUnrealizedPnlIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where unrealizedPnl is greater than or equal to
        defaultPositionFiltering(
            "unrealizedPnl.greaterThanOrEqual=" + DEFAULT_UNREALIZED_PNL,
            "unrealizedPnl.greaterThanOrEqual=" + UPDATED_UNREALIZED_PNL
        );
    }

    @Test
    @Transactional
    void getAllPositionsByUnrealizedPnlIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where unrealizedPnl is less than or equal to
        defaultPositionFiltering(
            "unrealizedPnl.lessThanOrEqual=" + DEFAULT_UNREALIZED_PNL,
            "unrealizedPnl.lessThanOrEqual=" + SMALLER_UNREALIZED_PNL
        );
    }

    @Test
    @Transactional
    void getAllPositionsByUnrealizedPnlIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where unrealizedPnl is less than
        defaultPositionFiltering("unrealizedPnl.lessThan=" + UPDATED_UNREALIZED_PNL, "unrealizedPnl.lessThan=" + DEFAULT_UNREALIZED_PNL);
    }

    @Test
    @Transactional
    void getAllPositionsByUnrealizedPnlIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where unrealizedPnl is greater than
        defaultPositionFiltering(
            "unrealizedPnl.greaterThan=" + SMALLER_UNREALIZED_PNL,
            "unrealizedPnl.greaterThan=" + DEFAULT_UNREALIZED_PNL
        );
    }

    @Test
    @Transactional
    void getAllPositionsByRealizedPnlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where realizedPnl equals to
        defaultPositionFiltering("realizedPnl.equals=" + DEFAULT_REALIZED_PNL, "realizedPnl.equals=" + UPDATED_REALIZED_PNL);
    }

    @Test
    @Transactional
    void getAllPositionsByRealizedPnlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where realizedPnl in
        defaultPositionFiltering(
            "realizedPnl.in=" + DEFAULT_REALIZED_PNL + "," + UPDATED_REALIZED_PNL,
            "realizedPnl.in=" + UPDATED_REALIZED_PNL
        );
    }

    @Test
    @Transactional
    void getAllPositionsByRealizedPnlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where realizedPnl is not null
        defaultPositionFiltering("realizedPnl.specified=true", "realizedPnl.specified=false");
    }

    @Test
    @Transactional
    void getAllPositionsByRealizedPnlIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where realizedPnl is greater than or equal to
        defaultPositionFiltering(
            "realizedPnl.greaterThanOrEqual=" + DEFAULT_REALIZED_PNL,
            "realizedPnl.greaterThanOrEqual=" + UPDATED_REALIZED_PNL
        );
    }

    @Test
    @Transactional
    void getAllPositionsByRealizedPnlIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where realizedPnl is less than or equal to
        defaultPositionFiltering(
            "realizedPnl.lessThanOrEqual=" + DEFAULT_REALIZED_PNL,
            "realizedPnl.lessThanOrEqual=" + SMALLER_REALIZED_PNL
        );
    }

    @Test
    @Transactional
    void getAllPositionsByRealizedPnlIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where realizedPnl is less than
        defaultPositionFiltering("realizedPnl.lessThan=" + UPDATED_REALIZED_PNL, "realizedPnl.lessThan=" + DEFAULT_REALIZED_PNL);
    }

    @Test
    @Transactional
    void getAllPositionsByRealizedPnlIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where realizedPnl is greater than
        defaultPositionFiltering("realizedPnl.greaterThan=" + SMALLER_REALIZED_PNL, "realizedPnl.greaterThan=" + DEFAULT_REALIZED_PNL);
    }

    @Test
    @Transactional
    void getAllPositionsByTradingAccountIsEqualToSomething() throws Exception {
        TradingAccount tradingAccount;
        if (TestUtil.findAll(em, TradingAccount.class).isEmpty()) {
            positionRepository.saveAndFlush(position);
            tradingAccount = TradingAccountResourceIT.createEntity();
        } else {
            tradingAccount = TestUtil.findAll(em, TradingAccount.class).get(0);
        }
        em.persist(tradingAccount);
        em.flush();
        position.setTradingAccount(tradingAccount);
        positionRepository.saveAndFlush(position);
        Long tradingAccountId = tradingAccount.getId();
        // Get all the positionList where tradingAccount equals to tradingAccountId
        defaultPositionShouldBeFound("tradingAccountId.equals=" + tradingAccountId);

        // Get all the positionList where tradingAccount equals to (tradingAccountId + 1)
        defaultPositionShouldNotBeFound("tradingAccountId.equals=" + (tradingAccountId + 1));
    }

    @Test
    @Transactional
    void getAllPositionsByInstrumentIsEqualToSomething() throws Exception {
        Instrument instrument;
        if (TestUtil.findAll(em, Instrument.class).isEmpty()) {
            positionRepository.saveAndFlush(position);
            instrument = InstrumentResourceIT.createEntity();
        } else {
            instrument = TestUtil.findAll(em, Instrument.class).get(0);
        }
        em.persist(instrument);
        em.flush();
        position.setInstrument(instrument);
        positionRepository.saveAndFlush(position);
        Long instrumentId = instrument.getId();
        // Get all the positionList where instrument equals to instrumentId
        defaultPositionShouldBeFound("instrumentId.equals=" + instrumentId);

        // Get all the positionList where instrument equals to (instrumentId + 1)
        defaultPositionShouldNotBeFound("instrumentId.equals=" + (instrumentId + 1));
    }

    private void defaultPositionFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultPositionShouldBeFound(shouldBeFound);
        defaultPositionShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPositionShouldBeFound(String filter) throws Exception {
        restPositionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(position.getId().intValue())))
            .andExpect(jsonPath("$.[*].qty").value(hasItem(sameNumber(DEFAULT_QTY))))
            .andExpect(jsonPath("$.[*].avgCost").value(hasItem(sameNumber(DEFAULT_AVG_COST))))
            .andExpect(jsonPath("$.[*].lastPx").value(hasItem(sameNumber(DEFAULT_LAST_PX))))
            .andExpect(jsonPath("$.[*].unrealizedPnl").value(hasItem(sameNumber(DEFAULT_UNREALIZED_PNL))))
            .andExpect(jsonPath("$.[*].realizedPnl").value(hasItem(sameNumber(DEFAULT_REALIZED_PNL))));

        // Check, that the count call also returns 1
        restPositionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPositionShouldNotBeFound(String filter) throws Exception {
        restPositionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPositionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingPosition() throws Exception {
        // Get the position
        restPositionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPosition() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the position
        Position updatedPosition = positionRepository.findById(position.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPosition are not directly saved in db
        em.detach(updatedPosition);
        updatedPosition
            .qty(UPDATED_QTY)
            .avgCost(UPDATED_AVG_COST)
            .lastPx(UPDATED_LAST_PX)
            .unrealizedPnl(UPDATED_UNREALIZED_PNL)
            .realizedPnl(UPDATED_REALIZED_PNL);
        PositionDTO positionDTO = positionMapper.toDto(updatedPosition);

        restPositionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, positionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPositionToMatchAllProperties(updatedPosition);
    }

    @Test
    @Transactional
    void putNonExistingPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(longCount.incrementAndGet());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, positionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(longCount.incrementAndGet());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(longCount.incrementAndGet());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePositionWithPatch() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the position using partial update
        Position partialUpdatedPosition = new Position();
        partialUpdatedPosition.setId(position.getId());

        partialUpdatedPosition.avgCost(UPDATED_AVG_COST).unrealizedPnl(UPDATED_UNREALIZED_PNL);

        restPositionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPosition.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPosition))
            )
            .andExpect(status().isOk());

        // Validate the Position in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPositionUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPosition, position), getPersistedPosition(position));
    }

    @Test
    @Transactional
    void fullUpdatePositionWithPatch() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the position using partial update
        Position partialUpdatedPosition = new Position();
        partialUpdatedPosition.setId(position.getId());

        partialUpdatedPosition
            .qty(UPDATED_QTY)
            .avgCost(UPDATED_AVG_COST)
            .lastPx(UPDATED_LAST_PX)
            .unrealizedPnl(UPDATED_UNREALIZED_PNL)
            .realizedPnl(UPDATED_REALIZED_PNL);

        restPositionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPosition.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPosition))
            )
            .andExpect(status().isOk());

        // Validate the Position in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPositionUpdatableFieldsEquals(partialUpdatedPosition, getPersistedPosition(partialUpdatedPosition));
    }

    @Test
    @Transactional
    void patchNonExistingPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(longCount.incrementAndGet());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, positionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(longCount.incrementAndGet());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(longCount.incrementAndGet());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePosition() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the position
        restPositionMockMvc
            .perform(delete(ENTITY_API_URL_ID, position.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return positionRepository.count();
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

    protected Position getPersistedPosition(Position position) {
        return positionRepository.findById(position.getId()).orElseThrow();
    }

    protected void assertPersistedPositionToMatchAllProperties(Position expectedPosition) {
        assertPositionAllPropertiesEquals(expectedPosition, getPersistedPosition(expectedPosition));
    }

    protected void assertPersistedPositionToMatchUpdatableProperties(Position expectedPosition) {
        assertPositionAllUpdatablePropertiesEquals(expectedPosition, getPersistedPosition(expectedPosition));
    }
}
