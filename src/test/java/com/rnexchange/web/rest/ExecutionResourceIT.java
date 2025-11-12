package com.rnexchange.web.rest;

import static com.rnexchange.domain.ExecutionAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static com.rnexchange.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Execution;
import com.rnexchange.domain.Order;
import com.rnexchange.repository.ExecutionRepository;
import com.rnexchange.service.dto.ExecutionDTO;
import com.rnexchange.service.mapper.ExecutionMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ExecutionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ExecutionResourceIT {

    private static final Instant DEFAULT_EXEC_TS = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_EXEC_TS = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final BigDecimal DEFAULT_PX = new BigDecimal(1);
    private static final BigDecimal UPDATED_PX = new BigDecimal(2);
    private static final BigDecimal SMALLER_PX = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_QTY = new BigDecimal(1);
    private static final BigDecimal UPDATED_QTY = new BigDecimal(2);
    private static final BigDecimal SMALLER_QTY = new BigDecimal(1 - 1);

    private static final String DEFAULT_LIQUIDITY = "AAAAAAAAAA";
    private static final String UPDATED_LIQUIDITY = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_FEE = new BigDecimal(1);
    private static final BigDecimal UPDATED_FEE = new BigDecimal(2);
    private static final BigDecimal SMALLER_FEE = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/executions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private ExecutionMapper executionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restExecutionMockMvc;

    private Execution execution;

    private Execution insertedExecution;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Execution createEntity() {
        return new Execution().execTs(DEFAULT_EXEC_TS).px(DEFAULT_PX).qty(DEFAULT_QTY).liquidity(DEFAULT_LIQUIDITY).fee(DEFAULT_FEE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Execution createUpdatedEntity() {
        return new Execution().execTs(UPDATED_EXEC_TS).px(UPDATED_PX).qty(UPDATED_QTY).liquidity(UPDATED_LIQUIDITY).fee(UPDATED_FEE);
    }

    @BeforeEach
    void initTest() {
        execution = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedExecution != null) {
            executionRepository.delete(insertedExecution);
            insertedExecution = null;
        }
    }

    @Test
    @Transactional
    void createExecution() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Execution
        ExecutionDTO executionDTO = executionMapper.toDto(execution);
        var returnedExecutionDTO = om.readValue(
            restExecutionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(executionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ExecutionDTO.class
        );

        // Validate the Execution in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedExecution = executionMapper.toEntity(returnedExecutionDTO);
        assertExecutionUpdatableFieldsEquals(returnedExecution, getPersistedExecution(returnedExecution));

        insertedExecution = returnedExecution;
    }

    @Test
    @Transactional
    void createExecutionWithExistingId() throws Exception {
        // Create the Execution with an existing ID
        execution.setId(1L);
        ExecutionDTO executionDTO = executionMapper.toDto(execution);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restExecutionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(executionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Execution in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkExecTsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        execution.setExecTs(null);

        // Create the Execution, which fails.
        ExecutionDTO executionDTO = executionMapper.toDto(execution);

        restExecutionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(executionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPxIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        execution.setPx(null);

        // Create the Execution, which fails.
        ExecutionDTO executionDTO = executionMapper.toDto(execution);

        restExecutionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(executionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkQtyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        execution.setQty(null);

        // Create the Execution, which fails.
        ExecutionDTO executionDTO = executionMapper.toDto(execution);

        restExecutionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(executionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllExecutions() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList
        restExecutionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(execution.getId().intValue())))
            .andExpect(jsonPath("$.[*].execTs").value(hasItem(DEFAULT_EXEC_TS.toString())))
            .andExpect(jsonPath("$.[*].px").value(hasItem(sameNumber(DEFAULT_PX))))
            .andExpect(jsonPath("$.[*].qty").value(hasItem(sameNumber(DEFAULT_QTY))))
            .andExpect(jsonPath("$.[*].liquidity").value(hasItem(DEFAULT_LIQUIDITY)))
            .andExpect(jsonPath("$.[*].fee").value(hasItem(sameNumber(DEFAULT_FEE))));
    }

    @Test
    @Transactional
    void getExecution() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get the execution
        restExecutionMockMvc
            .perform(get(ENTITY_API_URL_ID, execution.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(execution.getId().intValue()))
            .andExpect(jsonPath("$.execTs").value(DEFAULT_EXEC_TS.toString()))
            .andExpect(jsonPath("$.px").value(sameNumber(DEFAULT_PX)))
            .andExpect(jsonPath("$.qty").value(sameNumber(DEFAULT_QTY)))
            .andExpect(jsonPath("$.liquidity").value(DEFAULT_LIQUIDITY))
            .andExpect(jsonPath("$.fee").value(sameNumber(DEFAULT_FEE)));
    }

    @Test
    @Transactional
    void getExecutionsByIdFiltering() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        Long id = execution.getId();

        defaultExecutionFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultExecutionFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultExecutionFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllExecutionsByExecTsIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where execTs equals to
        defaultExecutionFiltering("execTs.equals=" + DEFAULT_EXEC_TS, "execTs.equals=" + UPDATED_EXEC_TS);
    }

    @Test
    @Transactional
    void getAllExecutionsByExecTsIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where execTs in
        defaultExecutionFiltering("execTs.in=" + DEFAULT_EXEC_TS + "," + UPDATED_EXEC_TS, "execTs.in=" + UPDATED_EXEC_TS);
    }

    @Test
    @Transactional
    void getAllExecutionsByExecTsIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where execTs is not null
        defaultExecutionFiltering("execTs.specified=true", "execTs.specified=false");
    }

    @Test
    @Transactional
    void getAllExecutionsByPxIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where px equals to
        defaultExecutionFiltering("px.equals=" + DEFAULT_PX, "px.equals=" + UPDATED_PX);
    }

    @Test
    @Transactional
    void getAllExecutionsByPxIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where px in
        defaultExecutionFiltering("px.in=" + DEFAULT_PX + "," + UPDATED_PX, "px.in=" + UPDATED_PX);
    }

    @Test
    @Transactional
    void getAllExecutionsByPxIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where px is not null
        defaultExecutionFiltering("px.specified=true", "px.specified=false");
    }

    @Test
    @Transactional
    void getAllExecutionsByPxIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where px is greater than or equal to
        defaultExecutionFiltering("px.greaterThanOrEqual=" + DEFAULT_PX, "px.greaterThanOrEqual=" + UPDATED_PX);
    }

    @Test
    @Transactional
    void getAllExecutionsByPxIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where px is less than or equal to
        defaultExecutionFiltering("px.lessThanOrEqual=" + DEFAULT_PX, "px.lessThanOrEqual=" + SMALLER_PX);
    }

    @Test
    @Transactional
    void getAllExecutionsByPxIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where px is less than
        defaultExecutionFiltering("px.lessThan=" + UPDATED_PX, "px.lessThan=" + DEFAULT_PX);
    }

    @Test
    @Transactional
    void getAllExecutionsByPxIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where px is greater than
        defaultExecutionFiltering("px.greaterThan=" + SMALLER_PX, "px.greaterThan=" + DEFAULT_PX);
    }

    @Test
    @Transactional
    void getAllExecutionsByQtyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where qty equals to
        defaultExecutionFiltering("qty.equals=" + DEFAULT_QTY, "qty.equals=" + UPDATED_QTY);
    }

    @Test
    @Transactional
    void getAllExecutionsByQtyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where qty in
        defaultExecutionFiltering("qty.in=" + DEFAULT_QTY + "," + UPDATED_QTY, "qty.in=" + UPDATED_QTY);
    }

    @Test
    @Transactional
    void getAllExecutionsByQtyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where qty is not null
        defaultExecutionFiltering("qty.specified=true", "qty.specified=false");
    }

    @Test
    @Transactional
    void getAllExecutionsByQtyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where qty is greater than or equal to
        defaultExecutionFiltering("qty.greaterThanOrEqual=" + DEFAULT_QTY, "qty.greaterThanOrEqual=" + UPDATED_QTY);
    }

    @Test
    @Transactional
    void getAllExecutionsByQtyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where qty is less than or equal to
        defaultExecutionFiltering("qty.lessThanOrEqual=" + DEFAULT_QTY, "qty.lessThanOrEqual=" + SMALLER_QTY);
    }

    @Test
    @Transactional
    void getAllExecutionsByQtyIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where qty is less than
        defaultExecutionFiltering("qty.lessThan=" + UPDATED_QTY, "qty.lessThan=" + DEFAULT_QTY);
    }

    @Test
    @Transactional
    void getAllExecutionsByQtyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where qty is greater than
        defaultExecutionFiltering("qty.greaterThan=" + SMALLER_QTY, "qty.greaterThan=" + DEFAULT_QTY);
    }

    @Test
    @Transactional
    void getAllExecutionsByLiquidityIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where liquidity equals to
        defaultExecutionFiltering("liquidity.equals=" + DEFAULT_LIQUIDITY, "liquidity.equals=" + UPDATED_LIQUIDITY);
    }

    @Test
    @Transactional
    void getAllExecutionsByLiquidityIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where liquidity in
        defaultExecutionFiltering("liquidity.in=" + DEFAULT_LIQUIDITY + "," + UPDATED_LIQUIDITY, "liquidity.in=" + UPDATED_LIQUIDITY);
    }

    @Test
    @Transactional
    void getAllExecutionsByLiquidityIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where liquidity is not null
        defaultExecutionFiltering("liquidity.specified=true", "liquidity.specified=false");
    }

    @Test
    @Transactional
    void getAllExecutionsByLiquidityContainsSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where liquidity contains
        defaultExecutionFiltering("liquidity.contains=" + DEFAULT_LIQUIDITY, "liquidity.contains=" + UPDATED_LIQUIDITY);
    }

    @Test
    @Transactional
    void getAllExecutionsByLiquidityNotContainsSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where liquidity does not contain
        defaultExecutionFiltering("liquidity.doesNotContain=" + UPDATED_LIQUIDITY, "liquidity.doesNotContain=" + DEFAULT_LIQUIDITY);
    }

    @Test
    @Transactional
    void getAllExecutionsByFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where fee equals to
        defaultExecutionFiltering("fee.equals=" + DEFAULT_FEE, "fee.equals=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    void getAllExecutionsByFeeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where fee in
        defaultExecutionFiltering("fee.in=" + DEFAULT_FEE + "," + UPDATED_FEE, "fee.in=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    void getAllExecutionsByFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where fee is not null
        defaultExecutionFiltering("fee.specified=true", "fee.specified=false");
    }

    @Test
    @Transactional
    void getAllExecutionsByFeeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where fee is greater than or equal to
        defaultExecutionFiltering("fee.greaterThanOrEqual=" + DEFAULT_FEE, "fee.greaterThanOrEqual=" + UPDATED_FEE);
    }

    @Test
    @Transactional
    void getAllExecutionsByFeeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where fee is less than or equal to
        defaultExecutionFiltering("fee.lessThanOrEqual=" + DEFAULT_FEE, "fee.lessThanOrEqual=" + SMALLER_FEE);
    }

    @Test
    @Transactional
    void getAllExecutionsByFeeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where fee is less than
        defaultExecutionFiltering("fee.lessThan=" + UPDATED_FEE, "fee.lessThan=" + DEFAULT_FEE);
    }

    @Test
    @Transactional
    void getAllExecutionsByFeeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        // Get all the executionList where fee is greater than
        defaultExecutionFiltering("fee.greaterThan=" + SMALLER_FEE, "fee.greaterThan=" + DEFAULT_FEE);
    }

    @Test
    @Transactional
    void getAllExecutionsByOrderIsEqualToSomething() throws Exception {
        Order order;
        if (TestUtil.findAll(em, Order.class).isEmpty()) {
            executionRepository.saveAndFlush(execution);
            order = OrderResourceIT.createEntity();
        } else {
            order = TestUtil.findAll(em, Order.class).get(0);
        }
        em.persist(order);
        em.flush();
        execution.setOrder(order);
        executionRepository.saveAndFlush(execution);
        Long orderId = order.getId();
        // Get all the executionList where order equals to orderId
        defaultExecutionShouldBeFound("orderId.equals=" + orderId);

        // Get all the executionList where order equals to (orderId + 1)
        defaultExecutionShouldNotBeFound("orderId.equals=" + (orderId + 1));
    }

    private void defaultExecutionFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultExecutionShouldBeFound(shouldBeFound);
        defaultExecutionShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultExecutionShouldBeFound(String filter) throws Exception {
        restExecutionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(execution.getId().intValue())))
            .andExpect(jsonPath("$.[*].execTs").value(hasItem(DEFAULT_EXEC_TS.toString())))
            .andExpect(jsonPath("$.[*].px").value(hasItem(sameNumber(DEFAULT_PX))))
            .andExpect(jsonPath("$.[*].qty").value(hasItem(sameNumber(DEFAULT_QTY))))
            .andExpect(jsonPath("$.[*].liquidity").value(hasItem(DEFAULT_LIQUIDITY)))
            .andExpect(jsonPath("$.[*].fee").value(hasItem(sameNumber(DEFAULT_FEE))));

        // Check, that the count call also returns 1
        restExecutionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultExecutionShouldNotBeFound(String filter) throws Exception {
        restExecutionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restExecutionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingExecution() throws Exception {
        // Get the execution
        restExecutionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingExecution() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the execution
        Execution updatedExecution = executionRepository.findById(execution.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedExecution are not directly saved in db
        em.detach(updatedExecution);
        updatedExecution.execTs(UPDATED_EXEC_TS).px(UPDATED_PX).qty(UPDATED_QTY).liquidity(UPDATED_LIQUIDITY).fee(UPDATED_FEE);
        ExecutionDTO executionDTO = executionMapper.toDto(updatedExecution);

        restExecutionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, executionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(executionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Execution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedExecutionToMatchAllProperties(updatedExecution);
    }

    @Test
    @Transactional
    void putNonExistingExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        execution.setId(longCount.incrementAndGet());

        // Create the Execution
        ExecutionDTO executionDTO = executionMapper.toDto(execution);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExecutionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, executionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(executionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Execution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        execution.setId(longCount.incrementAndGet());

        // Create the Execution
        ExecutionDTO executionDTO = executionMapper.toDto(execution);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExecutionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(executionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Execution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        execution.setId(longCount.incrementAndGet());

        // Create the Execution
        ExecutionDTO executionDTO = executionMapper.toDto(execution);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExecutionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(executionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Execution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateExecutionWithPatch() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the execution using partial update
        Execution partialUpdatedExecution = new Execution();
        partialUpdatedExecution.setId(execution.getId());

        partialUpdatedExecution.qty(UPDATED_QTY).liquidity(UPDATED_LIQUIDITY);

        restExecutionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExecution.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExecution))
            )
            .andExpect(status().isOk());

        // Validate the Execution in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExecutionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedExecution, execution),
            getPersistedExecution(execution)
        );
    }

    @Test
    @Transactional
    void fullUpdateExecutionWithPatch() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the execution using partial update
        Execution partialUpdatedExecution = new Execution();
        partialUpdatedExecution.setId(execution.getId());

        partialUpdatedExecution.execTs(UPDATED_EXEC_TS).px(UPDATED_PX).qty(UPDATED_QTY).liquidity(UPDATED_LIQUIDITY).fee(UPDATED_FEE);

        restExecutionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExecution.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExecution))
            )
            .andExpect(status().isOk());

        // Validate the Execution in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExecutionUpdatableFieldsEquals(partialUpdatedExecution, getPersistedExecution(partialUpdatedExecution));
    }

    @Test
    @Transactional
    void patchNonExistingExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        execution.setId(longCount.incrementAndGet());

        // Create the Execution
        ExecutionDTO executionDTO = executionMapper.toDto(execution);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExecutionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, executionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(executionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Execution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        execution.setId(longCount.incrementAndGet());

        // Create the Execution
        ExecutionDTO executionDTO = executionMapper.toDto(execution);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExecutionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(executionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Execution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamExecution() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        execution.setId(longCount.incrementAndGet());

        // Create the Execution
        ExecutionDTO executionDTO = executionMapper.toDto(execution);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExecutionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(executionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Execution in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteExecution() throws Exception {
        // Initialize the database
        insertedExecution = executionRepository.saveAndFlush(execution);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the execution
        restExecutionMockMvc
            .perform(delete(ENTITY_API_URL_ID, execution.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return executionRepository.count();
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

    protected Execution getPersistedExecution(Execution execution) {
        return executionRepository.findById(execution.getId()).orElseThrow();
    }

    protected void assertPersistedExecutionToMatchAllProperties(Execution expectedExecution) {
        assertExecutionAllPropertiesEquals(expectedExecution, getPersistedExecution(expectedExecution));
    }

    protected void assertPersistedExecutionToMatchUpdatableProperties(Execution expectedExecution) {
        assertExecutionAllUpdatablePropertiesEquals(expectedExecution, getPersistedExecution(expectedExecution));
    }
}
