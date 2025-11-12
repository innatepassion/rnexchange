package com.rnexchange.web.rest;

import static com.rnexchange.domain.SettlementBatchAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.SettlementBatch;
import com.rnexchange.domain.enumeration.SettlementKind;
import com.rnexchange.domain.enumeration.SettlementStatus;
import com.rnexchange.repository.SettlementBatchRepository;
import com.rnexchange.service.SettlementBatchService;
import com.rnexchange.service.dto.SettlementBatchDTO;
import com.rnexchange.service.mapper.SettlementBatchMapper;
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
 * Integration tests for the {@link SettlementBatchResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class SettlementBatchResourceIT {

    private static final LocalDate DEFAULT_REF_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_REF_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_REF_DATE = LocalDate.ofEpochDay(-1L);

    private static final SettlementKind DEFAULT_KIND = SettlementKind.EOD;
    private static final SettlementKind UPDATED_KIND = SettlementKind.VARIATION;

    private static final SettlementStatus DEFAULT_STATUS = SettlementStatus.CREATED;
    private static final SettlementStatus UPDATED_STATUS = SettlementStatus.PROCESSED;

    private static final String DEFAULT_REMARKS = "AAAAAAAAAA";
    private static final String UPDATED_REMARKS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/settlement-batches";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SettlementBatchRepository settlementBatchRepository;

    @Mock
    private SettlementBatchRepository settlementBatchRepositoryMock;

    @Autowired
    private SettlementBatchMapper settlementBatchMapper;

    @Mock
    private SettlementBatchService settlementBatchServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSettlementBatchMockMvc;

    private SettlementBatch settlementBatch;

    private SettlementBatch insertedSettlementBatch;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SettlementBatch createEntity() {
        return new SettlementBatch().refDate(DEFAULT_REF_DATE).kind(DEFAULT_KIND).status(DEFAULT_STATUS).remarks(DEFAULT_REMARKS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SettlementBatch createUpdatedEntity() {
        return new SettlementBatch().refDate(UPDATED_REF_DATE).kind(UPDATED_KIND).status(UPDATED_STATUS).remarks(UPDATED_REMARKS);
    }

    @BeforeEach
    void initTest() {
        settlementBatch = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSettlementBatch != null) {
            settlementBatchRepository.delete(insertedSettlementBatch);
            insertedSettlementBatch = null;
        }
    }

    @Test
    @Transactional
    void createSettlementBatch() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the SettlementBatch
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);
        var returnedSettlementBatchDTO = om.readValue(
            restSettlementBatchMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(settlementBatchDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SettlementBatchDTO.class
        );

        // Validate the SettlementBatch in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSettlementBatch = settlementBatchMapper.toEntity(returnedSettlementBatchDTO);
        assertSettlementBatchUpdatableFieldsEquals(returnedSettlementBatch, getPersistedSettlementBatch(returnedSettlementBatch));

        insertedSettlementBatch = returnedSettlementBatch;
    }

    @Test
    @Transactional
    void createSettlementBatchWithExistingId() throws Exception {
        // Create the SettlementBatch with an existing ID
        settlementBatch.setId(1L);
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSettlementBatchMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(settlementBatchDTO)))
            .andExpect(status().isBadRequest());

        // Validate the SettlementBatch in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRefDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        settlementBatch.setRefDate(null);

        // Create the SettlementBatch, which fails.
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);

        restSettlementBatchMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(settlementBatchDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkKindIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        settlementBatch.setKind(null);

        // Create the SettlementBatch, which fails.
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);

        restSettlementBatchMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(settlementBatchDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        settlementBatch.setStatus(null);

        // Create the SettlementBatch, which fails.
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);

        restSettlementBatchMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(settlementBatchDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSettlementBatches() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList
        restSettlementBatchMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(settlementBatch.getId().intValue())))
            .andExpect(jsonPath("$.[*].refDate").value(hasItem(DEFAULT_REF_DATE.toString())))
            .andExpect(jsonPath("$.[*].kind").value(hasItem(DEFAULT_KIND.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].remarks").value(hasItem(DEFAULT_REMARKS)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSettlementBatchesWithEagerRelationshipsIsEnabled() throws Exception {
        when(settlementBatchServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSettlementBatchMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(settlementBatchServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSettlementBatchesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(settlementBatchServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSettlementBatchMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(settlementBatchRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getSettlementBatch() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get the settlementBatch
        restSettlementBatchMockMvc
            .perform(get(ENTITY_API_URL_ID, settlementBatch.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(settlementBatch.getId().intValue()))
            .andExpect(jsonPath("$.refDate").value(DEFAULT_REF_DATE.toString()))
            .andExpect(jsonPath("$.kind").value(DEFAULT_KIND.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.remarks").value(DEFAULT_REMARKS));
    }

    @Test
    @Transactional
    void getSettlementBatchesByIdFiltering() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        Long id = settlementBatch.getId();

        defaultSettlementBatchFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultSettlementBatchFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultSettlementBatchFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRefDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where refDate equals to
        defaultSettlementBatchFiltering("refDate.equals=" + DEFAULT_REF_DATE, "refDate.equals=" + UPDATED_REF_DATE);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRefDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where refDate in
        defaultSettlementBatchFiltering("refDate.in=" + DEFAULT_REF_DATE + "," + UPDATED_REF_DATE, "refDate.in=" + UPDATED_REF_DATE);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRefDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where refDate is not null
        defaultSettlementBatchFiltering("refDate.specified=true", "refDate.specified=false");
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRefDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where refDate is greater than or equal to
        defaultSettlementBatchFiltering("refDate.greaterThanOrEqual=" + DEFAULT_REF_DATE, "refDate.greaterThanOrEqual=" + UPDATED_REF_DATE);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRefDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where refDate is less than or equal to
        defaultSettlementBatchFiltering("refDate.lessThanOrEqual=" + DEFAULT_REF_DATE, "refDate.lessThanOrEqual=" + SMALLER_REF_DATE);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRefDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where refDate is less than
        defaultSettlementBatchFiltering("refDate.lessThan=" + UPDATED_REF_DATE, "refDate.lessThan=" + DEFAULT_REF_DATE);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRefDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where refDate is greater than
        defaultSettlementBatchFiltering("refDate.greaterThan=" + SMALLER_REF_DATE, "refDate.greaterThan=" + DEFAULT_REF_DATE);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByKindIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where kind equals to
        defaultSettlementBatchFiltering("kind.equals=" + DEFAULT_KIND, "kind.equals=" + UPDATED_KIND);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByKindIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where kind in
        defaultSettlementBatchFiltering("kind.in=" + DEFAULT_KIND + "," + UPDATED_KIND, "kind.in=" + UPDATED_KIND);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByKindIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where kind is not null
        defaultSettlementBatchFiltering("kind.specified=true", "kind.specified=false");
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where status equals to
        defaultSettlementBatchFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where status in
        defaultSettlementBatchFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where status is not null
        defaultSettlementBatchFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRemarksIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where remarks equals to
        defaultSettlementBatchFiltering("remarks.equals=" + DEFAULT_REMARKS, "remarks.equals=" + UPDATED_REMARKS);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRemarksIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where remarks in
        defaultSettlementBatchFiltering("remarks.in=" + DEFAULT_REMARKS + "," + UPDATED_REMARKS, "remarks.in=" + UPDATED_REMARKS);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRemarksIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where remarks is not null
        defaultSettlementBatchFiltering("remarks.specified=true", "remarks.specified=false");
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRemarksContainsSomething() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where remarks contains
        defaultSettlementBatchFiltering("remarks.contains=" + DEFAULT_REMARKS, "remarks.contains=" + UPDATED_REMARKS);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByRemarksNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        // Get all the settlementBatchList where remarks does not contain
        defaultSettlementBatchFiltering("remarks.doesNotContain=" + UPDATED_REMARKS, "remarks.doesNotContain=" + DEFAULT_REMARKS);
    }

    @Test
    @Transactional
    void getAllSettlementBatchesByExchangeIsEqualToSomething() throws Exception {
        Exchange exchange;
        if (TestUtil.findAll(em, Exchange.class).isEmpty()) {
            settlementBatchRepository.saveAndFlush(settlementBatch);
            exchange = ExchangeResourceIT.createEntity();
        } else {
            exchange = TestUtil.findAll(em, Exchange.class).get(0);
        }
        em.persist(exchange);
        em.flush();
        settlementBatch.setExchange(exchange);
        settlementBatchRepository.saveAndFlush(settlementBatch);
        Long exchangeId = exchange.getId();
        // Get all the settlementBatchList where exchange equals to exchangeId
        defaultSettlementBatchShouldBeFound("exchangeId.equals=" + exchangeId);

        // Get all the settlementBatchList where exchange equals to (exchangeId + 1)
        defaultSettlementBatchShouldNotBeFound("exchangeId.equals=" + (exchangeId + 1));
    }

    private void defaultSettlementBatchFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultSettlementBatchShouldBeFound(shouldBeFound);
        defaultSettlementBatchShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSettlementBatchShouldBeFound(String filter) throws Exception {
        restSettlementBatchMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(settlementBatch.getId().intValue())))
            .andExpect(jsonPath("$.[*].refDate").value(hasItem(DEFAULT_REF_DATE.toString())))
            .andExpect(jsonPath("$.[*].kind").value(hasItem(DEFAULT_KIND.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].remarks").value(hasItem(DEFAULT_REMARKS)));

        // Check, that the count call also returns 1
        restSettlementBatchMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSettlementBatchShouldNotBeFound(String filter) throws Exception {
        restSettlementBatchMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSettlementBatchMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSettlementBatch() throws Exception {
        // Get the settlementBatch
        restSettlementBatchMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSettlementBatch() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the settlementBatch
        SettlementBatch updatedSettlementBatch = settlementBatchRepository.findById(settlementBatch.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSettlementBatch are not directly saved in db
        em.detach(updatedSettlementBatch);
        updatedSettlementBatch.refDate(UPDATED_REF_DATE).kind(UPDATED_KIND).status(UPDATED_STATUS).remarks(UPDATED_REMARKS);
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(updatedSettlementBatch);

        restSettlementBatchMockMvc
            .perform(
                put(ENTITY_API_URL_ID, settlementBatchDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(settlementBatchDTO))
            )
            .andExpect(status().isOk());

        // Validate the SettlementBatch in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSettlementBatchToMatchAllProperties(updatedSettlementBatch);
    }

    @Test
    @Transactional
    void putNonExistingSettlementBatch() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        settlementBatch.setId(longCount.incrementAndGet());

        // Create the SettlementBatch
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSettlementBatchMockMvc
            .perform(
                put(ENTITY_API_URL_ID, settlementBatchDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(settlementBatchDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettlementBatch in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSettlementBatch() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        settlementBatch.setId(longCount.incrementAndGet());

        // Create the SettlementBatch
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettlementBatchMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(settlementBatchDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettlementBatch in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSettlementBatch() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        settlementBatch.setId(longCount.incrementAndGet());

        // Create the SettlementBatch
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettlementBatchMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(settlementBatchDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SettlementBatch in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSettlementBatchWithPatch() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the settlementBatch using partial update
        SettlementBatch partialUpdatedSettlementBatch = new SettlementBatch();
        partialUpdatedSettlementBatch.setId(settlementBatch.getId());

        partialUpdatedSettlementBatch.kind(UPDATED_KIND).remarks(UPDATED_REMARKS);

        restSettlementBatchMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSettlementBatch.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSettlementBatch))
            )
            .andExpect(status().isOk());

        // Validate the SettlementBatch in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSettlementBatchUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSettlementBatch, settlementBatch),
            getPersistedSettlementBatch(settlementBatch)
        );
    }

    @Test
    @Transactional
    void fullUpdateSettlementBatchWithPatch() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the settlementBatch using partial update
        SettlementBatch partialUpdatedSettlementBatch = new SettlementBatch();
        partialUpdatedSettlementBatch.setId(settlementBatch.getId());

        partialUpdatedSettlementBatch.refDate(UPDATED_REF_DATE).kind(UPDATED_KIND).status(UPDATED_STATUS).remarks(UPDATED_REMARKS);

        restSettlementBatchMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSettlementBatch.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSettlementBatch))
            )
            .andExpect(status().isOk());

        // Validate the SettlementBatch in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSettlementBatchUpdatableFieldsEquals(
            partialUpdatedSettlementBatch,
            getPersistedSettlementBatch(partialUpdatedSettlementBatch)
        );
    }

    @Test
    @Transactional
    void patchNonExistingSettlementBatch() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        settlementBatch.setId(longCount.incrementAndGet());

        // Create the SettlementBatch
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSettlementBatchMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, settlementBatchDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(settlementBatchDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettlementBatch in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSettlementBatch() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        settlementBatch.setId(longCount.incrementAndGet());

        // Create the SettlementBatch
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettlementBatchMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(settlementBatchDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SettlementBatch in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSettlementBatch() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        settlementBatch.setId(longCount.incrementAndGet());

        // Create the SettlementBatch
        SettlementBatchDTO settlementBatchDTO = settlementBatchMapper.toDto(settlementBatch);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSettlementBatchMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(settlementBatchDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SettlementBatch in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSettlementBatch() throws Exception {
        // Initialize the database
        insertedSettlementBatch = settlementBatchRepository.saveAndFlush(settlementBatch);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the settlementBatch
        restSettlementBatchMockMvc
            .perform(delete(ENTITY_API_URL_ID, settlementBatch.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return settlementBatchRepository.count();
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

    protected SettlementBatch getPersistedSettlementBatch(SettlementBatch settlementBatch) {
        return settlementBatchRepository.findById(settlementBatch.getId()).orElseThrow();
    }

    protected void assertPersistedSettlementBatchToMatchAllProperties(SettlementBatch expectedSettlementBatch) {
        assertSettlementBatchAllPropertiesEquals(expectedSettlementBatch, getPersistedSettlementBatch(expectedSettlementBatch));
    }

    protected void assertPersistedSettlementBatchToMatchUpdatableProperties(SettlementBatch expectedSettlementBatch) {
        assertSettlementBatchAllUpdatablePropertiesEquals(expectedSettlementBatch, getPersistedSettlementBatch(expectedSettlementBatch));
    }
}
