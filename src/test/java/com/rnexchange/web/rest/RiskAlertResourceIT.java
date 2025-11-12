package com.rnexchange.web.rest;

import static com.rnexchange.domain.RiskAlertAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.RiskAlert;
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.enumeration.AlertType;
import com.rnexchange.repository.RiskAlertRepository;
import com.rnexchange.service.RiskAlertService;
import com.rnexchange.service.dto.RiskAlertDTO;
import com.rnexchange.service.mapper.RiskAlertMapper;
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
 * Integration tests for the {@link RiskAlertResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class RiskAlertResourceIT {

    private static final AlertType DEFAULT_ALERT_TYPE = AlertType.MARGIN_BREACH;
    private static final AlertType UPDATED_ALERT_TYPE = AlertType.AUTO_SQOFF;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/risk-alerts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RiskAlertRepository riskAlertRepository;

    @Mock
    private RiskAlertRepository riskAlertRepositoryMock;

    @Autowired
    private RiskAlertMapper riskAlertMapper;

    @Mock
    private RiskAlertService riskAlertServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRiskAlertMockMvc;

    private RiskAlert riskAlert;

    private RiskAlert insertedRiskAlert;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RiskAlert createEntity() {
        return new RiskAlert().alertType(DEFAULT_ALERT_TYPE).description(DEFAULT_DESCRIPTION).createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RiskAlert createUpdatedEntity() {
        return new RiskAlert().alertType(UPDATED_ALERT_TYPE).description(UPDATED_DESCRIPTION).createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    void initTest() {
        riskAlert = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedRiskAlert != null) {
            riskAlertRepository.delete(insertedRiskAlert);
            insertedRiskAlert = null;
        }
    }

    @Test
    @Transactional
    void createRiskAlert() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the RiskAlert
        RiskAlertDTO riskAlertDTO = riskAlertMapper.toDto(riskAlert);
        var returnedRiskAlertDTO = om.readValue(
            restRiskAlertMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(riskAlertDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            RiskAlertDTO.class
        );

        // Validate the RiskAlert in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedRiskAlert = riskAlertMapper.toEntity(returnedRiskAlertDTO);
        assertRiskAlertUpdatableFieldsEquals(returnedRiskAlert, getPersistedRiskAlert(returnedRiskAlert));

        insertedRiskAlert = returnedRiskAlert;
    }

    @Test
    @Transactional
    void createRiskAlertWithExistingId() throws Exception {
        // Create the RiskAlert with an existing ID
        riskAlert.setId(1L);
        RiskAlertDTO riskAlertDTO = riskAlertMapper.toDto(riskAlert);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRiskAlertMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(riskAlertDTO)))
            .andExpect(status().isBadRequest());

        // Validate the RiskAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkAlertTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        riskAlert.setAlertType(null);

        // Create the RiskAlert, which fails.
        RiskAlertDTO riskAlertDTO = riskAlertMapper.toDto(riskAlert);

        restRiskAlertMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(riskAlertDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRiskAlerts() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList
        restRiskAlertMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(riskAlert.getId().intValue())))
            .andExpect(jsonPath("$.[*].alertType").value(hasItem(DEFAULT_ALERT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRiskAlertsWithEagerRelationshipsIsEnabled() throws Exception {
        when(riskAlertServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRiskAlertMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(riskAlertServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRiskAlertsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(riskAlertServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRiskAlertMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(riskAlertRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getRiskAlert() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get the riskAlert
        restRiskAlertMockMvc
            .perform(get(ENTITY_API_URL_ID, riskAlert.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(riskAlert.getId().intValue()))
            .andExpect(jsonPath("$.alertType").value(DEFAULT_ALERT_TYPE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getRiskAlertsByIdFiltering() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        Long id = riskAlert.getId();

        defaultRiskAlertFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultRiskAlertFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultRiskAlertFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllRiskAlertsByAlertTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where alertType equals to
        defaultRiskAlertFiltering("alertType.equals=" + DEFAULT_ALERT_TYPE, "alertType.equals=" + UPDATED_ALERT_TYPE);
    }

    @Test
    @Transactional
    void getAllRiskAlertsByAlertTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where alertType in
        defaultRiskAlertFiltering("alertType.in=" + DEFAULT_ALERT_TYPE + "," + UPDATED_ALERT_TYPE, "alertType.in=" + UPDATED_ALERT_TYPE);
    }

    @Test
    @Transactional
    void getAllRiskAlertsByAlertTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where alertType is not null
        defaultRiskAlertFiltering("alertType.specified=true", "alertType.specified=false");
    }

    @Test
    @Transactional
    void getAllRiskAlertsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where description equals to
        defaultRiskAlertFiltering("description.equals=" + DEFAULT_DESCRIPTION, "description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllRiskAlertsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where description in
        defaultRiskAlertFiltering(
            "description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION,
            "description.in=" + UPDATED_DESCRIPTION
        );
    }

    @Test
    @Transactional
    void getAllRiskAlertsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where description is not null
        defaultRiskAlertFiltering("description.specified=true", "description.specified=false");
    }

    @Test
    @Transactional
    void getAllRiskAlertsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where description contains
        defaultRiskAlertFiltering("description.contains=" + DEFAULT_DESCRIPTION, "description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllRiskAlertsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where description does not contain
        defaultRiskAlertFiltering("description.doesNotContain=" + UPDATED_DESCRIPTION, "description.doesNotContain=" + DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllRiskAlertsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where createdAt equals to
        defaultRiskAlertFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllRiskAlertsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where createdAt in
        defaultRiskAlertFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllRiskAlertsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        // Get all the riskAlertList where createdAt is not null
        defaultRiskAlertFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllRiskAlertsByTradingAccountIsEqualToSomething() throws Exception {
        TradingAccount tradingAccount;
        if (TestUtil.findAll(em, TradingAccount.class).isEmpty()) {
            riskAlertRepository.saveAndFlush(riskAlert);
            tradingAccount = TradingAccountResourceIT.createEntity();
        } else {
            tradingAccount = TestUtil.findAll(em, TradingAccount.class).get(0);
        }
        em.persist(tradingAccount);
        em.flush();
        riskAlert.setTradingAccount(tradingAccount);
        riskAlertRepository.saveAndFlush(riskAlert);
        Long tradingAccountId = tradingAccount.getId();
        // Get all the riskAlertList where tradingAccount equals to tradingAccountId
        defaultRiskAlertShouldBeFound("tradingAccountId.equals=" + tradingAccountId);

        // Get all the riskAlertList where tradingAccount equals to (tradingAccountId + 1)
        defaultRiskAlertShouldNotBeFound("tradingAccountId.equals=" + (tradingAccountId + 1));
    }

    @Test
    @Transactional
    void getAllRiskAlertsByTraderIsEqualToSomething() throws Exception {
        TraderProfile trader;
        if (TestUtil.findAll(em, TraderProfile.class).isEmpty()) {
            riskAlertRepository.saveAndFlush(riskAlert);
            trader = TraderProfileResourceIT.createEntity();
        } else {
            trader = TestUtil.findAll(em, TraderProfile.class).get(0);
        }
        em.persist(trader);
        em.flush();
        riskAlert.setTrader(trader);
        riskAlertRepository.saveAndFlush(riskAlert);
        Long traderId = trader.getId();
        // Get all the riskAlertList where trader equals to traderId
        defaultRiskAlertShouldBeFound("traderId.equals=" + traderId);

        // Get all the riskAlertList where trader equals to (traderId + 1)
        defaultRiskAlertShouldNotBeFound("traderId.equals=" + (traderId + 1));
    }

    private void defaultRiskAlertFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultRiskAlertShouldBeFound(shouldBeFound);
        defaultRiskAlertShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRiskAlertShouldBeFound(String filter) throws Exception {
        restRiskAlertMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(riskAlert.getId().intValue())))
            .andExpect(jsonPath("$.[*].alertType").value(hasItem(DEFAULT_ALERT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));

        // Check, that the count call also returns 1
        restRiskAlertMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRiskAlertShouldNotBeFound(String filter) throws Exception {
        restRiskAlertMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRiskAlertMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingRiskAlert() throws Exception {
        // Get the riskAlert
        restRiskAlertMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingRiskAlert() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the riskAlert
        RiskAlert updatedRiskAlert = riskAlertRepository.findById(riskAlert.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedRiskAlert are not directly saved in db
        em.detach(updatedRiskAlert);
        updatedRiskAlert.alertType(UPDATED_ALERT_TYPE).description(UPDATED_DESCRIPTION).createdAt(UPDATED_CREATED_AT);
        RiskAlertDTO riskAlertDTO = riskAlertMapper.toDto(updatedRiskAlert);

        restRiskAlertMockMvc
            .perform(
                put(ENTITY_API_URL_ID, riskAlertDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(riskAlertDTO))
            )
            .andExpect(status().isOk());

        // Validate the RiskAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedRiskAlertToMatchAllProperties(updatedRiskAlert);
    }

    @Test
    @Transactional
    void putNonExistingRiskAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        riskAlert.setId(longCount.incrementAndGet());

        // Create the RiskAlert
        RiskAlertDTO riskAlertDTO = riskAlertMapper.toDto(riskAlert);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRiskAlertMockMvc
            .perform(
                put(ENTITY_API_URL_ID, riskAlertDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(riskAlertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RiskAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRiskAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        riskAlert.setId(longCount.incrementAndGet());

        // Create the RiskAlert
        RiskAlertDTO riskAlertDTO = riskAlertMapper.toDto(riskAlert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRiskAlertMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(riskAlertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RiskAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRiskAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        riskAlert.setId(longCount.incrementAndGet());

        // Create the RiskAlert
        RiskAlertDTO riskAlertDTO = riskAlertMapper.toDto(riskAlert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRiskAlertMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(riskAlertDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the RiskAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRiskAlertWithPatch() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the riskAlert using partial update
        RiskAlert partialUpdatedRiskAlert = new RiskAlert();
        partialUpdatedRiskAlert.setId(riskAlert.getId());

        partialUpdatedRiskAlert.description(UPDATED_DESCRIPTION).createdAt(UPDATED_CREATED_AT);

        restRiskAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRiskAlert.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRiskAlert))
            )
            .andExpect(status().isOk());

        // Validate the RiskAlert in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRiskAlertUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedRiskAlert, riskAlert),
            getPersistedRiskAlert(riskAlert)
        );
    }

    @Test
    @Transactional
    void fullUpdateRiskAlertWithPatch() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the riskAlert using partial update
        RiskAlert partialUpdatedRiskAlert = new RiskAlert();
        partialUpdatedRiskAlert.setId(riskAlert.getId());

        partialUpdatedRiskAlert.alertType(UPDATED_ALERT_TYPE).description(UPDATED_DESCRIPTION).createdAt(UPDATED_CREATED_AT);

        restRiskAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRiskAlert.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRiskAlert))
            )
            .andExpect(status().isOk());

        // Validate the RiskAlert in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRiskAlertUpdatableFieldsEquals(partialUpdatedRiskAlert, getPersistedRiskAlert(partialUpdatedRiskAlert));
    }

    @Test
    @Transactional
    void patchNonExistingRiskAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        riskAlert.setId(longCount.incrementAndGet());

        // Create the RiskAlert
        RiskAlertDTO riskAlertDTO = riskAlertMapper.toDto(riskAlert);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRiskAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, riskAlertDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(riskAlertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RiskAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRiskAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        riskAlert.setId(longCount.incrementAndGet());

        // Create the RiskAlert
        RiskAlertDTO riskAlertDTO = riskAlertMapper.toDto(riskAlert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRiskAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(riskAlertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RiskAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRiskAlert() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        riskAlert.setId(longCount.incrementAndGet());

        // Create the RiskAlert
        RiskAlertDTO riskAlertDTO = riskAlertMapper.toDto(riskAlert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRiskAlertMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(riskAlertDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the RiskAlert in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRiskAlert() throws Exception {
        // Initialize the database
        insertedRiskAlert = riskAlertRepository.saveAndFlush(riskAlert);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the riskAlert
        restRiskAlertMockMvc
            .perform(delete(ENTITY_API_URL_ID, riskAlert.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return riskAlertRepository.count();
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

    protected RiskAlert getPersistedRiskAlert(RiskAlert riskAlert) {
        return riskAlertRepository.findById(riskAlert.getId()).orElseThrow();
    }

    protected void assertPersistedRiskAlertToMatchAllProperties(RiskAlert expectedRiskAlert) {
        assertRiskAlertAllPropertiesEquals(expectedRiskAlert, getPersistedRiskAlert(expectedRiskAlert));
    }

    protected void assertPersistedRiskAlertToMatchUpdatableProperties(RiskAlert expectedRiskAlert) {
        assertRiskAlertAllUpdatablePropertiesEquals(expectedRiskAlert, getPersistedRiskAlert(expectedRiskAlert));
    }
}
