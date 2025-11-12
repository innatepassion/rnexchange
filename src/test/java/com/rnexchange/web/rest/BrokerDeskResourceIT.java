package com.rnexchange.web.rest;

import static com.rnexchange.domain.BrokerDeskAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Broker;
import com.rnexchange.domain.BrokerDesk;
import com.rnexchange.domain.User;
import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.repository.UserRepository;
import com.rnexchange.service.BrokerDeskService;
import com.rnexchange.service.dto.BrokerDeskDTO;
import com.rnexchange.service.mapper.BrokerDeskMapper;
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
 * Integration tests for the {@link BrokerDeskResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class BrokerDeskResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/broker-desks";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BrokerDeskRepository brokerDeskRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private BrokerDeskRepository brokerDeskRepositoryMock;

    @Autowired
    private BrokerDeskMapper brokerDeskMapper;

    @Mock
    private BrokerDeskService brokerDeskServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBrokerDeskMockMvc;

    private BrokerDesk brokerDesk;

    private BrokerDesk insertedBrokerDesk;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BrokerDesk createEntity() {
        return new BrokerDesk().name(DEFAULT_NAME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BrokerDesk createUpdatedEntity() {
        return new BrokerDesk().name(UPDATED_NAME);
    }

    @BeforeEach
    void initTest() {
        brokerDesk = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedBrokerDesk != null) {
            brokerDeskRepository.delete(insertedBrokerDesk);
            insertedBrokerDesk = null;
        }
    }

    @Test
    @Transactional
    void createBrokerDesk() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the BrokerDesk
        BrokerDeskDTO brokerDeskDTO = brokerDeskMapper.toDto(brokerDesk);
        var returnedBrokerDeskDTO = om.readValue(
            restBrokerDeskMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDeskDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BrokerDeskDTO.class
        );

        // Validate the BrokerDesk in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBrokerDesk = brokerDeskMapper.toEntity(returnedBrokerDeskDTO);
        assertBrokerDeskUpdatableFieldsEquals(returnedBrokerDesk, getPersistedBrokerDesk(returnedBrokerDesk));

        insertedBrokerDesk = returnedBrokerDesk;
    }

    @Test
    @Transactional
    void createBrokerDeskWithExistingId() throws Exception {
        // Create the BrokerDesk with an existing ID
        brokerDesk.setId(1L);
        BrokerDeskDTO brokerDeskDTO = brokerDeskMapper.toDto(brokerDesk);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBrokerDeskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDeskDTO)))
            .andExpect(status().isBadRequest());

        // Validate the BrokerDesk in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        brokerDesk.setName(null);

        // Create the BrokerDesk, which fails.
        BrokerDeskDTO brokerDeskDTO = brokerDeskMapper.toDto(brokerDesk);

        restBrokerDeskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDeskDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBrokerDesks() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        // Get all the brokerDeskList
        restBrokerDeskMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(brokerDesk.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBrokerDesksWithEagerRelationshipsIsEnabled() throws Exception {
        when(brokerDeskServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restBrokerDeskMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(brokerDeskServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllBrokerDesksWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(brokerDeskServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restBrokerDeskMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(brokerDeskRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getBrokerDesk() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        // Get the brokerDesk
        restBrokerDeskMockMvc
            .perform(get(ENTITY_API_URL_ID, brokerDesk.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(brokerDesk.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getBrokerDesksByIdFiltering() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        Long id = brokerDesk.getId();

        defaultBrokerDeskFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultBrokerDeskFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultBrokerDeskFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllBrokerDesksByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        // Get all the brokerDeskList where name equals to
        defaultBrokerDeskFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllBrokerDesksByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        // Get all the brokerDeskList where name in
        defaultBrokerDeskFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllBrokerDesksByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        // Get all the brokerDeskList where name is not null
        defaultBrokerDeskFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllBrokerDesksByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        // Get all the brokerDeskList where name contains
        defaultBrokerDeskFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllBrokerDesksByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        // Get all the brokerDeskList where name does not contain
        defaultBrokerDeskFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllBrokerDesksByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            brokerDeskRepository.saveAndFlush(brokerDesk);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        brokerDesk.setUser(user);
        brokerDeskRepository.saveAndFlush(brokerDesk);
        Long userId = user.getId();
        // Get all the brokerDeskList where user equals to userId
        defaultBrokerDeskShouldBeFound("userId.equals=" + userId);

        // Get all the brokerDeskList where user equals to (userId + 1)
        defaultBrokerDeskShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    @Test
    @Transactional
    void getAllBrokerDesksByBrokerIsEqualToSomething() throws Exception {
        Broker broker;
        if (TestUtil.findAll(em, Broker.class).isEmpty()) {
            brokerDeskRepository.saveAndFlush(brokerDesk);
            broker = BrokerResourceIT.createEntity();
        } else {
            broker = TestUtil.findAll(em, Broker.class).get(0);
        }
        em.persist(broker);
        em.flush();
        brokerDesk.setBroker(broker);
        brokerDeskRepository.saveAndFlush(brokerDesk);
        Long brokerId = broker.getId();
        // Get all the brokerDeskList where broker equals to brokerId
        defaultBrokerDeskShouldBeFound("brokerId.equals=" + brokerId);

        // Get all the brokerDeskList where broker equals to (brokerId + 1)
        defaultBrokerDeskShouldNotBeFound("brokerId.equals=" + (brokerId + 1));
    }

    private void defaultBrokerDeskFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultBrokerDeskShouldBeFound(shouldBeFound);
        defaultBrokerDeskShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBrokerDeskShouldBeFound(String filter) throws Exception {
        restBrokerDeskMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(brokerDesk.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // Check, that the count call also returns 1
        restBrokerDeskMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultBrokerDeskShouldNotBeFound(String filter) throws Exception {
        restBrokerDeskMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restBrokerDeskMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingBrokerDesk() throws Exception {
        // Get the brokerDesk
        restBrokerDeskMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBrokerDesk() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the brokerDesk
        BrokerDesk updatedBrokerDesk = brokerDeskRepository.findById(brokerDesk.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBrokerDesk are not directly saved in db
        em.detach(updatedBrokerDesk);
        updatedBrokerDesk.name(UPDATED_NAME);
        BrokerDeskDTO brokerDeskDTO = brokerDeskMapper.toDto(updatedBrokerDesk);

        restBrokerDeskMockMvc
            .perform(
                put(ENTITY_API_URL_ID, brokerDeskDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(brokerDeskDTO))
            )
            .andExpect(status().isOk());

        // Validate the BrokerDesk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBrokerDeskToMatchAllProperties(updatedBrokerDesk);
    }

    @Test
    @Transactional
    void putNonExistingBrokerDesk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerDesk.setId(longCount.incrementAndGet());

        // Create the BrokerDesk
        BrokerDeskDTO brokerDeskDTO = brokerDeskMapper.toDto(brokerDesk);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBrokerDeskMockMvc
            .perform(
                put(ENTITY_API_URL_ID, brokerDeskDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(brokerDeskDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BrokerDesk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBrokerDesk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerDesk.setId(longCount.incrementAndGet());

        // Create the BrokerDesk
        BrokerDeskDTO brokerDeskDTO = brokerDeskMapper.toDto(brokerDesk);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerDeskMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(brokerDeskDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BrokerDesk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBrokerDesk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerDesk.setId(longCount.incrementAndGet());

        // Create the BrokerDesk
        BrokerDeskDTO brokerDeskDTO = brokerDeskMapper.toDto(brokerDesk);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerDeskMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDeskDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BrokerDesk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBrokerDeskWithPatch() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the brokerDesk using partial update
        BrokerDesk partialUpdatedBrokerDesk = new BrokerDesk();
        partialUpdatedBrokerDesk.setId(brokerDesk.getId());

        partialUpdatedBrokerDesk.name(UPDATED_NAME);

        restBrokerDeskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBrokerDesk.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBrokerDesk))
            )
            .andExpect(status().isOk());

        // Validate the BrokerDesk in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerDeskUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedBrokerDesk, brokerDesk),
            getPersistedBrokerDesk(brokerDesk)
        );
    }

    @Test
    @Transactional
    void fullUpdateBrokerDeskWithPatch() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the brokerDesk using partial update
        BrokerDesk partialUpdatedBrokerDesk = new BrokerDesk();
        partialUpdatedBrokerDesk.setId(brokerDesk.getId());

        partialUpdatedBrokerDesk.name(UPDATED_NAME);

        restBrokerDeskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBrokerDesk.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBrokerDesk))
            )
            .andExpect(status().isOk());

        // Validate the BrokerDesk in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerDeskUpdatableFieldsEquals(partialUpdatedBrokerDesk, getPersistedBrokerDesk(partialUpdatedBrokerDesk));
    }

    @Test
    @Transactional
    void patchNonExistingBrokerDesk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerDesk.setId(longCount.incrementAndGet());

        // Create the BrokerDesk
        BrokerDeskDTO brokerDeskDTO = brokerDeskMapper.toDto(brokerDesk);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBrokerDeskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, brokerDeskDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(brokerDeskDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BrokerDesk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBrokerDesk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerDesk.setId(longCount.incrementAndGet());

        // Create the BrokerDesk
        BrokerDeskDTO brokerDeskDTO = brokerDeskMapper.toDto(brokerDesk);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerDeskMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(brokerDeskDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BrokerDesk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBrokerDesk() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerDesk.setId(longCount.incrementAndGet());

        // Create the BrokerDesk
        BrokerDeskDTO brokerDeskDTO = brokerDeskMapper.toDto(brokerDesk);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerDeskMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(brokerDeskDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BrokerDesk in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBrokerDesk() throws Exception {
        // Initialize the database
        insertedBrokerDesk = brokerDeskRepository.saveAndFlush(brokerDesk);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the brokerDesk
        restBrokerDeskMockMvc
            .perform(delete(ENTITY_API_URL_ID, brokerDesk.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return brokerDeskRepository.count();
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

    protected BrokerDesk getPersistedBrokerDesk(BrokerDesk brokerDesk) {
        return brokerDeskRepository.findById(brokerDesk.getId()).orElseThrow();
    }

    protected void assertPersistedBrokerDeskToMatchAllProperties(BrokerDesk expectedBrokerDesk) {
        assertBrokerDeskAllPropertiesEquals(expectedBrokerDesk, getPersistedBrokerDesk(expectedBrokerDesk));
    }

    protected void assertPersistedBrokerDeskToMatchUpdatableProperties(BrokerDesk expectedBrokerDesk) {
        assertBrokerDeskAllUpdatablePropertiesEquals(expectedBrokerDesk, getPersistedBrokerDesk(expectedBrokerDesk));
    }
}
