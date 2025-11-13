package com.rnexchange.web.rest;

import static com.rnexchange.domain.BrokerAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Broker;
import com.rnexchange.domain.Exchange;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.service.BrokerService;
import com.rnexchange.service.dto.BrokerDTO;
import com.rnexchange.service.mapper.BrokerMapper;
import com.rnexchange.service.seed.BaselineSeedService;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
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
 * Integration tests for the {@link BrokerResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class BrokerResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/brokers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Mock
    private BrokerRepository brokerRepositoryMock;

    @Autowired
    private BrokerMapper brokerMapper;

    @Mock
    private BrokerService brokerServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBrokerMockMvc;

    private Broker broker;

    private Broker insertedBroker;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Broker createEntity() {
        return new Broker().code(DEFAULT_CODE).name(DEFAULT_NAME).status(DEFAULT_STATUS).createdDate(DEFAULT_CREATED_DATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Broker createUpdatedEntity() {
        return new Broker().code(UPDATED_CODE).name(UPDATED_NAME).status(UPDATED_STATUS).createdDate(UPDATED_CREATED_DATE);
    }

    @BeforeEach
    void initTest() {
        broker = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedBroker != null) {
            brokerRepository.delete(insertedBroker);
            insertedBroker = null;
        }
    }

    @Test
    @Transactional
    void createBroker() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);
        var returnedBrokerDTO = om.readValue(
            restBrokerMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BrokerDTO.class
        );

        // Validate the Broker in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBroker = brokerMapper.toEntity(returnedBrokerDTO);
        assertBrokerUpdatableFieldsEquals(returnedBroker, getPersistedBroker(returnedBroker));

        insertedBroker = returnedBroker;
    }

    @Test
    @Transactional
    void createBrokerWithExistingId() throws Exception {
        // Create the Broker with an existing ID
        broker.setId(1L);
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBrokerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        broker.setCode(null);

        // Create the Broker, which fails.
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        restBrokerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        broker.setName(null);

        // Create the Broker, which fails.
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        restBrokerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        broker.setStatus(null);

        // Create the Broker, which fails.
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        restBrokerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBrokers() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(broker.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBrokersWithEagerRelationshipsIsEnabled() throws Exception {
        when(brokerServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restBrokerMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(brokerServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBrokersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(brokerServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restBrokerMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(brokerRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getBroker() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get the broker
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL_ID, broker.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(broker.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    @Transactional
    void getBrokerBaselineView() throws Exception {
        baselineSeedService.runBaselineSeedBlocking(BaselineSeedRequest.builder().invocationId(UUID.randomUUID()).build());

        Broker baselineBroker = brokerRepository.findOneByCode("RN_DEMO").orElseThrow();

        restBrokerMockMvc
            .perform(get(ENTITY_API_URL_ID + "/baseline", baselineBroker.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.code").value("RN_DEMO"))
            .andExpect(jsonPath("$.name").value("RN DEMO BROKING"))
            .andExpect(jsonPath("$.exchangeCode").value("NSE"))
            .andExpect(jsonPath("$.exchangeName").value("National Stock Exchange"))
            .andExpect(jsonPath("$.exchangeTimezone").value("Asia/Kolkata"))
            .andExpect(jsonPath("$.exchangeMemberships", containsInAnyOrder("NSE", "BSE", "MCX")))
            .andExpect(jsonPath("$.brokerAdminLogin").value("broker-admin"))
            .andExpect(jsonPath("$.instrumentCount").value(greaterThanOrEqualTo(10)))
            .andExpect(jsonPath("$.instrumentCatalog.length()").value(greaterThanOrEqualTo(10)))
            .andExpect(jsonPath("$.instrumentCatalog[?(@.symbol=='RELIANCE')].exchangeCode").value(hasItem("NSE")))
            .andExpect(jsonPath("$.instrumentCatalog[?(@.symbol=='RELIANCE')].assetClass").value(hasItem("EQUITY")))
            .andExpect(jsonPath("$.instrumentCatalog[?(@.symbol=='RELIANCE')].tickSize").value(hasItem(0.05)))
            .andExpect(jsonPath("$.instrumentCatalog[?(@.symbol=='RELIANCE')].lotSize").value(hasItem(1)))
            .andExpect(jsonPath("$.instrumentCatalog[?(@.symbol=='RELIANCE_BSE')].exchangeCode").value(hasItem("BSE")))
            .andExpect(jsonPath("$.instrumentCatalog[?(@.symbol=='CRUDEOIL')].exchangeCode").value(hasItem("MCX")))
            .andExpect(jsonPath("$.instrumentCatalog[?(@.symbol=='CRUDEOIL')].assetClass").value(hasItem("COMMODITY")))
            .andExpect(jsonPath("$.instrumentCatalog[?(@.symbol=='CRUDEOIL')].tickSize").value(hasItem(1.0)))
            .andExpect(jsonPath("$.instrumentCatalog[?(@.symbol=='CRUDEOIL')].lotSize").value(hasItem(10)));
    }

    @Test
    @Transactional
    void getBrokersByIdFiltering() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        Long id = broker.getId();

        defaultBrokerFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultBrokerFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultBrokerFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllBrokersByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where code equals to
        defaultBrokerFiltering("code.equals=" + DEFAULT_CODE, "code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllBrokersByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where code in
        defaultBrokerFiltering("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE, "code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllBrokersByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where code is not null
        defaultBrokerFiltering("code.specified=true", "code.specified=false");
    }

    @Test
    @Transactional
    void getAllBrokersByCodeContainsSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where code contains
        defaultBrokerFiltering("code.contains=" + DEFAULT_CODE, "code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllBrokersByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where code does not contain
        defaultBrokerFiltering("code.doesNotContain=" + UPDATED_CODE, "code.doesNotContain=" + DEFAULT_CODE);
    }

    @Test
    @Transactional
    void getAllBrokersByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where name equals to
        defaultBrokerFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllBrokersByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where name in
        defaultBrokerFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllBrokersByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where name is not null
        defaultBrokerFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllBrokersByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where name contains
        defaultBrokerFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllBrokersByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where name does not contain
        defaultBrokerFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllBrokersByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where status equals to
        defaultBrokerFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllBrokersByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where status in
        defaultBrokerFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllBrokersByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where status is not null
        defaultBrokerFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllBrokersByStatusContainsSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where status contains
        defaultBrokerFiltering("status.contains=" + DEFAULT_STATUS, "status.contains=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllBrokersByStatusNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where status does not contain
        defaultBrokerFiltering("status.doesNotContain=" + UPDATED_STATUS, "status.doesNotContain=" + DEFAULT_STATUS);
    }

    @Test
    @Transactional
    void getAllBrokersByCreatedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where createdDate equals to
        defaultBrokerFiltering("createdDate.equals=" + DEFAULT_CREATED_DATE, "createdDate.equals=" + UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void getAllBrokersByCreatedDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where createdDate in
        defaultBrokerFiltering(
            "createdDate.in=" + DEFAULT_CREATED_DATE + "," + UPDATED_CREATED_DATE,
            "createdDate.in=" + UPDATED_CREATED_DATE
        );
    }

    @Test
    @Transactional
    void getAllBrokersByCreatedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where createdDate is not null
        defaultBrokerFiltering("createdDate.specified=true", "createdDate.specified=false");
    }

    @Test
    @Transactional
    void getAllBrokersByExchangeIsEqualToSomething() throws Exception {
        Exchange exchange;
        if (TestUtil.findAll(em, Exchange.class).isEmpty()) {
            brokerRepository.saveAndFlush(broker);
            exchange = ExchangeResourceIT.createEntity();
        } else {
            exchange = TestUtil.findAll(em, Exchange.class).get(0);
        }
        em.persist(exchange);
        em.flush();
        broker.setExchange(exchange);
        brokerRepository.saveAndFlush(broker);
        Long exchangeId = exchange.getId();
        // Get all the brokerList where exchange equals to exchangeId
        defaultBrokerShouldBeFound("exchangeId.equals=" + exchangeId);

        // Get all the brokerList where exchange equals to (exchangeId + 1)
        defaultBrokerShouldNotBeFound("exchangeId.equals=" + (exchangeId + 1));
    }

    private void defaultBrokerFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultBrokerShouldBeFound(shouldBeFound);
        defaultBrokerShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBrokerShouldBeFound(String filter) throws Exception {
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(broker.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())));

        // Check, that the count call also returns 1
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").value(greaterThanOrEqualTo(1)));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultBrokerShouldNotBeFound(String filter) throws Exception {
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.[*].id").value(not(hasItem(broker.getId().intValue()))));

        // Check, that the count call also returns 0
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").value(greaterThanOrEqualTo(0)));
    }

    @Test
    @Transactional
    void getNonExistingBroker() throws Exception {
        // Get the broker
        restBrokerMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBroker() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the broker
        Broker updatedBroker = brokerRepository.findById(broker.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBroker are not directly saved in db
        em.detach(updatedBroker);
        updatedBroker.code(UPDATED_CODE).name(UPDATED_NAME).status(UPDATED_STATUS).createdDate(UPDATED_CREATED_DATE);
        BrokerDTO brokerDTO = brokerMapper.toDto(updatedBroker);

        restBrokerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, brokerDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isOk());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBrokerToMatchAllProperties(updatedBroker);
    }

    @Test
    @Transactional
    void putNonExistingBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(longCount.incrementAndGet());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, brokerDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(longCount.incrementAndGet());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(longCount.incrementAndGet());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBrokerWithPatch() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the broker using partial update
        Broker partialUpdatedBroker = new Broker();
        partialUpdatedBroker.setId(broker.getId());

        partialUpdatedBroker.code(UPDATED_CODE).createdDate(UPDATED_CREATED_DATE);

        restBrokerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBroker.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBroker))
            )
            .andExpect(status().isOk());

        // Validate the Broker in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedBroker, broker), getPersistedBroker(broker));
    }

    @Test
    @Transactional
    void fullUpdateBrokerWithPatch() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the broker using partial update
        Broker partialUpdatedBroker = new Broker();
        partialUpdatedBroker.setId(broker.getId());

        partialUpdatedBroker.code(UPDATED_CODE).name(UPDATED_NAME).status(UPDATED_STATUS).createdDate(UPDATED_CREATED_DATE);

        restBrokerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBroker.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBroker))
            )
            .andExpect(status().isOk());

        // Validate the Broker in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerUpdatableFieldsEquals(partialUpdatedBroker, getPersistedBroker(partialUpdatedBroker));
    }

    @Test
    @Transactional
    void patchNonExistingBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(longCount.incrementAndGet());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, brokerDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(longCount.incrementAndGet());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(longCount.incrementAndGet());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(brokerDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBroker() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the broker
        restBrokerMockMvc
            .perform(delete(ENTITY_API_URL_ID, broker.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return brokerRepository.count();
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

    protected Broker getPersistedBroker(Broker broker) {
        return brokerRepository.findById(broker.getId()).orElseThrow();
    }

    protected void assertPersistedBrokerToMatchAllProperties(Broker expectedBroker) {
        assertBrokerAllPropertiesEquals(expectedBroker, getPersistedBroker(expectedBroker));
    }

    protected void assertPersistedBrokerToMatchUpdatableProperties(Broker expectedBroker) {
        assertBrokerAllUpdatablePropertiesEquals(expectedBroker, getPersistedBroker(expectedBroker));
    }
}
