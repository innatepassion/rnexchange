package com.rnexchange.web.rest;

import static com.rnexchange.domain.MarginRuleAsserts.*;
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
import com.rnexchange.domain.MarginRule;
import com.rnexchange.repository.MarginRuleRepository;
import com.rnexchange.service.MarginRuleService;
import com.rnexchange.service.dto.MarginRuleDTO;
import com.rnexchange.service.mapper.MarginRuleMapper;
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
 * Integration tests for the {@link MarginRuleResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class MarginRuleResourceIT {

    private static final String DEFAULT_SCOPE = "AAAAAAAAAA";
    private static final String UPDATED_SCOPE = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_INITIAL_PCT = new BigDecimal(1);
    private static final BigDecimal UPDATED_INITIAL_PCT = new BigDecimal(2);
    private static final BigDecimal SMALLER_INITIAL_PCT = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_MAINT_PCT = new BigDecimal(1);
    private static final BigDecimal UPDATED_MAINT_PCT = new BigDecimal(2);
    private static final BigDecimal SMALLER_MAINT_PCT = new BigDecimal(1 - 1);

    private static final String DEFAULT_SPAN_JSON = "AAAAAAAAAA";
    private static final String UPDATED_SPAN_JSON = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/margin-rules";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MarginRuleRepository marginRuleRepository;

    @Mock
    private MarginRuleRepository marginRuleRepositoryMock;

    @Autowired
    private MarginRuleMapper marginRuleMapper;

    @Mock
    private MarginRuleService marginRuleServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMarginRuleMockMvc;

    private MarginRule marginRule;

    private MarginRule insertedMarginRule;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MarginRule createEntity() {
        return new MarginRule()
            .scope(DEFAULT_SCOPE)
            .initialPct(DEFAULT_INITIAL_PCT)
            .maintPct(DEFAULT_MAINT_PCT)
            .spanJson(DEFAULT_SPAN_JSON);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MarginRule createUpdatedEntity() {
        return new MarginRule()
            .scope(UPDATED_SCOPE)
            .initialPct(UPDATED_INITIAL_PCT)
            .maintPct(UPDATED_MAINT_PCT)
            .spanJson(UPDATED_SPAN_JSON);
    }

    @BeforeEach
    void initTest() {
        marginRule = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMarginRule != null) {
            marginRuleRepository.delete(insertedMarginRule);
            insertedMarginRule = null;
        }
    }

    @Test
    @Transactional
    void createMarginRule() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MarginRule
        MarginRuleDTO marginRuleDTO = marginRuleMapper.toDto(marginRule);
        var returnedMarginRuleDTO = om.readValue(
            restMarginRuleMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marginRuleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MarginRuleDTO.class
        );

        // Validate the MarginRule in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMarginRule = marginRuleMapper.toEntity(returnedMarginRuleDTO);
        assertMarginRuleUpdatableFieldsEquals(returnedMarginRule, getPersistedMarginRule(returnedMarginRule));

        insertedMarginRule = returnedMarginRule;
    }

    @Test
    @Transactional
    void createMarginRuleWithExistingId() throws Exception {
        // Create the MarginRule with an existing ID
        marginRule.setId(1L);
        MarginRuleDTO marginRuleDTO = marginRuleMapper.toDto(marginRule);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMarginRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marginRuleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MarginRule in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkScopeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        marginRule.setScope(null);

        // Create the MarginRule, which fails.
        MarginRuleDTO marginRuleDTO = marginRuleMapper.toDto(marginRule);

        restMarginRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marginRuleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMarginRules() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList
        restMarginRuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(marginRule.getId().intValue())))
            .andExpect(jsonPath("$.[*].scope").value(hasItem(DEFAULT_SCOPE)))
            .andExpect(jsonPath("$.[*].initialPct").value(hasItem(sameNumber(DEFAULT_INITIAL_PCT))))
            .andExpect(jsonPath("$.[*].maintPct").value(hasItem(sameNumber(DEFAULT_MAINT_PCT))))
            .andExpect(jsonPath("$.[*].spanJson").value(hasItem(DEFAULT_SPAN_JSON)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMarginRulesWithEagerRelationshipsIsEnabled() throws Exception {
        when(marginRuleServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMarginRuleMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(marginRuleServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMarginRulesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(marginRuleServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMarginRuleMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(marginRuleRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getMarginRule() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get the marginRule
        restMarginRuleMockMvc
            .perform(get(ENTITY_API_URL_ID, marginRule.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(marginRule.getId().intValue()))
            .andExpect(jsonPath("$.scope").value(DEFAULT_SCOPE))
            .andExpect(jsonPath("$.initialPct").value(sameNumber(DEFAULT_INITIAL_PCT)))
            .andExpect(jsonPath("$.maintPct").value(sameNumber(DEFAULT_MAINT_PCT)))
            .andExpect(jsonPath("$.spanJson").value(DEFAULT_SPAN_JSON));
    }

    @Test
    @Transactional
    void getMarginRulesByIdFiltering() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        Long id = marginRule.getId();

        defaultMarginRuleFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultMarginRuleFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultMarginRuleFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllMarginRulesByScopeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where scope equals to
        defaultMarginRuleFiltering("scope.equals=" + DEFAULT_SCOPE, "scope.equals=" + UPDATED_SCOPE);
    }

    @Test
    @Transactional
    void getAllMarginRulesByScopeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where scope in
        defaultMarginRuleFiltering("scope.in=" + DEFAULT_SCOPE + "," + UPDATED_SCOPE, "scope.in=" + UPDATED_SCOPE);
    }

    @Test
    @Transactional
    void getAllMarginRulesByScopeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where scope is not null
        defaultMarginRuleFiltering("scope.specified=true", "scope.specified=false");
    }

    @Test
    @Transactional
    void getAllMarginRulesByScopeContainsSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where scope contains
        defaultMarginRuleFiltering("scope.contains=" + DEFAULT_SCOPE, "scope.contains=" + UPDATED_SCOPE);
    }

    @Test
    @Transactional
    void getAllMarginRulesByScopeNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where scope does not contain
        defaultMarginRuleFiltering("scope.doesNotContain=" + UPDATED_SCOPE, "scope.doesNotContain=" + DEFAULT_SCOPE);
    }

    @Test
    @Transactional
    void getAllMarginRulesByInitialPctIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where initialPct equals to
        defaultMarginRuleFiltering("initialPct.equals=" + DEFAULT_INITIAL_PCT, "initialPct.equals=" + UPDATED_INITIAL_PCT);
    }

    @Test
    @Transactional
    void getAllMarginRulesByInitialPctIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where initialPct in
        defaultMarginRuleFiltering(
            "initialPct.in=" + DEFAULT_INITIAL_PCT + "," + UPDATED_INITIAL_PCT,
            "initialPct.in=" + UPDATED_INITIAL_PCT
        );
    }

    @Test
    @Transactional
    void getAllMarginRulesByInitialPctIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where initialPct is not null
        defaultMarginRuleFiltering("initialPct.specified=true", "initialPct.specified=false");
    }

    @Test
    @Transactional
    void getAllMarginRulesByInitialPctIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where initialPct is greater than or equal to
        defaultMarginRuleFiltering(
            "initialPct.greaterThanOrEqual=" + DEFAULT_INITIAL_PCT,
            "initialPct.greaterThanOrEqual=" + UPDATED_INITIAL_PCT
        );
    }

    @Test
    @Transactional
    void getAllMarginRulesByInitialPctIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where initialPct is less than or equal to
        defaultMarginRuleFiltering(
            "initialPct.lessThanOrEqual=" + DEFAULT_INITIAL_PCT,
            "initialPct.lessThanOrEqual=" + SMALLER_INITIAL_PCT
        );
    }

    @Test
    @Transactional
    void getAllMarginRulesByInitialPctIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where initialPct is less than
        defaultMarginRuleFiltering("initialPct.lessThan=" + UPDATED_INITIAL_PCT, "initialPct.lessThan=" + DEFAULT_INITIAL_PCT);
    }

    @Test
    @Transactional
    void getAllMarginRulesByInitialPctIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where initialPct is greater than
        defaultMarginRuleFiltering("initialPct.greaterThan=" + SMALLER_INITIAL_PCT, "initialPct.greaterThan=" + DEFAULT_INITIAL_PCT);
    }

    @Test
    @Transactional
    void getAllMarginRulesByMaintPctIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where maintPct equals to
        defaultMarginRuleFiltering("maintPct.equals=" + DEFAULT_MAINT_PCT, "maintPct.equals=" + UPDATED_MAINT_PCT);
    }

    @Test
    @Transactional
    void getAllMarginRulesByMaintPctIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where maintPct in
        defaultMarginRuleFiltering("maintPct.in=" + DEFAULT_MAINT_PCT + "," + UPDATED_MAINT_PCT, "maintPct.in=" + UPDATED_MAINT_PCT);
    }

    @Test
    @Transactional
    void getAllMarginRulesByMaintPctIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where maintPct is not null
        defaultMarginRuleFiltering("maintPct.specified=true", "maintPct.specified=false");
    }

    @Test
    @Transactional
    void getAllMarginRulesByMaintPctIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where maintPct is greater than or equal to
        defaultMarginRuleFiltering("maintPct.greaterThanOrEqual=" + DEFAULT_MAINT_PCT, "maintPct.greaterThanOrEqual=" + UPDATED_MAINT_PCT);
    }

    @Test
    @Transactional
    void getAllMarginRulesByMaintPctIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where maintPct is less than or equal to
        defaultMarginRuleFiltering("maintPct.lessThanOrEqual=" + DEFAULT_MAINT_PCT, "maintPct.lessThanOrEqual=" + SMALLER_MAINT_PCT);
    }

    @Test
    @Transactional
    void getAllMarginRulesByMaintPctIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where maintPct is less than
        defaultMarginRuleFiltering("maintPct.lessThan=" + UPDATED_MAINT_PCT, "maintPct.lessThan=" + DEFAULT_MAINT_PCT);
    }

    @Test
    @Transactional
    void getAllMarginRulesByMaintPctIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        // Get all the marginRuleList where maintPct is greater than
        defaultMarginRuleFiltering("maintPct.greaterThan=" + SMALLER_MAINT_PCT, "maintPct.greaterThan=" + DEFAULT_MAINT_PCT);
    }

    @Test
    @Transactional
    void getAllMarginRulesByExchangeIsEqualToSomething() throws Exception {
        Exchange exchange;
        if (TestUtil.findAll(em, Exchange.class).isEmpty()) {
            marginRuleRepository.saveAndFlush(marginRule);
            exchange = ExchangeResourceIT.createEntity();
        } else {
            exchange = TestUtil.findAll(em, Exchange.class).get(0);
        }
        em.persist(exchange);
        em.flush();
        marginRule.setExchange(exchange);
        marginRuleRepository.saveAndFlush(marginRule);
        Long exchangeId = exchange.getId();
        // Get all the marginRuleList where exchange equals to exchangeId
        defaultMarginRuleShouldBeFound("exchangeId.equals=" + exchangeId);

        // Get all the marginRuleList where exchange equals to (exchangeId + 1)
        defaultMarginRuleShouldNotBeFound("exchangeId.equals=" + (exchangeId + 1));
    }

    private void defaultMarginRuleFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultMarginRuleShouldBeFound(shouldBeFound);
        defaultMarginRuleShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMarginRuleShouldBeFound(String filter) throws Exception {
        restMarginRuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(marginRule.getId().intValue())))
            .andExpect(jsonPath("$.[*].scope").value(hasItem(DEFAULT_SCOPE)))
            .andExpect(jsonPath("$.[*].initialPct").value(hasItem(sameNumber(DEFAULT_INITIAL_PCT))))
            .andExpect(jsonPath("$.[*].maintPct").value(hasItem(sameNumber(DEFAULT_MAINT_PCT))))
            .andExpect(jsonPath("$.[*].spanJson").value(hasItem(DEFAULT_SPAN_JSON)));

        // Check, that the count call also returns 1
        restMarginRuleMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMarginRuleShouldNotBeFound(String filter) throws Exception {
        restMarginRuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMarginRuleMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMarginRule() throws Exception {
        // Get the marginRule
        restMarginRuleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMarginRule() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the marginRule
        MarginRule updatedMarginRule = marginRuleRepository.findById(marginRule.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMarginRule are not directly saved in db
        em.detach(updatedMarginRule);
        updatedMarginRule.scope(UPDATED_SCOPE).initialPct(UPDATED_INITIAL_PCT).maintPct(UPDATED_MAINT_PCT).spanJson(UPDATED_SPAN_JSON);
        MarginRuleDTO marginRuleDTO = marginRuleMapper.toDto(updatedMarginRule);

        restMarginRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, marginRuleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(marginRuleDTO))
            )
            .andExpect(status().isOk());

        // Validate the MarginRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMarginRuleToMatchAllProperties(updatedMarginRule);
    }

    @Test
    @Transactional
    void putNonExistingMarginRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marginRule.setId(longCount.incrementAndGet());

        // Create the MarginRule
        MarginRuleDTO marginRuleDTO = marginRuleMapper.toDto(marginRule);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMarginRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, marginRuleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(marginRuleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MarginRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMarginRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marginRule.setId(longCount.incrementAndGet());

        // Create the MarginRule
        MarginRuleDTO marginRuleDTO = marginRuleMapper.toDto(marginRule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarginRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(marginRuleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MarginRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMarginRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marginRule.setId(longCount.incrementAndGet());

        // Create the MarginRule
        MarginRuleDTO marginRuleDTO = marginRuleMapper.toDto(marginRule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarginRuleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(marginRuleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MarginRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMarginRuleWithPatch() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the marginRule using partial update
        MarginRule partialUpdatedMarginRule = new MarginRule();
        partialUpdatedMarginRule.setId(marginRule.getId());

        partialUpdatedMarginRule.scope(UPDATED_SCOPE).initialPct(UPDATED_INITIAL_PCT);

        restMarginRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMarginRule.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMarginRule))
            )
            .andExpect(status().isOk());

        // Validate the MarginRule in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMarginRuleUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMarginRule, marginRule),
            getPersistedMarginRule(marginRule)
        );
    }

    @Test
    @Transactional
    void fullUpdateMarginRuleWithPatch() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the marginRule using partial update
        MarginRule partialUpdatedMarginRule = new MarginRule();
        partialUpdatedMarginRule.setId(marginRule.getId());

        partialUpdatedMarginRule
            .scope(UPDATED_SCOPE)
            .initialPct(UPDATED_INITIAL_PCT)
            .maintPct(UPDATED_MAINT_PCT)
            .spanJson(UPDATED_SPAN_JSON);

        restMarginRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMarginRule.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMarginRule))
            )
            .andExpect(status().isOk());

        // Validate the MarginRule in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMarginRuleUpdatableFieldsEquals(partialUpdatedMarginRule, getPersistedMarginRule(partialUpdatedMarginRule));
    }

    @Test
    @Transactional
    void patchNonExistingMarginRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marginRule.setId(longCount.incrementAndGet());

        // Create the MarginRule
        MarginRuleDTO marginRuleDTO = marginRuleMapper.toDto(marginRule);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMarginRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, marginRuleDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(marginRuleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MarginRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMarginRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marginRule.setId(longCount.incrementAndGet());

        // Create the MarginRule
        MarginRuleDTO marginRuleDTO = marginRuleMapper.toDto(marginRule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarginRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(marginRuleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MarginRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMarginRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        marginRule.setId(longCount.incrementAndGet());

        // Create the MarginRule
        MarginRuleDTO marginRuleDTO = marginRuleMapper.toDto(marginRule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarginRuleMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(marginRuleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MarginRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMarginRule() throws Exception {
        // Initialize the database
        insertedMarginRule = marginRuleRepository.saveAndFlush(marginRule);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the marginRule
        restMarginRuleMockMvc
            .perform(delete(ENTITY_API_URL_ID, marginRule.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return marginRuleRepository.count();
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

    protected MarginRule getPersistedMarginRule(MarginRule marginRule) {
        return marginRuleRepository.findById(marginRule.getId()).orElseThrow();
    }

    protected void assertPersistedMarginRuleToMatchAllProperties(MarginRule expectedMarginRule) {
        assertMarginRuleAllPropertiesEquals(expectedMarginRule, getPersistedMarginRule(expectedMarginRule));
    }

    protected void assertPersistedMarginRuleToMatchUpdatableProperties(MarginRule expectedMarginRule) {
        assertMarginRuleAllUpdatablePropertiesEquals(expectedMarginRule, getPersistedMarginRule(expectedMarginRule));
    }
}
