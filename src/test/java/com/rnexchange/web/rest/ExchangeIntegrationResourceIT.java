package com.rnexchange.web.rest;

import static com.rnexchange.domain.ExchangeIntegrationAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.ExchangeIntegration;
import com.rnexchange.domain.enumeration.IntegrationStatus;
import com.rnexchange.repository.ExchangeIntegrationRepository;
import com.rnexchange.service.ExchangeIntegrationService;
import com.rnexchange.service.dto.ExchangeIntegrationDTO;
import com.rnexchange.service.mapper.ExchangeIntegrationMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link ExchangeIntegrationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ExchangeIntegrationResourceIT {

    private static final String DEFAULT_PROVIDER = "AAAAAAAAAA";
    private static final String UPDATED_PROVIDER = "BBBBBBBBBB";

    private static final String DEFAULT_API_KEY = "AAAAAAAAAA";
    private static final String UPDATED_API_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_API_SECRET = "AAAAAAAAAA";
    private static final String UPDATED_API_SECRET = "BBBBBBBBBB";

    private static final IntegrationStatus DEFAULT_STATUS = IntegrationStatus.DISABLED;
    private static final IntegrationStatus UPDATED_STATUS = IntegrationStatus.ENABLED;

    private static final Instant DEFAULT_LAST_HEARTBEAT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_HEARTBEAT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/exchange-integrations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ExchangeIntegrationRepository exchangeIntegrationRepository;

    @Mock
    private ExchangeIntegrationRepository exchangeIntegrationRepositoryMock;

    @Autowired
    private ExchangeIntegrationMapper exchangeIntegrationMapper;

    @Mock
    private ExchangeIntegrationService exchangeIntegrationServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restExchangeIntegrationMockMvc;

    private ExchangeIntegration exchangeIntegration;

    private ExchangeIntegration insertedExchangeIntegration;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ExchangeIntegration createEntity() {
        return new ExchangeIntegration()
            .provider(DEFAULT_PROVIDER)
            .apiKey(DEFAULT_API_KEY)
            .apiSecret(DEFAULT_API_SECRET)
            .status(DEFAULT_STATUS)
            .lastHeartbeat(DEFAULT_LAST_HEARTBEAT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ExchangeIntegration createUpdatedEntity() {
        return new ExchangeIntegration()
            .provider(UPDATED_PROVIDER)
            .apiKey(UPDATED_API_KEY)
            .apiSecret(UPDATED_API_SECRET)
            .status(UPDATED_STATUS)
            .lastHeartbeat(UPDATED_LAST_HEARTBEAT);
    }

    @BeforeEach
    void initTest() {
        exchangeIntegration = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedExchangeIntegration != null) {
            exchangeIntegrationRepository.delete(insertedExchangeIntegration);
            insertedExchangeIntegration = null;
        }
    }

    @Test
    @Transactional
    void createExchangeIntegration() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ExchangeIntegration
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(exchangeIntegration);
        var returnedExchangeIntegrationDTO = om.readValue(
            restExchangeIntegrationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeIntegrationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ExchangeIntegrationDTO.class
        );

        // Validate the ExchangeIntegration in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedExchangeIntegration = exchangeIntegrationMapper.toEntity(returnedExchangeIntegrationDTO);
        assertExchangeIntegrationUpdatableFieldsEquals(
            returnedExchangeIntegration,
            getPersistedExchangeIntegration(returnedExchangeIntegration)
        );

        insertedExchangeIntegration = returnedExchangeIntegration;
    }

    @Test
    @Transactional
    void createExchangeIntegrationWithExistingId() throws Exception {
        // Create the ExchangeIntegration with an existing ID
        exchangeIntegration.setId(1L);
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(exchangeIntegration);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restExchangeIntegrationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeIntegrationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ExchangeIntegration in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkProviderIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        exchangeIntegration.setProvider(null);

        // Create the ExchangeIntegration, which fails.
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(exchangeIntegration);

        restExchangeIntegrationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeIntegrationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        exchangeIntegration.setStatus(null);

        // Create the ExchangeIntegration, which fails.
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(exchangeIntegration);

        restExchangeIntegrationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeIntegrationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrations() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList
        restExchangeIntegrationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(exchangeIntegration.getId().intValue())))
            .andExpect(jsonPath("$.[*].provider").value(hasItem(DEFAULT_PROVIDER)))
            .andExpect(jsonPath("$.[*].apiKey").value(hasItem(DEFAULT_API_KEY)))
            .andExpect(jsonPath("$.[*].apiSecret").value(hasItem(DEFAULT_API_SECRET)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].lastHeartbeat").value(hasItem(DEFAULT_LAST_HEARTBEAT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllExchangeIntegrationsWithEagerRelationshipsIsEnabled() throws Exception {
        when(exchangeIntegrationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restExchangeIntegrationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(exchangeIntegrationServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllExchangeIntegrationsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(exchangeIntegrationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restExchangeIntegrationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(exchangeIntegrationRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getExchangeIntegration() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get the exchangeIntegration
        restExchangeIntegrationMockMvc
            .perform(get(ENTITY_API_URL_ID, exchangeIntegration.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(exchangeIntegration.getId().intValue()))
            .andExpect(jsonPath("$.provider").value(DEFAULT_PROVIDER))
            .andExpect(jsonPath("$.apiKey").value(DEFAULT_API_KEY))
            .andExpect(jsonPath("$.apiSecret").value(DEFAULT_API_SECRET))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.lastHeartbeat").value(DEFAULT_LAST_HEARTBEAT.toString()));
    }

    @Test
    @Transactional
    void getExchangeIntegrationsByIdFiltering() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        Long id = exchangeIntegration.getId();

        defaultExchangeIntegrationFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultExchangeIntegrationFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultExchangeIntegrationFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByProviderIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where provider equals to
        defaultExchangeIntegrationFiltering("provider.equals=" + DEFAULT_PROVIDER, "provider.equals=" + UPDATED_PROVIDER);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByProviderIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where provider in
        defaultExchangeIntegrationFiltering("provider.in=" + DEFAULT_PROVIDER + "," + UPDATED_PROVIDER, "provider.in=" + UPDATED_PROVIDER);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByProviderIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where provider is not null
        defaultExchangeIntegrationFiltering("provider.specified=true", "provider.specified=false");
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByProviderContainsSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where provider contains
        defaultExchangeIntegrationFiltering("provider.contains=" + DEFAULT_PROVIDER, "provider.contains=" + UPDATED_PROVIDER);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByProviderNotContainsSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where provider does not contain
        defaultExchangeIntegrationFiltering("provider.doesNotContain=" + UPDATED_PROVIDER, "provider.doesNotContain=" + DEFAULT_PROVIDER);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByApiKeyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where apiKey equals to
        defaultExchangeIntegrationFiltering("apiKey.equals=" + DEFAULT_API_KEY, "apiKey.equals=" + UPDATED_API_KEY);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByApiKeyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where apiKey in
        defaultExchangeIntegrationFiltering("apiKey.in=" + DEFAULT_API_KEY + "," + UPDATED_API_KEY, "apiKey.in=" + UPDATED_API_KEY);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByApiKeyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where apiKey is not null
        defaultExchangeIntegrationFiltering("apiKey.specified=true", "apiKey.specified=false");
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByApiKeyContainsSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where apiKey contains
        defaultExchangeIntegrationFiltering("apiKey.contains=" + DEFAULT_API_KEY, "apiKey.contains=" + UPDATED_API_KEY);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByApiKeyNotContainsSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where apiKey does not contain
        defaultExchangeIntegrationFiltering("apiKey.doesNotContain=" + UPDATED_API_KEY, "apiKey.doesNotContain=" + DEFAULT_API_KEY);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByApiSecretIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where apiSecret equals to
        defaultExchangeIntegrationFiltering("apiSecret.equals=" + DEFAULT_API_SECRET, "apiSecret.equals=" + UPDATED_API_SECRET);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByApiSecretIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where apiSecret in
        defaultExchangeIntegrationFiltering(
            "apiSecret.in=" + DEFAULT_API_SECRET + "," + UPDATED_API_SECRET,
            "apiSecret.in=" + UPDATED_API_SECRET
        );
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByApiSecretIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where apiSecret is not null
        defaultExchangeIntegrationFiltering("apiSecret.specified=true", "apiSecret.specified=false");
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByApiSecretContainsSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where apiSecret contains
        defaultExchangeIntegrationFiltering("apiSecret.contains=" + DEFAULT_API_SECRET, "apiSecret.contains=" + UPDATED_API_SECRET);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByApiSecretNotContainsSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where apiSecret does not contain
        defaultExchangeIntegrationFiltering(
            "apiSecret.doesNotContain=" + UPDATED_API_SECRET,
            "apiSecret.doesNotContain=" + DEFAULT_API_SECRET
        );
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where status equals to
        defaultExchangeIntegrationFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where status in
        defaultExchangeIntegrationFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where status is not null
        defaultExchangeIntegrationFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByLastHeartbeatIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where lastHeartbeat equals to
        defaultExchangeIntegrationFiltering(
            "lastHeartbeat.equals=" + DEFAULT_LAST_HEARTBEAT,
            "lastHeartbeat.equals=" + UPDATED_LAST_HEARTBEAT
        );
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByLastHeartbeatIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where lastHeartbeat in
        defaultExchangeIntegrationFiltering(
            "lastHeartbeat.in=" + DEFAULT_LAST_HEARTBEAT + "," + UPDATED_LAST_HEARTBEAT,
            "lastHeartbeat.in=" + UPDATED_LAST_HEARTBEAT
        );
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByLastHeartbeatIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        // Get all the exchangeIntegrationList where lastHeartbeat is not null
        defaultExchangeIntegrationFiltering("lastHeartbeat.specified=true", "lastHeartbeat.specified=false");
    }

    @Test
    @Transactional
    void getAllExchangeIntegrationsByExchangeIsEqualToSomething() throws Exception {
        Exchange exchange;
        if (TestUtil.findAll(em, Exchange.class).isEmpty()) {
            exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);
            exchange = ExchangeResourceIT.createEntity();
        } else {
            exchange = TestUtil.findAll(em, Exchange.class).get(0);
        }
        em.persist(exchange);
        em.flush();
        exchangeIntegration.setExchange(exchange);
        exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);
        Long exchangeId = exchange.getId();
        // Get all the exchangeIntegrationList where exchange equals to exchangeId
        defaultExchangeIntegrationShouldBeFound("exchangeId.equals=" + exchangeId);

        // Get all the exchangeIntegrationList where exchange equals to (exchangeId + 1)
        defaultExchangeIntegrationShouldNotBeFound("exchangeId.equals=" + (exchangeId + 1));
    }

    private void defaultExchangeIntegrationFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultExchangeIntegrationShouldBeFound(shouldBeFound);
        defaultExchangeIntegrationShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultExchangeIntegrationShouldBeFound(String filter) throws Exception {
        restExchangeIntegrationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(exchangeIntegration.getId().intValue())))
            .andExpect(jsonPath("$.[*].provider").value(hasItem(DEFAULT_PROVIDER)))
            .andExpect(jsonPath("$.[*].apiKey").value(hasItem(DEFAULT_API_KEY)))
            .andExpect(jsonPath("$.[*].apiSecret").value(hasItem(DEFAULT_API_SECRET)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].lastHeartbeat").value(hasItem(DEFAULT_LAST_HEARTBEAT.toString())));

        // Check, that the count call also returns 1
        restExchangeIntegrationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultExchangeIntegrationShouldNotBeFound(String filter) throws Exception {
        restExchangeIntegrationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restExchangeIntegrationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingExchangeIntegration() throws Exception {
        // Get the exchangeIntegration
        restExchangeIntegrationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingExchangeIntegration() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the exchangeIntegration
        ExchangeIntegration updatedExchangeIntegration = exchangeIntegrationRepository.findById(exchangeIntegration.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedExchangeIntegration are not directly saved in db
        em.detach(updatedExchangeIntegration);
        updatedExchangeIntegration
            .provider(UPDATED_PROVIDER)
            .apiKey(UPDATED_API_KEY)
            .apiSecret(UPDATED_API_SECRET)
            .status(UPDATED_STATUS)
            .lastHeartbeat(UPDATED_LAST_HEARTBEAT);
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(updatedExchangeIntegration);

        restExchangeIntegrationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, exchangeIntegrationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(exchangeIntegrationDTO))
            )
            .andExpect(status().isOk());

        // Validate the ExchangeIntegration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedExchangeIntegrationToMatchAllProperties(updatedExchangeIntegration);
    }

    @Test
    @Transactional
    void putNonExistingExchangeIntegration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeIntegration.setId(longCount.incrementAndGet());

        // Create the ExchangeIntegration
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(exchangeIntegration);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExchangeIntegrationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, exchangeIntegrationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(exchangeIntegrationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExchangeIntegration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchExchangeIntegration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeIntegration.setId(longCount.incrementAndGet());

        // Create the ExchangeIntegration
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(exchangeIntegration);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeIntegrationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(exchangeIntegrationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExchangeIntegration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamExchangeIntegration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeIntegration.setId(longCount.incrementAndGet());

        // Create the ExchangeIntegration
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(exchangeIntegration);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeIntegrationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeIntegrationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ExchangeIntegration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateExchangeIntegrationWithPatch() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the exchangeIntegration using partial update
        ExchangeIntegration partialUpdatedExchangeIntegration = new ExchangeIntegration();
        partialUpdatedExchangeIntegration.setId(exchangeIntegration.getId());

        partialUpdatedExchangeIntegration
            .provider(UPDATED_PROVIDER)
            .apiKey(UPDATED_API_KEY)
            .apiSecret(UPDATED_API_SECRET)
            .lastHeartbeat(UPDATED_LAST_HEARTBEAT);

        restExchangeIntegrationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExchangeIntegration.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExchangeIntegration))
            )
            .andExpect(status().isOk());

        // Validate the ExchangeIntegration in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExchangeIntegrationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedExchangeIntegration, exchangeIntegration),
            getPersistedExchangeIntegration(exchangeIntegration)
        );
    }

    @Test
    @Transactional
    void fullUpdateExchangeIntegrationWithPatch() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the exchangeIntegration using partial update
        ExchangeIntegration partialUpdatedExchangeIntegration = new ExchangeIntegration();
        partialUpdatedExchangeIntegration.setId(exchangeIntegration.getId());

        partialUpdatedExchangeIntegration
            .provider(UPDATED_PROVIDER)
            .apiKey(UPDATED_API_KEY)
            .apiSecret(UPDATED_API_SECRET)
            .status(UPDATED_STATUS)
            .lastHeartbeat(UPDATED_LAST_HEARTBEAT);

        restExchangeIntegrationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExchangeIntegration.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExchangeIntegration))
            )
            .andExpect(status().isOk());

        // Validate the ExchangeIntegration in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExchangeIntegrationUpdatableFieldsEquals(
            partialUpdatedExchangeIntegration,
            getPersistedExchangeIntegration(partialUpdatedExchangeIntegration)
        );
    }

    @Test
    @Transactional
    void patchNonExistingExchangeIntegration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeIntegration.setId(longCount.incrementAndGet());

        // Create the ExchangeIntegration
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(exchangeIntegration);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExchangeIntegrationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, exchangeIntegrationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(exchangeIntegrationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExchangeIntegration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchExchangeIntegration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeIntegration.setId(longCount.incrementAndGet());

        // Create the ExchangeIntegration
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(exchangeIntegration);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeIntegrationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(exchangeIntegrationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExchangeIntegration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamExchangeIntegration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeIntegration.setId(longCount.incrementAndGet());

        // Create the ExchangeIntegration
        ExchangeIntegrationDTO exchangeIntegrationDTO = exchangeIntegrationMapper.toDto(exchangeIntegration);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeIntegrationMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(exchangeIntegrationDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ExchangeIntegration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteExchangeIntegration() throws Exception {
        // Initialize the database
        insertedExchangeIntegration = exchangeIntegrationRepository.saveAndFlush(exchangeIntegration);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the exchangeIntegration
        restExchangeIntegrationMockMvc
            .perform(delete(ENTITY_API_URL_ID, exchangeIntegration.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return exchangeIntegrationRepository.count();
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

    protected ExchangeIntegration getPersistedExchangeIntegration(ExchangeIntegration exchangeIntegration) {
        return exchangeIntegrationRepository.findById(exchangeIntegration.getId()).orElseThrow();
    }

    protected void assertPersistedExchangeIntegrationToMatchAllProperties(ExchangeIntegration expectedExchangeIntegration) {
        assertExchangeIntegrationAllPropertiesEquals(
            expectedExchangeIntegration,
            getPersistedExchangeIntegration(expectedExchangeIntegration)
        );
    }

    protected void assertPersistedExchangeIntegrationToMatchUpdatableProperties(ExchangeIntegration expectedExchangeIntegration) {
        assertExchangeIntegrationAllUpdatablePropertiesEquals(
            expectedExchangeIntegration,
            getPersistedExchangeIntegration(expectedExchangeIntegration)
        );
    }
}
