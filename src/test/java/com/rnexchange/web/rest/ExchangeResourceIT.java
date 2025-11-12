package com.rnexchange.web.rest;

import static com.rnexchange.domain.ExchangeAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.enumeration.ExchangeStatus;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.service.dto.ExchangeDTO;
import com.rnexchange.service.mapper.ExchangeMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link ExchangeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ExchangeResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_TIMEZONE = "AAAAAAAAAA";
    private static final String UPDATED_TIMEZONE = "BBBBBBBBBB";

    private static final ExchangeStatus DEFAULT_STATUS = ExchangeStatus.ACTIVE;
    private static final ExchangeStatus UPDATED_STATUS = ExchangeStatus.INACTIVE;

    private static final String ENTITY_API_URL = "/api/exchanges";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private ExchangeMapper exchangeMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restExchangeMockMvc;

    private Exchange exchange;

    private Exchange insertedExchange;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Exchange createEntity() {
        return new Exchange().code(DEFAULT_CODE).name(DEFAULT_NAME).timezone(DEFAULT_TIMEZONE).status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Exchange createUpdatedEntity() {
        return new Exchange().code(UPDATED_CODE).name(UPDATED_NAME).timezone(UPDATED_TIMEZONE).status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        exchange = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedExchange != null) {
            exchangeRepository.delete(insertedExchange);
            insertedExchange = null;
        }
    }

    @Test
    @Transactional
    void createExchange() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Exchange
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);
        var returnedExchangeDTO = om.readValue(
            restExchangeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ExchangeDTO.class
        );

        // Validate the Exchange in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedExchange = exchangeMapper.toEntity(returnedExchangeDTO);
        assertExchangeUpdatableFieldsEquals(returnedExchange, getPersistedExchange(returnedExchange));

        insertedExchange = returnedExchange;
    }

    @Test
    @Transactional
    void createExchangeWithExistingId() throws Exception {
        // Create the Exchange with an existing ID
        exchange.setId(1L);
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restExchangeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Exchange in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCodeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        exchange.setCode(null);

        // Create the Exchange, which fails.
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        restExchangeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        exchange.setName(null);

        // Create the Exchange, which fails.
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        restExchangeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTimezoneIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        exchange.setTimezone(null);

        // Create the Exchange, which fails.
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        restExchangeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        exchange.setStatus(null);

        // Create the Exchange, which fails.
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        restExchangeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllExchanges() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList
        restExchangeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(exchange.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].timezone").value(hasItem(DEFAULT_TIMEZONE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    @Transactional
    void getExchange() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get the exchange
        restExchangeMockMvc
            .perform(get(ENTITY_API_URL_ID, exchange.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(exchange.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.timezone").value(DEFAULT_TIMEZONE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getExchangesByIdFiltering() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        Long id = exchange.getId();

        defaultExchangeFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultExchangeFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultExchangeFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllExchangesByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where code equals to
        defaultExchangeFiltering("code.equals=" + DEFAULT_CODE, "code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllExchangesByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where code in
        defaultExchangeFiltering("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE, "code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllExchangesByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where code is not null
        defaultExchangeFiltering("code.specified=true", "code.specified=false");
    }

    @Test
    @Transactional
    void getAllExchangesByCodeContainsSomething() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where code contains
        defaultExchangeFiltering("code.contains=" + DEFAULT_CODE, "code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    void getAllExchangesByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where code does not contain
        defaultExchangeFiltering("code.doesNotContain=" + UPDATED_CODE, "code.doesNotContain=" + DEFAULT_CODE);
    }

    @Test
    @Transactional
    void getAllExchangesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where name equals to
        defaultExchangeFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllExchangesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where name in
        defaultExchangeFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllExchangesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where name is not null
        defaultExchangeFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllExchangesByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where name contains
        defaultExchangeFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllExchangesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where name does not contain
        defaultExchangeFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllExchangesByTimezoneIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where timezone equals to
        defaultExchangeFiltering("timezone.equals=" + DEFAULT_TIMEZONE, "timezone.equals=" + UPDATED_TIMEZONE);
    }

    @Test
    @Transactional
    void getAllExchangesByTimezoneIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where timezone in
        defaultExchangeFiltering("timezone.in=" + DEFAULT_TIMEZONE + "," + UPDATED_TIMEZONE, "timezone.in=" + UPDATED_TIMEZONE);
    }

    @Test
    @Transactional
    void getAllExchangesByTimezoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where timezone is not null
        defaultExchangeFiltering("timezone.specified=true", "timezone.specified=false");
    }

    @Test
    @Transactional
    void getAllExchangesByTimezoneContainsSomething() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where timezone contains
        defaultExchangeFiltering("timezone.contains=" + DEFAULT_TIMEZONE, "timezone.contains=" + UPDATED_TIMEZONE);
    }

    @Test
    @Transactional
    void getAllExchangesByTimezoneNotContainsSomething() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where timezone does not contain
        defaultExchangeFiltering("timezone.doesNotContain=" + UPDATED_TIMEZONE, "timezone.doesNotContain=" + DEFAULT_TIMEZONE);
    }

    @Test
    @Transactional
    void getAllExchangesByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where status equals to
        defaultExchangeFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllExchangesByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where status in
        defaultExchangeFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllExchangesByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        // Get all the exchangeList where status is not null
        defaultExchangeFiltering("status.specified=true", "status.specified=false");
    }

    private void defaultExchangeFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultExchangeShouldBeFound(shouldBeFound);
        defaultExchangeShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultExchangeShouldBeFound(String filter) throws Exception {
        restExchangeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(exchange.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].timezone").value(hasItem(DEFAULT_TIMEZONE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));

        // Check, that the count call also returns 1
        restExchangeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultExchangeShouldNotBeFound(String filter) throws Exception {
        restExchangeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restExchangeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingExchange() throws Exception {
        // Get the exchange
        restExchangeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingExchange() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the exchange
        Exchange updatedExchange = exchangeRepository.findById(exchange.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedExchange are not directly saved in db
        em.detach(updatedExchange);
        updatedExchange.code(UPDATED_CODE).name(UPDATED_NAME).timezone(UPDATED_TIMEZONE).status(UPDATED_STATUS);
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(updatedExchange);

        restExchangeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, exchangeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(exchangeDTO))
            )
            .andExpect(status().isOk());

        // Validate the Exchange in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedExchangeToMatchAllProperties(updatedExchange);
    }

    @Test
    @Transactional
    void putNonExistingExchange() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchange.setId(longCount.incrementAndGet());

        // Create the Exchange
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExchangeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, exchangeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(exchangeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Exchange in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchExchange() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchange.setId(longCount.incrementAndGet());

        // Create the Exchange
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(exchangeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Exchange in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamExchange() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchange.setId(longCount.incrementAndGet());

        // Create the Exchange
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Exchange in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateExchangeWithPatch() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the exchange using partial update
        Exchange partialUpdatedExchange = new Exchange();
        partialUpdatedExchange.setId(exchange.getId());

        partialUpdatedExchange.name(UPDATED_NAME);

        restExchangeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExchange.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExchange))
            )
            .andExpect(status().isOk());

        // Validate the Exchange in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExchangeUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedExchange, exchange), getPersistedExchange(exchange));
    }

    @Test
    @Transactional
    void fullUpdateExchangeWithPatch() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the exchange using partial update
        Exchange partialUpdatedExchange = new Exchange();
        partialUpdatedExchange.setId(exchange.getId());

        partialUpdatedExchange.code(UPDATED_CODE).name(UPDATED_NAME).timezone(UPDATED_TIMEZONE).status(UPDATED_STATUS);

        restExchangeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExchange.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExchange))
            )
            .andExpect(status().isOk());

        // Validate the Exchange in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExchangeUpdatableFieldsEquals(partialUpdatedExchange, getPersistedExchange(partialUpdatedExchange));
    }

    @Test
    @Transactional
    void patchNonExistingExchange() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchange.setId(longCount.incrementAndGet());

        // Create the Exchange
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExchangeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, exchangeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(exchangeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Exchange in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchExchange() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchange.setId(longCount.incrementAndGet());

        // Create the Exchange
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(exchangeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Exchange in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamExchange() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchange.setId(longCount.incrementAndGet());

        // Create the Exchange
        ExchangeDTO exchangeDTO = exchangeMapper.toDto(exchange);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(exchangeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Exchange in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteExchange() throws Exception {
        // Initialize the database
        insertedExchange = exchangeRepository.saveAndFlush(exchange);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the exchange
        restExchangeMockMvc
            .perform(delete(ENTITY_API_URL_ID, exchange.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return exchangeRepository.count();
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

    protected Exchange getPersistedExchange(Exchange exchange) {
        return exchangeRepository.findById(exchange.getId()).orElseThrow();
    }

    protected void assertPersistedExchangeToMatchAllProperties(Exchange expectedExchange) {
        assertExchangeAllPropertiesEquals(expectedExchange, getPersistedExchange(expectedExchange));
    }

    protected void assertPersistedExchangeToMatchUpdatableProperties(Exchange expectedExchange) {
        assertExchangeAllUpdatablePropertiesEquals(expectedExchange, getPersistedExchange(expectedExchange));
    }
}
