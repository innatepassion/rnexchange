package com.rnexchange.web.rest;

import static com.rnexchange.domain.TraderProfileAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.User;
import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.KycStatus;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.repository.UserRepository;
import com.rnexchange.service.TraderProfileService;
import com.rnexchange.service.dto.TraderProfileDTO;
import com.rnexchange.service.mapper.TraderProfileMapper;
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
 * Integration tests for the {@link TraderProfileResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TraderProfileResourceIT {

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_MOBILE = "AAAAAAAAAA";
    private static final String UPDATED_MOBILE = "BBBBBBBBBB";

    private static final KycStatus DEFAULT_KYC_STATUS = KycStatus.PENDING;
    private static final KycStatus UPDATED_KYC_STATUS = KycStatus.APPROVED;

    private static final AccountStatus DEFAULT_STATUS = AccountStatus.ACTIVE;
    private static final AccountStatus UPDATED_STATUS = AccountStatus.INACTIVE;

    private static final String ENTITY_API_URL = "/api/trader-profiles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TraderProfileRepository traderProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private TraderProfileRepository traderProfileRepositoryMock;

    @Autowired
    private TraderProfileMapper traderProfileMapper;

    @Mock
    private TraderProfileService traderProfileServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTraderProfileMockMvc;

    private TraderProfile traderProfile;

    private TraderProfile insertedTraderProfile;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TraderProfile createEntity() {
        return new TraderProfile()
            .displayName(DEFAULT_DISPLAY_NAME)
            .email(DEFAULT_EMAIL)
            .mobile(DEFAULT_MOBILE)
            .kycStatus(DEFAULT_KYC_STATUS)
            .status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TraderProfile createUpdatedEntity() {
        return new TraderProfile()
            .displayName(UPDATED_DISPLAY_NAME)
            .email(UPDATED_EMAIL)
            .mobile(UPDATED_MOBILE)
            .kycStatus(UPDATED_KYC_STATUS)
            .status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        traderProfile = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTraderProfile != null) {
            traderProfileRepository.delete(insertedTraderProfile);
            insertedTraderProfile = null;
        }
    }

    @Test
    @Transactional
    void createTraderProfile() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TraderProfile
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);
        var returnedTraderProfileDTO = om.readValue(
            restTraderProfileMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traderProfileDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TraderProfileDTO.class
        );

        // Validate the TraderProfile in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTraderProfile = traderProfileMapper.toEntity(returnedTraderProfileDTO);
        assertTraderProfileUpdatableFieldsEquals(returnedTraderProfile, getPersistedTraderProfile(returnedTraderProfile));

        insertedTraderProfile = returnedTraderProfile;
    }

    @Test
    @Transactional
    void createTraderProfileWithExistingId() throws Exception {
        // Create the TraderProfile with an existing ID
        traderProfile.setId(1L);
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTraderProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traderProfileDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TraderProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDisplayNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        traderProfile.setDisplayName(null);

        // Create the TraderProfile, which fails.
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        restTraderProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traderProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        traderProfile.setEmail(null);

        // Create the TraderProfile, which fails.
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        restTraderProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traderProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkKycStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        traderProfile.setKycStatus(null);

        // Create the TraderProfile, which fails.
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        restTraderProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traderProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        traderProfile.setStatus(null);

        // Create the TraderProfile, which fails.
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        restTraderProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traderProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTraderProfiles() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList
        restTraderProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(traderProfile.getId().intValue())))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].mobile").value(hasItem(DEFAULT_MOBILE)))
            .andExpect(jsonPath("$.[*].kycStatus").value(hasItem(DEFAULT_KYC_STATUS.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTraderProfilesWithEagerRelationshipsIsEnabled() throws Exception {
        when(traderProfileServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTraderProfileMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(traderProfileServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTraderProfilesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(traderProfileServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTraderProfileMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(traderProfileRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTraderProfile() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get the traderProfile
        restTraderProfileMockMvc
            .perform(get(ENTITY_API_URL_ID, traderProfile.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(traderProfile.getId().intValue()))
            .andExpect(jsonPath("$.displayName").value(DEFAULT_DISPLAY_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.mobile").value(DEFAULT_MOBILE))
            .andExpect(jsonPath("$.kycStatus").value(DEFAULT_KYC_STATUS.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getTraderProfilesByIdFiltering() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        Long id = traderProfile.getId();

        defaultTraderProfileFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultTraderProfileFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultTraderProfileFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByDisplayNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where displayName equals to
        defaultTraderProfileFiltering("displayName.equals=" + DEFAULT_DISPLAY_NAME, "displayName.equals=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByDisplayNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where displayName in
        defaultTraderProfileFiltering(
            "displayName.in=" + DEFAULT_DISPLAY_NAME + "," + UPDATED_DISPLAY_NAME,
            "displayName.in=" + UPDATED_DISPLAY_NAME
        );
    }

    @Test
    @Transactional
    void getAllTraderProfilesByDisplayNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where displayName is not null
        defaultTraderProfileFiltering("displayName.specified=true", "displayName.specified=false");
    }

    @Test
    @Transactional
    void getAllTraderProfilesByDisplayNameContainsSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where displayName contains
        defaultTraderProfileFiltering("displayName.contains=" + DEFAULT_DISPLAY_NAME, "displayName.contains=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByDisplayNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where displayName does not contain
        defaultTraderProfileFiltering(
            "displayName.doesNotContain=" + UPDATED_DISPLAY_NAME,
            "displayName.doesNotContain=" + DEFAULT_DISPLAY_NAME
        );
    }

    @Test
    @Transactional
    void getAllTraderProfilesByEmailIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where email equals to
        defaultTraderProfileFiltering("email.equals=" + DEFAULT_EMAIL, "email.equals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByEmailIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where email in
        defaultTraderProfileFiltering("email.in=" + DEFAULT_EMAIL + "," + UPDATED_EMAIL, "email.in=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByEmailIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where email is not null
        defaultTraderProfileFiltering("email.specified=true", "email.specified=false");
    }

    @Test
    @Transactional
    void getAllTraderProfilesByEmailContainsSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where email contains
        defaultTraderProfileFiltering("email.contains=" + DEFAULT_EMAIL, "email.contains=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByEmailNotContainsSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where email does not contain
        defaultTraderProfileFiltering("email.doesNotContain=" + UPDATED_EMAIL, "email.doesNotContain=" + DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByMobileIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where mobile equals to
        defaultTraderProfileFiltering("mobile.equals=" + DEFAULT_MOBILE, "mobile.equals=" + UPDATED_MOBILE);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByMobileIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where mobile in
        defaultTraderProfileFiltering("mobile.in=" + DEFAULT_MOBILE + "," + UPDATED_MOBILE, "mobile.in=" + UPDATED_MOBILE);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByMobileIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where mobile is not null
        defaultTraderProfileFiltering("mobile.specified=true", "mobile.specified=false");
    }

    @Test
    @Transactional
    void getAllTraderProfilesByMobileContainsSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where mobile contains
        defaultTraderProfileFiltering("mobile.contains=" + DEFAULT_MOBILE, "mobile.contains=" + UPDATED_MOBILE);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByMobileNotContainsSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where mobile does not contain
        defaultTraderProfileFiltering("mobile.doesNotContain=" + UPDATED_MOBILE, "mobile.doesNotContain=" + DEFAULT_MOBILE);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByKycStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where kycStatus equals to
        defaultTraderProfileFiltering("kycStatus.equals=" + DEFAULT_KYC_STATUS, "kycStatus.equals=" + UPDATED_KYC_STATUS);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByKycStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where kycStatus in
        defaultTraderProfileFiltering(
            "kycStatus.in=" + DEFAULT_KYC_STATUS + "," + UPDATED_KYC_STATUS,
            "kycStatus.in=" + UPDATED_KYC_STATUS
        );
    }

    @Test
    @Transactional
    void getAllTraderProfilesByKycStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where kycStatus is not null
        defaultTraderProfileFiltering("kycStatus.specified=true", "kycStatus.specified=false");
    }

    @Test
    @Transactional
    void getAllTraderProfilesByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where status equals to
        defaultTraderProfileFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where status in
        defaultTraderProfileFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTraderProfilesByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        // Get all the traderProfileList where status is not null
        defaultTraderProfileFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllTraderProfilesByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            traderProfileRepository.saveAndFlush(traderProfile);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        traderProfile.setUser(user);
        traderProfileRepository.saveAndFlush(traderProfile);
        Long userId = user.getId();
        // Get all the traderProfileList where user equals to userId
        defaultTraderProfileShouldBeFound("userId.equals=" + userId);

        // Get all the traderProfileList where user equals to (userId + 1)
        defaultTraderProfileShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    private void defaultTraderProfileFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultTraderProfileShouldBeFound(shouldBeFound);
        defaultTraderProfileShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTraderProfileShouldBeFound(String filter) throws Exception {
        restTraderProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(traderProfile.getId().intValue())))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].mobile").value(hasItem(DEFAULT_MOBILE)))
            .andExpect(jsonPath("$.[*].kycStatus").value(hasItem(DEFAULT_KYC_STATUS.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));

        // Check, that the count call also returns 1
        restTraderProfileMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTraderProfileShouldNotBeFound(String filter) throws Exception {
        restTraderProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTraderProfileMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingTraderProfile() throws Exception {
        // Get the traderProfile
        restTraderProfileMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTraderProfile() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the traderProfile
        TraderProfile updatedTraderProfile = traderProfileRepository.findById(traderProfile.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTraderProfile are not directly saved in db
        em.detach(updatedTraderProfile);
        updatedTraderProfile
            .displayName(UPDATED_DISPLAY_NAME)
            .email(UPDATED_EMAIL)
            .mobile(UPDATED_MOBILE)
            .kycStatus(UPDATED_KYC_STATUS)
            .status(UPDATED_STATUS);
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(updatedTraderProfile);

        restTraderProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, traderProfileDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(traderProfileDTO))
            )
            .andExpect(status().isOk());

        // Validate the TraderProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTraderProfileToMatchAllProperties(updatedTraderProfile);
    }

    @Test
    @Transactional
    void putNonExistingTraderProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traderProfile.setId(longCount.incrementAndGet());

        // Create the TraderProfile
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTraderProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, traderProfileDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(traderProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TraderProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTraderProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traderProfile.setId(longCount.incrementAndGet());

        // Create the TraderProfile
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTraderProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(traderProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TraderProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTraderProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traderProfile.setId(longCount.incrementAndGet());

        // Create the TraderProfile
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTraderProfileMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(traderProfileDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TraderProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTraderProfileWithPatch() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the traderProfile using partial update
        TraderProfile partialUpdatedTraderProfile = new TraderProfile();
        partialUpdatedTraderProfile.setId(traderProfile.getId());

        partialUpdatedTraderProfile.displayName(UPDATED_DISPLAY_NAME).email(UPDATED_EMAIL).status(UPDATED_STATUS);

        restTraderProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTraderProfile.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTraderProfile))
            )
            .andExpect(status().isOk());

        // Validate the TraderProfile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTraderProfileUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTraderProfile, traderProfile),
            getPersistedTraderProfile(traderProfile)
        );
    }

    @Test
    @Transactional
    void fullUpdateTraderProfileWithPatch() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the traderProfile using partial update
        TraderProfile partialUpdatedTraderProfile = new TraderProfile();
        partialUpdatedTraderProfile.setId(traderProfile.getId());

        partialUpdatedTraderProfile
            .displayName(UPDATED_DISPLAY_NAME)
            .email(UPDATED_EMAIL)
            .mobile(UPDATED_MOBILE)
            .kycStatus(UPDATED_KYC_STATUS)
            .status(UPDATED_STATUS);

        restTraderProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTraderProfile.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTraderProfile))
            )
            .andExpect(status().isOk());

        // Validate the TraderProfile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTraderProfileUpdatableFieldsEquals(partialUpdatedTraderProfile, getPersistedTraderProfile(partialUpdatedTraderProfile));
    }

    @Test
    @Transactional
    void patchNonExistingTraderProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traderProfile.setId(longCount.incrementAndGet());

        // Create the TraderProfile
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTraderProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, traderProfileDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(traderProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TraderProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTraderProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traderProfile.setId(longCount.incrementAndGet());

        // Create the TraderProfile
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTraderProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(traderProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TraderProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTraderProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        traderProfile.setId(longCount.incrementAndGet());

        // Create the TraderProfile
        TraderProfileDTO traderProfileDTO = traderProfileMapper.toDto(traderProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTraderProfileMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(traderProfileDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TraderProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTraderProfile() throws Exception {
        // Initialize the database
        insertedTraderProfile = traderProfileRepository.saveAndFlush(traderProfile);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the traderProfile
        restTraderProfileMockMvc
            .perform(delete(ENTITY_API_URL_ID, traderProfile.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return traderProfileRepository.count();
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

    protected TraderProfile getPersistedTraderProfile(TraderProfile traderProfile) {
        return traderProfileRepository.findById(traderProfile.getId()).orElseThrow();
    }

    protected void assertPersistedTraderProfileToMatchAllProperties(TraderProfile expectedTraderProfile) {
        assertTraderProfileAllPropertiesEquals(expectedTraderProfile, getPersistedTraderProfile(expectedTraderProfile));
    }

    protected void assertPersistedTraderProfileToMatchUpdatableProperties(TraderProfile expectedTraderProfile) {
        assertTraderProfileAllUpdatablePropertiesEquals(expectedTraderProfile, getPersistedTraderProfile(expectedTraderProfile));
    }
}
