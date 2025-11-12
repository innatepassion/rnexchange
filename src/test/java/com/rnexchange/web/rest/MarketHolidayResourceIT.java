package com.rnexchange.web.rest;

import static com.rnexchange.domain.MarketHolidayAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.MarketHoliday;
import com.rnexchange.repository.MarketHolidayRepository;
import com.rnexchange.service.MarketHolidayService;
import com.rnexchange.service.dto.MarketHolidayDTO;
import com.rnexchange.service.mapper.MarketHolidayMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link MarketHolidayResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class MarketHolidayResourceIT {

    private static final LocalDate DEFAULT_TRADE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TRADE_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_TRADE_DATE = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REASON = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_HOLIDAY = false;
    private static final Boolean UPDATED_IS_HOLIDAY = true;

    private static final String ENTITY_API_URL = "/api/market-holidays";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MarketHolidayRepository marketHolidayRepository;

    @Mock
    private MarketHolidayRepository marketHolidayRepositoryMock;

    @Autowired
    private MarketHolidayMapper marketHolidayMapper;

    @Mock
    private MarketHolidayService marketHolidayServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMarketHolidayMockMvc;

    private MarketHoliday marketHoliday;

    private MarketHoliday insertedMarketHoliday;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MarketHoliday createEntity() {
        return new MarketHoliday().tradeDate(DEFAULT_TRADE_DATE).reason(DEFAULT_REASON).isHoliday(DEFAULT_IS_HOLIDAY);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MarketHoliday createUpdatedEntity() {
        return new MarketHoliday().tradeDate(UPDATED_TRADE_DATE).reason(UPDATED_REASON).isHoliday(UPDATED_IS_HOLIDAY);
    }

    @BeforeEach
    void initTest() {
        marketHoliday = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMarketHoliday != null) {
            marketHolidayRepository.delete(insertedMarketHoliday);
            insertedMarketHoliday = null;
        }
    }

    @Test
    @Transactional
    void createMarketHoliday() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MarketHoliday
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(marketHoliday);
        var returnedMarketHolidayDTO = om.readValue(
            restMarketHolidayMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marketHolidayDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MarketHolidayDTO.class
        );

        // Validate the MarketHoliday in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMarketHoliday = marketHolidayMapper.toEntity(returnedMarketHolidayDTO);
        assertMarketHolidayUpdatableFieldsEquals(returnedMarketHoliday, getPersistedMarketHoliday(returnedMarketHoliday));

        insertedMarketHoliday = returnedMarketHoliday;
    }

    @Test
    @Transactional
    void createMarketHolidayWithExistingId() throws Exception {
        // Create the MarketHoliday with an existing ID
        marketHoliday.setId(1L);
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(marketHoliday);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMarketHolidayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marketHolidayDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MarketHoliday in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTradeDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        marketHoliday.setTradeDate(null);

        // Create the MarketHoliday, which fails.
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(marketHoliday);

        restMarketHolidayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marketHolidayDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIsHolidayIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        marketHoliday.setIsHoliday(null);

        // Create the MarketHoliday, which fails.
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(marketHoliday);

        restMarketHolidayMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marketHolidayDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMarketHolidays() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList
        restMarketHolidayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(marketHoliday.getId().intValue())))
            .andExpect(jsonPath("$.[*].tradeDate").value(hasItem(DEFAULT_TRADE_DATE.toString())))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)))
            .andExpect(jsonPath("$.[*].isHoliday").value(hasItem(DEFAULT_IS_HOLIDAY)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMarketHolidaysWithEagerRelationshipsIsEnabled() throws Exception {
        when(marketHolidayServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMarketHolidayMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(marketHolidayServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMarketHolidaysWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(marketHolidayServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMarketHolidayMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(marketHolidayRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getMarketHoliday() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get the marketHoliday
        restMarketHolidayMockMvc
            .perform(get(ENTITY_API_URL_ID, marketHoliday.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(marketHoliday.getId().intValue()))
            .andExpect(jsonPath("$.tradeDate").value(DEFAULT_TRADE_DATE.toString()))
            .andExpect(jsonPath("$.reason").value(DEFAULT_REASON))
            .andExpect(jsonPath("$.isHoliday").value(DEFAULT_IS_HOLIDAY));
    }

    @Test
    @Transactional
    void getMarketHolidaysByIdFiltering() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        Long id = marketHoliday.getId();

        defaultMarketHolidayFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultMarketHolidayFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultMarketHolidayFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByTradeDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where tradeDate equals to
        defaultMarketHolidayFiltering("tradeDate.equals=" + DEFAULT_TRADE_DATE, "tradeDate.equals=" + UPDATED_TRADE_DATE);
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByTradeDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where tradeDate in
        defaultMarketHolidayFiltering(
            "tradeDate.in=" + DEFAULT_TRADE_DATE + "," + UPDATED_TRADE_DATE,
            "tradeDate.in=" + UPDATED_TRADE_DATE
        );
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByTradeDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where tradeDate is not null
        defaultMarketHolidayFiltering("tradeDate.specified=true", "tradeDate.specified=false");
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByTradeDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where tradeDate is greater than or equal to
        defaultMarketHolidayFiltering(
            "tradeDate.greaterThanOrEqual=" + DEFAULT_TRADE_DATE,
            "tradeDate.greaterThanOrEqual=" + UPDATED_TRADE_DATE
        );
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByTradeDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where tradeDate is less than or equal to
        defaultMarketHolidayFiltering("tradeDate.lessThanOrEqual=" + DEFAULT_TRADE_DATE, "tradeDate.lessThanOrEqual=" + SMALLER_TRADE_DATE);
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByTradeDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where tradeDate is less than
        defaultMarketHolidayFiltering("tradeDate.lessThan=" + UPDATED_TRADE_DATE, "tradeDate.lessThan=" + DEFAULT_TRADE_DATE);
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByTradeDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where tradeDate is greater than
        defaultMarketHolidayFiltering("tradeDate.greaterThan=" + SMALLER_TRADE_DATE, "tradeDate.greaterThan=" + DEFAULT_TRADE_DATE);
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByReasonIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where reason equals to
        defaultMarketHolidayFiltering("reason.equals=" + DEFAULT_REASON, "reason.equals=" + UPDATED_REASON);
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByReasonIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where reason in
        defaultMarketHolidayFiltering("reason.in=" + DEFAULT_REASON + "," + UPDATED_REASON, "reason.in=" + UPDATED_REASON);
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByReasonIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where reason is not null
        defaultMarketHolidayFiltering("reason.specified=true", "reason.specified=false");
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByReasonContainsSomething() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where reason contains
        defaultMarketHolidayFiltering("reason.contains=" + DEFAULT_REASON, "reason.contains=" + UPDATED_REASON);
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByReasonNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where reason does not contain
        defaultMarketHolidayFiltering("reason.doesNotContain=" + UPDATED_REASON, "reason.doesNotContain=" + DEFAULT_REASON);
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByIsHolidayIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where isHoliday equals to
        defaultMarketHolidayFiltering("isHoliday.equals=" + DEFAULT_IS_HOLIDAY, "isHoliday.equals=" + UPDATED_IS_HOLIDAY);
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByIsHolidayIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where isHoliday in
        defaultMarketHolidayFiltering(
            "isHoliday.in=" + DEFAULT_IS_HOLIDAY + "," + UPDATED_IS_HOLIDAY,
            "isHoliday.in=" + UPDATED_IS_HOLIDAY
        );
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByIsHolidayIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        // Get all the marketHolidayList where isHoliday is not null
        defaultMarketHolidayFiltering("isHoliday.specified=true", "isHoliday.specified=false");
    }

    @Test
    @Transactional
    void getAllMarketHolidaysByExchangeIsEqualToSomething() throws Exception {
        Exchange exchange;
        if (TestUtil.findAll(em, Exchange.class).isEmpty()) {
            marketHolidayRepository.saveAndFlush(marketHoliday);
            exchange = ExchangeResourceIT.createEntity();
        } else {
            exchange = TestUtil.findAll(em, Exchange.class).get(0);
        }
        em.persist(exchange);
        em.flush();
        marketHoliday.setExchange(exchange);
        marketHolidayRepository.saveAndFlush(marketHoliday);
        Long exchangeId = exchange.getId();
        // Get all the marketHolidayList where exchange equals to exchangeId
        defaultMarketHolidayShouldBeFound("exchangeId.equals=" + exchangeId);

        // Get all the marketHolidayList where exchange equals to (exchangeId + 1)
        defaultMarketHolidayShouldNotBeFound("exchangeId.equals=" + (exchangeId + 1));
    }

    private void defaultMarketHolidayFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultMarketHolidayShouldBeFound(shouldBeFound);
        defaultMarketHolidayShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMarketHolidayShouldBeFound(String filter) throws Exception {
        restMarketHolidayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(marketHoliday.getId().intValue())))
            .andExpect(jsonPath("$.[*].tradeDate").value(hasItem(DEFAULT_TRADE_DATE.toString())))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)))
            .andExpect(jsonPath("$.[*].isHoliday").value(hasItem(DEFAULT_IS_HOLIDAY)));

        // Check, that the count call also returns 1
        restMarketHolidayMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMarketHolidayShouldNotBeFound(String filter) throws Exception {
        restMarketHolidayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMarketHolidayMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMarketHoliday() throws Exception {
        // Get the marketHoliday
        restMarketHolidayMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMarketHoliday() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the marketHoliday
        MarketHoliday updatedMarketHoliday = marketHolidayRepository.findById(marketHoliday.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMarketHoliday are not directly saved in db
        em.detach(updatedMarketHoliday);
        updatedMarketHoliday.tradeDate(UPDATED_TRADE_DATE).reason(UPDATED_REASON).isHoliday(UPDATED_IS_HOLIDAY);
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(updatedMarketHoliday);

        restMarketHolidayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, marketHolidayDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(marketHolidayDTO))
            )
            .andExpect(status().isOk());

        // Validate the MarketHoliday in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMarketHolidayToMatchAllProperties(updatedMarketHoliday);
    }

    @Test
    @Transactional
    void putNonExistingMarketHoliday() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marketHoliday.setId(longCount.incrementAndGet());

        // Create the MarketHoliday
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(marketHoliday);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMarketHolidayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, marketHolidayDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(marketHolidayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MarketHoliday in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMarketHoliday() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marketHoliday.setId(longCount.incrementAndGet());

        // Create the MarketHoliday
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(marketHoliday);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarketHolidayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(marketHolidayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MarketHoliday in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMarketHoliday() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marketHoliday.setId(longCount.incrementAndGet());

        // Create the MarketHoliday
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(marketHoliday);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarketHolidayMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marketHolidayDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MarketHoliday in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMarketHolidayWithPatch() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the marketHoliday using partial update
        MarketHoliday partialUpdatedMarketHoliday = new MarketHoliday();
        partialUpdatedMarketHoliday.setId(marketHoliday.getId());

        partialUpdatedMarketHoliday.isHoliday(UPDATED_IS_HOLIDAY);

        restMarketHolidayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMarketHoliday.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMarketHoliday))
            )
            .andExpect(status().isOk());

        // Validate the MarketHoliday in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMarketHolidayUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMarketHoliday, marketHoliday),
            getPersistedMarketHoliday(marketHoliday)
        );
    }

    @Test
    @Transactional
    void fullUpdateMarketHolidayWithPatch() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the marketHoliday using partial update
        MarketHoliday partialUpdatedMarketHoliday = new MarketHoliday();
        partialUpdatedMarketHoliday.setId(marketHoliday.getId());

        partialUpdatedMarketHoliday.tradeDate(UPDATED_TRADE_DATE).reason(UPDATED_REASON).isHoliday(UPDATED_IS_HOLIDAY);

        restMarketHolidayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMarketHoliday.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMarketHoliday))
            )
            .andExpect(status().isOk());

        // Validate the MarketHoliday in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMarketHolidayUpdatableFieldsEquals(partialUpdatedMarketHoliday, getPersistedMarketHoliday(partialUpdatedMarketHoliday));
    }

    @Test
    @Transactional
    void patchNonExistingMarketHoliday() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marketHoliday.setId(longCount.incrementAndGet());

        // Create the MarketHoliday
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(marketHoliday);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMarketHolidayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, marketHolidayDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(marketHolidayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MarketHoliday in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMarketHoliday() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marketHoliday.setId(longCount.incrementAndGet());

        // Create the MarketHoliday
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(marketHoliday);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarketHolidayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(marketHolidayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MarketHoliday in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMarketHoliday() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marketHoliday.setId(longCount.incrementAndGet());

        // Create the MarketHoliday
        MarketHolidayDTO marketHolidayDTO = marketHolidayMapper.toDto(marketHoliday);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarketHolidayMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(marketHolidayDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MarketHoliday in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMarketHoliday() throws Exception {
        // Initialize the database
        insertedMarketHoliday = marketHolidayRepository.saveAndFlush(marketHoliday);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the marketHoliday
        restMarketHolidayMockMvc
            .perform(delete(ENTITY_API_URL_ID, marketHoliday.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return marketHolidayRepository.count();
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

    protected MarketHoliday getPersistedMarketHoliday(MarketHoliday marketHoliday) {
        return marketHolidayRepository.findById(marketHoliday.getId()).orElseThrow();
    }

    protected void assertPersistedMarketHolidayToMatchAllProperties(MarketHoliday expectedMarketHoliday) {
        assertMarketHolidayAllPropertiesEquals(expectedMarketHoliday, getPersistedMarketHoliday(expectedMarketHoliday));
    }

    protected void assertPersistedMarketHolidayToMatchUpdatableProperties(MarketHoliday expectedMarketHoliday) {
        assertMarketHolidayAllUpdatablePropertiesEquals(expectedMarketHoliday, getPersistedMarketHoliday(expectedMarketHoliday));
    }
}
