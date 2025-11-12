package com.rnexchange.web.rest;

import static com.rnexchange.domain.ExchangeOperatorAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.ExchangeOperator;
import com.rnexchange.domain.User;
import com.rnexchange.repository.ExchangeOperatorRepository;
import com.rnexchange.repository.UserRepository;
import com.rnexchange.service.ExchangeOperatorService;
import com.rnexchange.service.dto.ExchangeOperatorDTO;
import com.rnexchange.service.mapper.ExchangeOperatorMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link ExchangeOperatorResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ExchangeOperatorResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/exchange-operators";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ExchangeOperatorRepository exchangeOperatorRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ExchangeOperatorRepository exchangeOperatorRepositoryMock;

    @Autowired
    private ExchangeOperatorMapper exchangeOperatorMapper;

    @Mock
    private ExchangeOperatorService exchangeOperatorServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restExchangeOperatorMockMvc;

    private ExchangeOperator exchangeOperator;

    private ExchangeOperator insertedExchangeOperator;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ExchangeOperator createEntity() {
        return new ExchangeOperator().name(DEFAULT_NAME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ExchangeOperator createUpdatedEntity() {
        return new ExchangeOperator().name(UPDATED_NAME);
    }

    @BeforeEach
    void initTest() {
        exchangeOperator = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedExchangeOperator != null) {
            exchangeOperatorRepository.delete(insertedExchangeOperator);
            insertedExchangeOperator = null;
        }
    }

    @Test
    @Transactional
    void createExchangeOperator() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ExchangeOperator
        ExchangeOperatorDTO exchangeOperatorDTO = exchangeOperatorMapper.toDto(exchangeOperator);
        var returnedExchangeOperatorDTO = om.readValue(
            restExchangeOperatorMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeOperatorDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ExchangeOperatorDTO.class
        );

        // Validate the ExchangeOperator in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedExchangeOperator = exchangeOperatorMapper.toEntity(returnedExchangeOperatorDTO);
        assertExchangeOperatorUpdatableFieldsEquals(returnedExchangeOperator, getPersistedExchangeOperator(returnedExchangeOperator));

        insertedExchangeOperator = returnedExchangeOperator;
    }

    @Test
    @Transactional
    void createExchangeOperatorWithExistingId() throws Exception {
        // Create the ExchangeOperator with an existing ID
        exchangeOperator.setId(1L);
        ExchangeOperatorDTO exchangeOperatorDTO = exchangeOperatorMapper.toDto(exchangeOperator);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restExchangeOperatorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeOperatorDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ExchangeOperator in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        exchangeOperator.setName(null);

        // Create the ExchangeOperator, which fails.
        ExchangeOperatorDTO exchangeOperatorDTO = exchangeOperatorMapper.toDto(exchangeOperator);

        restExchangeOperatorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeOperatorDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllExchangeOperators() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        // Get all the exchangeOperatorList
        restExchangeOperatorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(exchangeOperator.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllExchangeOperatorsWithEagerRelationshipsIsEnabled() throws Exception {
        when(exchangeOperatorServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restExchangeOperatorMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(exchangeOperatorServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllExchangeOperatorsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(exchangeOperatorServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restExchangeOperatorMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(exchangeOperatorRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getExchangeOperator() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        // Get the exchangeOperator
        restExchangeOperatorMockMvc
            .perform(get(ENTITY_API_URL_ID, exchangeOperator.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(exchangeOperator.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getExchangeOperatorsByIdFiltering() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        Long id = exchangeOperator.getId();

        defaultExchangeOperatorFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultExchangeOperatorFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultExchangeOperatorFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllExchangeOperatorsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        // Get all the exchangeOperatorList where name equals to
        defaultExchangeOperatorFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllExchangeOperatorsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        // Get all the exchangeOperatorList where name in
        defaultExchangeOperatorFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllExchangeOperatorsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        // Get all the exchangeOperatorList where name is not null
        defaultExchangeOperatorFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllExchangeOperatorsByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        // Get all the exchangeOperatorList where name contains
        defaultExchangeOperatorFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllExchangeOperatorsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        // Get all the exchangeOperatorList where name does not contain
        defaultExchangeOperatorFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllExchangeOperatorsByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            exchangeOperatorRepository.saveAndFlush(exchangeOperator);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        exchangeOperator.setUser(user);
        exchangeOperatorRepository.saveAndFlush(exchangeOperator);
        Long userId = user.getId();
        // Get all the exchangeOperatorList where user equals to userId
        defaultExchangeOperatorShouldBeFound("userId.equals=" + userId);

        // Get all the exchangeOperatorList where user equals to (userId + 1)
        defaultExchangeOperatorShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    @Test
    @Transactional
    void getAllExchangeOperatorsByExchangeIsEqualToSomething() throws Exception {
        Exchange exchange;
        if (TestUtil.findAll(em, Exchange.class).isEmpty()) {
            exchangeOperatorRepository.saveAndFlush(exchangeOperator);
            exchange = ExchangeResourceIT.createEntity();
        } else {
            exchange = TestUtil.findAll(em, Exchange.class).get(0);
        }
        em.persist(exchange);
        em.flush();
        exchangeOperator.setExchange(exchange);
        exchangeOperatorRepository.saveAndFlush(exchangeOperator);
        Long exchangeId = exchange.getId();
        // Get all the exchangeOperatorList where exchange equals to exchangeId
        defaultExchangeOperatorShouldBeFound("exchangeId.equals=" + exchangeId);

        // Get all the exchangeOperatorList where exchange equals to (exchangeId + 1)
        defaultExchangeOperatorShouldNotBeFound("exchangeId.equals=" + (exchangeId + 1));
    }

    private void defaultExchangeOperatorFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultExchangeOperatorShouldBeFound(shouldBeFound);
        defaultExchangeOperatorShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultExchangeOperatorShouldBeFound(String filter) throws Exception {
        restExchangeOperatorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(exchangeOperator.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // Check, that the count call also returns 1
        restExchangeOperatorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultExchangeOperatorShouldNotBeFound(String filter) throws Exception {
        restExchangeOperatorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restExchangeOperatorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingExchangeOperator() throws Exception {
        // Get the exchangeOperator
        restExchangeOperatorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingExchangeOperator() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the exchangeOperator
        ExchangeOperator updatedExchangeOperator = exchangeOperatorRepository.findById(exchangeOperator.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedExchangeOperator are not directly saved in db
        em.detach(updatedExchangeOperator);
        updatedExchangeOperator.name(UPDATED_NAME);
        ExchangeOperatorDTO exchangeOperatorDTO = exchangeOperatorMapper.toDto(updatedExchangeOperator);

        restExchangeOperatorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, exchangeOperatorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(exchangeOperatorDTO))
            )
            .andExpect(status().isOk());

        // Validate the ExchangeOperator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedExchangeOperatorToMatchAllProperties(updatedExchangeOperator);
    }

    @Test
    @Transactional
    void putNonExistingExchangeOperator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeOperator.setId(longCount.incrementAndGet());

        // Create the ExchangeOperator
        ExchangeOperatorDTO exchangeOperatorDTO = exchangeOperatorMapper.toDto(exchangeOperator);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExchangeOperatorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, exchangeOperatorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(exchangeOperatorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExchangeOperator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchExchangeOperator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeOperator.setId(longCount.incrementAndGet());

        // Create the ExchangeOperator
        ExchangeOperatorDTO exchangeOperatorDTO = exchangeOperatorMapper.toDto(exchangeOperator);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeOperatorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(exchangeOperatorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExchangeOperator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamExchangeOperator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeOperator.setId(longCount.incrementAndGet());

        // Create the ExchangeOperator
        ExchangeOperatorDTO exchangeOperatorDTO = exchangeOperatorMapper.toDto(exchangeOperator);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeOperatorMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exchangeOperatorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ExchangeOperator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateExchangeOperatorWithPatch() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the exchangeOperator using partial update
        ExchangeOperator partialUpdatedExchangeOperator = new ExchangeOperator();
        partialUpdatedExchangeOperator.setId(exchangeOperator.getId());

        partialUpdatedExchangeOperator.name(UPDATED_NAME);

        restExchangeOperatorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExchangeOperator.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExchangeOperator))
            )
            .andExpect(status().isOk());

        // Validate the ExchangeOperator in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExchangeOperatorUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedExchangeOperator, exchangeOperator),
            getPersistedExchangeOperator(exchangeOperator)
        );
    }

    @Test
    @Transactional
    void fullUpdateExchangeOperatorWithPatch() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the exchangeOperator using partial update
        ExchangeOperator partialUpdatedExchangeOperator = new ExchangeOperator();
        partialUpdatedExchangeOperator.setId(exchangeOperator.getId());

        partialUpdatedExchangeOperator.name(UPDATED_NAME);

        restExchangeOperatorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedExchangeOperator.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedExchangeOperator))
            )
            .andExpect(status().isOk());

        // Validate the ExchangeOperator in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertExchangeOperatorUpdatableFieldsEquals(
            partialUpdatedExchangeOperator,
            getPersistedExchangeOperator(partialUpdatedExchangeOperator)
        );
    }

    @Test
    @Transactional
    void patchNonExistingExchangeOperator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeOperator.setId(longCount.incrementAndGet());

        // Create the ExchangeOperator
        ExchangeOperatorDTO exchangeOperatorDTO = exchangeOperatorMapper.toDto(exchangeOperator);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restExchangeOperatorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, exchangeOperatorDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(exchangeOperatorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExchangeOperator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchExchangeOperator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeOperator.setId(longCount.incrementAndGet());

        // Create the ExchangeOperator
        ExchangeOperatorDTO exchangeOperatorDTO = exchangeOperatorMapper.toDto(exchangeOperator);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeOperatorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(exchangeOperatorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ExchangeOperator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamExchangeOperator() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        exchangeOperator.setId(longCount.incrementAndGet());

        // Create the ExchangeOperator
        ExchangeOperatorDTO exchangeOperatorDTO = exchangeOperatorMapper.toDto(exchangeOperator);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restExchangeOperatorMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(exchangeOperatorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ExchangeOperator in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteExchangeOperator() throws Exception {
        // Initialize the database
        insertedExchangeOperator = exchangeOperatorRepository.saveAndFlush(exchangeOperator);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the exchangeOperator
        restExchangeOperatorMockMvc
            .perform(delete(ENTITY_API_URL_ID, exchangeOperator.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return exchangeOperatorRepository.count();
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

    protected ExchangeOperator getPersistedExchangeOperator(ExchangeOperator exchangeOperator) {
        return exchangeOperatorRepository.findById(exchangeOperator.getId()).orElseThrow();
    }

    protected void assertPersistedExchangeOperatorToMatchAllProperties(ExchangeOperator expectedExchangeOperator) {
        assertExchangeOperatorAllPropertiesEquals(expectedExchangeOperator, getPersistedExchangeOperator(expectedExchangeOperator));
    }

    protected void assertPersistedExchangeOperatorToMatchUpdatableProperties(ExchangeOperator expectedExchangeOperator) {
        assertExchangeOperatorAllUpdatablePropertiesEquals(
            expectedExchangeOperator,
            getPersistedExchangeOperator(expectedExchangeOperator)
        );
    }
}
