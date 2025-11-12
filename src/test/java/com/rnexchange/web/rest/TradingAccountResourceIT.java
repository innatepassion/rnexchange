package com.rnexchange.web.rest;

import static com.rnexchange.domain.TradingAccountAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static com.rnexchange.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Broker;
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.AccountType;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.service.TradingAccountService;
import com.rnexchange.service.dto.TradingAccountDTO;
import com.rnexchange.service.mapper.TradingAccountMapper;
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
 * Integration tests for the {@link TradingAccountResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TradingAccountResourceIT {

    private static final AccountType DEFAULT_TYPE = AccountType.CASH;
    private static final AccountType UPDATED_TYPE = AccountType.MARGIN;

    private static final Currency DEFAULT_BASE_CCY = Currency.INR;
    private static final Currency UPDATED_BASE_CCY = Currency.USD;

    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal(1);
    private static final BigDecimal UPDATED_BALANCE = new BigDecimal(2);
    private static final BigDecimal SMALLER_BALANCE = new BigDecimal(1 - 1);

    private static final AccountStatus DEFAULT_STATUS = AccountStatus.ACTIVE;
    private static final AccountStatus UPDATED_STATUS = AccountStatus.INACTIVE;

    private static final String ENTITY_API_URL = "/api/trading-accounts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TradingAccountRepository tradingAccountRepository;

    @Mock
    private TradingAccountRepository tradingAccountRepositoryMock;

    @Autowired
    private TradingAccountMapper tradingAccountMapper;

    @Mock
    private TradingAccountService tradingAccountServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTradingAccountMockMvc;

    private TradingAccount tradingAccount;

    private TradingAccount insertedTradingAccount;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TradingAccount createEntity() {
        return new TradingAccount().type(DEFAULT_TYPE).baseCcy(DEFAULT_BASE_CCY).balance(DEFAULT_BALANCE).status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TradingAccount createUpdatedEntity() {
        return new TradingAccount().type(UPDATED_TYPE).baseCcy(UPDATED_BASE_CCY).balance(UPDATED_BALANCE).status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        tradingAccount = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTradingAccount != null) {
            tradingAccountRepository.delete(insertedTradingAccount);
            insertedTradingAccount = null;
        }
    }

    @Test
    @Transactional
    void createTradingAccount() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TradingAccount
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);
        var returnedTradingAccountDTO = om.readValue(
            restTradingAccountMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tradingAccountDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TradingAccountDTO.class
        );

        // Validate the TradingAccount in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTradingAccount = tradingAccountMapper.toEntity(returnedTradingAccountDTO);
        assertTradingAccountUpdatableFieldsEquals(returnedTradingAccount, getPersistedTradingAccount(returnedTradingAccount));

        insertedTradingAccount = returnedTradingAccount;
    }

    @Test
    @Transactional
    void createTradingAccountWithExistingId() throws Exception {
        // Create the TradingAccount with an existing ID
        tradingAccount.setId(1L);
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTradingAccountMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tradingAccountDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TradingAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        tradingAccount.setType(null);

        // Create the TradingAccount, which fails.
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        restTradingAccountMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tradingAccountDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBaseCcyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        tradingAccount.setBaseCcy(null);

        // Create the TradingAccount, which fails.
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        restTradingAccountMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tradingAccountDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBalanceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        tradingAccount.setBalance(null);

        // Create the TradingAccount, which fails.
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        restTradingAccountMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tradingAccountDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        tradingAccount.setStatus(null);

        // Create the TradingAccount, which fails.
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        restTradingAccountMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tradingAccountDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTradingAccounts() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList
        restTradingAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tradingAccount.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].baseCcy").value(hasItem(DEFAULT_BASE_CCY.toString())))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(sameNumber(DEFAULT_BALANCE))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTradingAccountsWithEagerRelationshipsIsEnabled() throws Exception {
        when(tradingAccountServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTradingAccountMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(tradingAccountServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTradingAccountsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(tradingAccountServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTradingAccountMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(tradingAccountRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTradingAccount() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get the tradingAccount
        restTradingAccountMockMvc
            .perform(get(ENTITY_API_URL_ID, tradingAccount.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tradingAccount.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.baseCcy").value(DEFAULT_BASE_CCY.toString()))
            .andExpect(jsonPath("$.balance").value(sameNumber(DEFAULT_BALANCE)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getTradingAccountsByIdFiltering() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        Long id = tradingAccount.getId();

        defaultTradingAccountFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultTradingAccountFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultTradingAccountFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where type equals to
        defaultTradingAccountFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where type in
        defaultTradingAccountFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where type is not null
        defaultTradingAccountFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBaseCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where baseCcy equals to
        defaultTradingAccountFiltering("baseCcy.equals=" + DEFAULT_BASE_CCY, "baseCcy.equals=" + UPDATED_BASE_CCY);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBaseCcyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where baseCcy in
        defaultTradingAccountFiltering("baseCcy.in=" + DEFAULT_BASE_CCY + "," + UPDATED_BASE_CCY, "baseCcy.in=" + UPDATED_BASE_CCY);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBaseCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where baseCcy is not null
        defaultTradingAccountFiltering("baseCcy.specified=true", "baseCcy.specified=false");
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBalanceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where balance equals to
        defaultTradingAccountFiltering("balance.equals=" + DEFAULT_BALANCE, "balance.equals=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBalanceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where balance in
        defaultTradingAccountFiltering("balance.in=" + DEFAULT_BALANCE + "," + UPDATED_BALANCE, "balance.in=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBalanceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where balance is not null
        defaultTradingAccountFiltering("balance.specified=true", "balance.specified=false");
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBalanceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where balance is greater than or equal to
        defaultTradingAccountFiltering("balance.greaterThanOrEqual=" + DEFAULT_BALANCE, "balance.greaterThanOrEqual=" + UPDATED_BALANCE);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBalanceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where balance is less than or equal to
        defaultTradingAccountFiltering("balance.lessThanOrEqual=" + DEFAULT_BALANCE, "balance.lessThanOrEqual=" + SMALLER_BALANCE);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBalanceIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where balance is less than
        defaultTradingAccountFiltering("balance.lessThan=" + UPDATED_BALANCE, "balance.lessThan=" + DEFAULT_BALANCE);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBalanceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where balance is greater than
        defaultTradingAccountFiltering("balance.greaterThan=" + SMALLER_BALANCE, "balance.greaterThan=" + DEFAULT_BALANCE);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where status equals to
        defaultTradingAccountFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where status in
        defaultTradingAccountFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTradingAccountsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        // Get all the tradingAccountList where status is not null
        defaultTradingAccountFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllTradingAccountsByBrokerIsEqualToSomething() throws Exception {
        Broker broker;
        if (TestUtil.findAll(em, Broker.class).isEmpty()) {
            tradingAccountRepository.saveAndFlush(tradingAccount);
            broker = BrokerResourceIT.createEntity();
        } else {
            broker = TestUtil.findAll(em, Broker.class).get(0);
        }
        em.persist(broker);
        em.flush();
        tradingAccount.setBroker(broker);
        tradingAccountRepository.saveAndFlush(tradingAccount);
        Long brokerId = broker.getId();
        // Get all the tradingAccountList where broker equals to brokerId
        defaultTradingAccountShouldBeFound("brokerId.equals=" + brokerId);

        // Get all the tradingAccountList where broker equals to (brokerId + 1)
        defaultTradingAccountShouldNotBeFound("brokerId.equals=" + (brokerId + 1));
    }

    @Test
    @Transactional
    void getAllTradingAccountsByTraderIsEqualToSomething() throws Exception {
        TraderProfile trader;
        if (TestUtil.findAll(em, TraderProfile.class).isEmpty()) {
            tradingAccountRepository.saveAndFlush(tradingAccount);
            trader = TraderProfileResourceIT.createEntity();
        } else {
            trader = TestUtil.findAll(em, TraderProfile.class).get(0);
        }
        em.persist(trader);
        em.flush();
        tradingAccount.setTrader(trader);
        tradingAccountRepository.saveAndFlush(tradingAccount);
        Long traderId = trader.getId();
        // Get all the tradingAccountList where trader equals to traderId
        defaultTradingAccountShouldBeFound("traderId.equals=" + traderId);

        // Get all the tradingAccountList where trader equals to (traderId + 1)
        defaultTradingAccountShouldNotBeFound("traderId.equals=" + (traderId + 1));
    }

    private void defaultTradingAccountFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultTradingAccountShouldBeFound(shouldBeFound);
        defaultTradingAccountShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTradingAccountShouldBeFound(String filter) throws Exception {
        restTradingAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tradingAccount.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].baseCcy").value(hasItem(DEFAULT_BASE_CCY.toString())))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(sameNumber(DEFAULT_BALANCE))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));

        // Check, that the count call also returns 1
        restTradingAccountMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTradingAccountShouldNotBeFound(String filter) throws Exception {
        restTradingAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTradingAccountMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingTradingAccount() throws Exception {
        // Get the tradingAccount
        restTradingAccountMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTradingAccount() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tradingAccount
        TradingAccount updatedTradingAccount = tradingAccountRepository.findById(tradingAccount.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTradingAccount are not directly saved in db
        em.detach(updatedTradingAccount);
        updatedTradingAccount.type(UPDATED_TYPE).baseCcy(UPDATED_BASE_CCY).balance(UPDATED_BALANCE).status(UPDATED_STATUS);
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(updatedTradingAccount);

        restTradingAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tradingAccountDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tradingAccountDTO))
            )
            .andExpect(status().isOk());

        // Validate the TradingAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTradingAccountToMatchAllProperties(updatedTradingAccount);
    }

    @Test
    @Transactional
    void putNonExistingTradingAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tradingAccount.setId(longCount.incrementAndGet());

        // Create the TradingAccount
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTradingAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, tradingAccountDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tradingAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TradingAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTradingAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tradingAccount.setId(longCount.incrementAndGet());

        // Create the TradingAccount
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTradingAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(tradingAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TradingAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTradingAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tradingAccount.setId(longCount.incrementAndGet());

        // Create the TradingAccount
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTradingAccountMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(tradingAccountDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TradingAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTradingAccountWithPatch() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tradingAccount using partial update
        TradingAccount partialUpdatedTradingAccount = new TradingAccount();
        partialUpdatedTradingAccount.setId(tradingAccount.getId());

        partialUpdatedTradingAccount.balance(UPDATED_BALANCE).status(UPDATED_STATUS);

        restTradingAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTradingAccount.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTradingAccount))
            )
            .andExpect(status().isOk());

        // Validate the TradingAccount in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTradingAccountUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTradingAccount, tradingAccount),
            getPersistedTradingAccount(tradingAccount)
        );
    }

    @Test
    @Transactional
    void fullUpdateTradingAccountWithPatch() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the tradingAccount using partial update
        TradingAccount partialUpdatedTradingAccount = new TradingAccount();
        partialUpdatedTradingAccount.setId(tradingAccount.getId());

        partialUpdatedTradingAccount.type(UPDATED_TYPE).baseCcy(UPDATED_BASE_CCY).balance(UPDATED_BALANCE).status(UPDATED_STATUS);

        restTradingAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTradingAccount.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTradingAccount))
            )
            .andExpect(status().isOk());

        // Validate the TradingAccount in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTradingAccountUpdatableFieldsEquals(partialUpdatedTradingAccount, getPersistedTradingAccount(partialUpdatedTradingAccount));
    }

    @Test
    @Transactional
    void patchNonExistingTradingAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tradingAccount.setId(longCount.incrementAndGet());

        // Create the TradingAccount
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTradingAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, tradingAccountDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tradingAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TradingAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTradingAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tradingAccount.setId(longCount.incrementAndGet());

        // Create the TradingAccount
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTradingAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(tradingAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TradingAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTradingAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        tradingAccount.setId(longCount.incrementAndGet());

        // Create the TradingAccount
        TradingAccountDTO tradingAccountDTO = tradingAccountMapper.toDto(tradingAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTradingAccountMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(tradingAccountDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TradingAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTradingAccount() throws Exception {
        // Initialize the database
        insertedTradingAccount = tradingAccountRepository.saveAndFlush(tradingAccount);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the tradingAccount
        restTradingAccountMockMvc
            .perform(delete(ENTITY_API_URL_ID, tradingAccount.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return tradingAccountRepository.count();
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

    protected TradingAccount getPersistedTradingAccount(TradingAccount tradingAccount) {
        return tradingAccountRepository.findById(tradingAccount.getId()).orElseThrow();
    }

    protected void assertPersistedTradingAccountToMatchAllProperties(TradingAccount expectedTradingAccount) {
        assertTradingAccountAllPropertiesEquals(expectedTradingAccount, getPersistedTradingAccount(expectedTradingAccount));
    }

    protected void assertPersistedTradingAccountToMatchUpdatableProperties(TradingAccount expectedTradingAccount) {
        assertTradingAccountAllUpdatablePropertiesEquals(expectedTradingAccount, getPersistedTradingAccount(expectedTradingAccount));
    }
}
