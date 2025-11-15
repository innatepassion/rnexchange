package com.rnexchange.web.rest;

import static com.rnexchange.domain.LedgerEntryAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static com.rnexchange.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.LedgerEntry;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.domain.enumeration.LedgerEntryType;
import com.rnexchange.repository.LedgerEntryRepository;
import com.rnexchange.service.dto.LedgerEntryDTO;
import com.rnexchange.service.mapper.LedgerEntryMapper;
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
 * Integration tests for the {@link LedgerEntryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class LedgerEntryResourceIT {

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final LedgerEntryType DEFAULT_TYPE = LedgerEntryType.DEBIT;
    private static final LedgerEntryType UPDATED_TYPE = LedgerEntryType.CREDIT;

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);
    private static final BigDecimal SMALLER_AMOUNT = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_FEE = new BigDecimal(0);
    private static final BigDecimal UPDATED_FEE = new BigDecimal(1);
    private static final BigDecimal SMALLER_FEE = new BigDecimal(0 - 1);

    private static final Currency DEFAULT_CCY = Currency.INR;
    private static final Currency UPDATED_CCY = Currency.USD;

    private static final BigDecimal DEFAULT_BALANCE_AFTER = new BigDecimal(1);
    private static final BigDecimal UPDATED_BALANCE_AFTER = new BigDecimal(2);
    private static final BigDecimal SMALLER_BALANCE_AFTER = new BigDecimal(1 - 1);

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_REFERENCE = "AAAAAAAAAA";
    private static final String UPDATED_REFERENCE = "BBBBBBBBBB";

    private static final String DEFAULT_REMARKS = "AAAAAAAAAA";
    private static final String UPDATED_REMARKS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/ledger-entries";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private LedgerEntryMapper ledgerEntryMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restLedgerEntryMockMvc;

    private LedgerEntry ledgerEntry;

    private LedgerEntry insertedLedgerEntry;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static LedgerEntry createEntity() {
        return new LedgerEntry()
            .createdAt(DEFAULT_CREATED_AT)
            .type(DEFAULT_TYPE)
            .amount(DEFAULT_AMOUNT)
            .fee(DEFAULT_FEE)
            .ccy(DEFAULT_CCY)
            .balanceAfter(DEFAULT_BALANCE_AFTER)
            .description(DEFAULT_DESCRIPTION)
            .reference(DEFAULT_REFERENCE)
            .remarks(DEFAULT_REMARKS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static LedgerEntry createUpdatedEntity() {
        return new LedgerEntry()
            .createdAt(UPDATED_CREATED_AT)
            .type(UPDATED_TYPE)
            .amount(UPDATED_AMOUNT)
            .fee(UPDATED_FEE)
            .ccy(UPDATED_CCY)
            .balanceAfter(UPDATED_BALANCE_AFTER)
            .description(UPDATED_DESCRIPTION)
            .reference(UPDATED_REFERENCE)
            .remarks(UPDATED_REMARKS);
    }

    @BeforeEach
    void initTest() {
        ledgerEntry = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedLedgerEntry != null) {
            ledgerEntryRepository.delete(insertedLedgerEntry);
            insertedLedgerEntry = null;
        }
    }

    @Test
    @Transactional
    void createLedgerEntry() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the LedgerEntry
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);
        var returnedLedgerEntryDTO = om.readValue(
            restLedgerEntryMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ledgerEntryDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            LedgerEntryDTO.class
        );

        // Validate the LedgerEntry in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedLedgerEntry = ledgerEntryMapper.toEntity(returnedLedgerEntryDTO);
        assertLedgerEntryUpdatableFieldsEquals(returnedLedgerEntry, getPersistedLedgerEntry(returnedLedgerEntry));

        insertedLedgerEntry = returnedLedgerEntry;
    }

    @Test
    @Transactional
    void createLedgerEntryWithExistingId() throws Exception {
        // Create the LedgerEntry with an existing ID
        ledgerEntry.setId(1L);
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restLedgerEntryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ledgerEntryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the LedgerEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ledgerEntry.setCreatedAt(null);

        // Create the LedgerEntry, which fails.
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        restLedgerEntryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ledgerEntryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ledgerEntry.setType(null);

        // Create the LedgerEntry, which fails.
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        restLedgerEntryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ledgerEntryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ledgerEntry.setAmount(null);

        // Create the LedgerEntry, which fails.
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        restLedgerEntryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ledgerEntryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCcyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ledgerEntry.setCcy(null);

        // Create the LedgerEntry, which fails.
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        restLedgerEntryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ledgerEntryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllLedgerEntries() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList
        restLedgerEntryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ledgerEntry.getId().intValue())))
            .andExpect(jsonPath("$.[*].ts").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].ccy").value(hasItem(DEFAULT_CCY.toString())))
            .andExpect(jsonPath("$.[*].balanceAfter").value(hasItem(sameNumber(DEFAULT_BALANCE_AFTER))))
            .andExpect(jsonPath("$.[*].reference").value(hasItem(DEFAULT_REFERENCE)))
            .andExpect(jsonPath("$.[*].remarks").value(hasItem(DEFAULT_REMARKS)));
    }

    @Test
    @Transactional
    void getLedgerEntry() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get the ledgerEntry
        restLedgerEntryMockMvc
            .perform(get(ENTITY_API_URL_ID, ledgerEntry.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ledgerEntry.getId().intValue()))
            .andExpect(jsonPath("$.ts").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.ccy").value(DEFAULT_CCY.toString()))
            .andExpect(jsonPath("$.balanceAfter").value(sameNumber(DEFAULT_BALANCE_AFTER)))
            .andExpect(jsonPath("$.reference").value(DEFAULT_REFERENCE))
            .andExpect(jsonPath("$.remarks").value(DEFAULT_REMARKS));
    }

    @Test
    @Transactional
    void getLedgerEntriesByIdFiltering() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        Long id = ledgerEntry.getId();

        defaultLedgerEntryFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultLedgerEntryFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultLedgerEntryFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByTsIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where ts equals to
        defaultLedgerEntryFiltering("ts.equals=" + DEFAULT_CREATED_AT, "ts.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByTsIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where ts in
        defaultLedgerEntryFiltering("ts.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "ts.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByTsIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where ts is not null
        defaultLedgerEntryFiltering("ts.specified=true", "ts.specified=false");
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where type equals to
        defaultLedgerEntryFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where type in
        defaultLedgerEntryFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where type is not null
        defaultLedgerEntryFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByTypeContainsSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where type contains
        defaultLedgerEntryFiltering("type.contains=" + DEFAULT_TYPE, "type.contains=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByTypeNotContainsSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where type does not contain
        defaultLedgerEntryFiltering("type.doesNotContain=" + UPDATED_TYPE, "type.doesNotContain=" + DEFAULT_TYPE);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where amount equals to
        defaultLedgerEntryFiltering("amount.equals=" + DEFAULT_AMOUNT, "amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where amount in
        defaultLedgerEntryFiltering("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT, "amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where amount is not null
        defaultLedgerEntryFiltering("amount.specified=true", "amount.specified=false");
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where amount is greater than or equal to
        defaultLedgerEntryFiltering("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT, "amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where amount is less than or equal to
        defaultLedgerEntryFiltering("amount.lessThanOrEqual=" + DEFAULT_AMOUNT, "amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where amount is less than
        defaultLedgerEntryFiltering("amount.lessThan=" + UPDATED_AMOUNT, "amount.lessThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where amount is greater than
        defaultLedgerEntryFiltering("amount.greaterThan=" + SMALLER_AMOUNT, "amount.greaterThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByCcyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where ccy equals to
        defaultLedgerEntryFiltering("ccy.equals=" + DEFAULT_CCY, "ccy.equals=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByCcyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where ccy in
        defaultLedgerEntryFiltering("ccy.in=" + DEFAULT_CCY + "," + UPDATED_CCY, "ccy.in=" + UPDATED_CCY);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByCcyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where ccy is not null
        defaultLedgerEntryFiltering("ccy.specified=true", "ccy.specified=false");
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByBalanceAfterIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where balanceAfter equals to
        defaultLedgerEntryFiltering("balanceAfter.equals=" + DEFAULT_BALANCE_AFTER, "balanceAfter.equals=" + UPDATED_BALANCE_AFTER);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByBalanceAfterIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where balanceAfter in
        defaultLedgerEntryFiltering(
            "balanceAfter.in=" + DEFAULT_BALANCE_AFTER + "," + UPDATED_BALANCE_AFTER,
            "balanceAfter.in=" + UPDATED_BALANCE_AFTER
        );
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByBalanceAfterIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where balanceAfter is not null
        defaultLedgerEntryFiltering("balanceAfter.specified=true", "balanceAfter.specified=false");
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByBalanceAfterIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where balanceAfter is greater than or equal to
        defaultLedgerEntryFiltering(
            "balanceAfter.greaterThanOrEqual=" + DEFAULT_BALANCE_AFTER,
            "balanceAfter.greaterThanOrEqual=" + UPDATED_BALANCE_AFTER
        );
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByBalanceAfterIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where balanceAfter is less than or equal to
        defaultLedgerEntryFiltering(
            "balanceAfter.lessThanOrEqual=" + DEFAULT_BALANCE_AFTER,
            "balanceAfter.lessThanOrEqual=" + SMALLER_BALANCE_AFTER
        );
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByBalanceAfterIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where balanceAfter is less than
        defaultLedgerEntryFiltering("balanceAfter.lessThan=" + UPDATED_BALANCE_AFTER, "balanceAfter.lessThan=" + DEFAULT_BALANCE_AFTER);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByBalanceAfterIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where balanceAfter is greater than
        defaultLedgerEntryFiltering(
            "balanceAfter.greaterThan=" + SMALLER_BALANCE_AFTER,
            "balanceAfter.greaterThan=" + DEFAULT_BALANCE_AFTER
        );
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByReferenceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where reference equals to
        defaultLedgerEntryFiltering("reference.equals=" + DEFAULT_REFERENCE, "reference.equals=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByReferenceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where reference in
        defaultLedgerEntryFiltering("reference.in=" + DEFAULT_REFERENCE + "," + UPDATED_REFERENCE, "reference.in=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByReferenceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where reference is not null
        defaultLedgerEntryFiltering("reference.specified=true", "reference.specified=false");
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByReferenceContainsSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where reference contains
        defaultLedgerEntryFiltering("reference.contains=" + DEFAULT_REFERENCE, "reference.contains=" + UPDATED_REFERENCE);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByReferenceNotContainsSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where reference does not contain
        defaultLedgerEntryFiltering("reference.doesNotContain=" + UPDATED_REFERENCE, "reference.doesNotContain=" + DEFAULT_REFERENCE);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByRemarksIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where remarks equals to
        defaultLedgerEntryFiltering("remarks.equals=" + DEFAULT_REMARKS, "remarks.equals=" + UPDATED_REMARKS);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByRemarksIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where remarks in
        defaultLedgerEntryFiltering("remarks.in=" + DEFAULT_REMARKS + "," + UPDATED_REMARKS, "remarks.in=" + UPDATED_REMARKS);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByRemarksIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where remarks is not null
        defaultLedgerEntryFiltering("remarks.specified=true", "remarks.specified=false");
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByRemarksContainsSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where remarks contains
        defaultLedgerEntryFiltering("remarks.contains=" + DEFAULT_REMARKS, "remarks.contains=" + UPDATED_REMARKS);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByRemarksNotContainsSomething() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Get all the ledgerEntryList where remarks does not contain
        defaultLedgerEntryFiltering("remarks.doesNotContain=" + UPDATED_REMARKS, "remarks.doesNotContain=" + DEFAULT_REMARKS);
    }

    @Test
    @Transactional
    void getAllLedgerEntriesByTradingAccountIsEqualToSomething() throws Exception {
        TradingAccount tradingAccount;
        if (TestUtil.findAll(em, TradingAccount.class).isEmpty()) {
            ledgerEntryRepository.saveAndFlush(ledgerEntry);
            tradingAccount = TradingAccountResourceIT.createEntity();
        } else {
            tradingAccount = TestUtil.findAll(em, TradingAccount.class).get(0);
        }
        em.persist(tradingAccount);
        em.flush();
        ledgerEntry.setTradingAccount(tradingAccount);
        ledgerEntryRepository.saveAndFlush(ledgerEntry);
        Long tradingAccountId = tradingAccount.getId();
        // Get all the ledgerEntryList where tradingAccount equals to tradingAccountId
        defaultLedgerEntryShouldBeFound("tradingAccountId.equals=" + tradingAccountId);

        // Get all the ledgerEntryList where tradingAccount equals to (tradingAccountId + 1)
        defaultLedgerEntryShouldNotBeFound("tradingAccountId.equals=" + (tradingAccountId + 1));
    }

    private void defaultLedgerEntryFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultLedgerEntryShouldBeFound(shouldBeFound);
        defaultLedgerEntryShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultLedgerEntryShouldBeFound(String filter) throws Exception {
        restLedgerEntryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ledgerEntry.getId().intValue())))
            .andExpect(jsonPath("$.[*].ts").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].ccy").value(hasItem(DEFAULT_CCY.toString())))
            .andExpect(jsonPath("$.[*].balanceAfter").value(hasItem(sameNumber(DEFAULT_BALANCE_AFTER))))
            .andExpect(jsonPath("$.[*].reference").value(hasItem(DEFAULT_REFERENCE)))
            .andExpect(jsonPath("$.[*].remarks").value(hasItem(DEFAULT_REMARKS)));

        // Check, that the count call also returns 1
        restLedgerEntryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultLedgerEntryShouldNotBeFound(String filter) throws Exception {
        restLedgerEntryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restLedgerEntryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingLedgerEntry() throws Exception {
        // Get the ledgerEntry
        restLedgerEntryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingLedgerEntry() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ledgerEntry
        LedgerEntry updatedLedgerEntry = ledgerEntryRepository.findById(ledgerEntry.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedLedgerEntry are not directly saved in db
        em.detach(updatedLedgerEntry);
        updatedLedgerEntry
            .createdAt(UPDATED_CREATED_AT)
            .type(UPDATED_TYPE)
            .amount(UPDATED_AMOUNT)
            .ccy(UPDATED_CCY)
            .balanceAfter(UPDATED_BALANCE_AFTER)
            .reference(UPDATED_REFERENCE)
            .remarks(UPDATED_REMARKS);
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(updatedLedgerEntry);

        restLedgerEntryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, ledgerEntryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(ledgerEntryDTO))
            )
            .andExpect(status().isOk());

        // Validate the LedgerEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedLedgerEntryToMatchAllProperties(updatedLedgerEntry);
    }

    @Test
    @Transactional
    void putNonExistingLedgerEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ledgerEntry.setId(longCount.incrementAndGet());

        // Create the LedgerEntry
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLedgerEntryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, ledgerEntryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(ledgerEntryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the LedgerEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchLedgerEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ledgerEntry.setId(longCount.incrementAndGet());

        // Create the LedgerEntry
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLedgerEntryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(ledgerEntryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the LedgerEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamLedgerEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ledgerEntry.setId(longCount.incrementAndGet());

        // Create the LedgerEntry
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLedgerEntryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ledgerEntryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the LedgerEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateLedgerEntryWithPatch() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ledgerEntry using partial update
        LedgerEntry partialUpdatedLedgerEntry = new LedgerEntry();
        partialUpdatedLedgerEntry.setId(ledgerEntry.getId());

        partialUpdatedLedgerEntry.type(UPDATED_TYPE).amount(UPDATED_AMOUNT).ccy(UPDATED_CCY);

        restLedgerEntryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLedgerEntry.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLedgerEntry))
            )
            .andExpect(status().isOk());

        // Validate the LedgerEntry in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLedgerEntryUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedLedgerEntry, ledgerEntry),
            getPersistedLedgerEntry(ledgerEntry)
        );
    }

    @Test
    @Transactional
    void fullUpdateLedgerEntryWithPatch() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ledgerEntry using partial update
        LedgerEntry partialUpdatedLedgerEntry = new LedgerEntry();
        partialUpdatedLedgerEntry.setId(ledgerEntry.getId());

        partialUpdatedLedgerEntry
            .createdAt(UPDATED_CREATED_AT)
            .type(UPDATED_TYPE)
            .amount(UPDATED_AMOUNT)
            .ccy(UPDATED_CCY)
            .balanceAfter(UPDATED_BALANCE_AFTER)
            .reference(UPDATED_REFERENCE)
            .remarks(UPDATED_REMARKS);

        restLedgerEntryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLedgerEntry.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLedgerEntry))
            )
            .andExpect(status().isOk());

        // Validate the LedgerEntry in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLedgerEntryUpdatableFieldsEquals(partialUpdatedLedgerEntry, getPersistedLedgerEntry(partialUpdatedLedgerEntry));
    }

    @Test
    @Transactional
    void patchNonExistingLedgerEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ledgerEntry.setId(longCount.incrementAndGet());

        // Create the LedgerEntry
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLedgerEntryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, ledgerEntryDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(ledgerEntryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the LedgerEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchLedgerEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ledgerEntry.setId(longCount.incrementAndGet());

        // Create the LedgerEntry
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLedgerEntryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(ledgerEntryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the LedgerEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamLedgerEntry() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ledgerEntry.setId(longCount.incrementAndGet());

        // Create the LedgerEntry
        LedgerEntryDTO ledgerEntryDTO = ledgerEntryMapper.toDto(ledgerEntry);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLedgerEntryMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(ledgerEntryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the LedgerEntry in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteLedgerEntry() throws Exception {
        // Initialize the database
        insertedLedgerEntry = ledgerEntryRepository.saveAndFlush(ledgerEntry);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the ledgerEntry
        restLedgerEntryMockMvc
            .perform(delete(ENTITY_API_URL_ID, ledgerEntry.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return ledgerEntryRepository.count();
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

    protected LedgerEntry getPersistedLedgerEntry(LedgerEntry ledgerEntry) {
        return ledgerEntryRepository.findById(ledgerEntry.getId()).orElseThrow();
    }

    protected void assertPersistedLedgerEntryToMatchAllProperties(LedgerEntry expectedLedgerEntry) {
        assertLedgerEntryAllPropertiesEquals(expectedLedgerEntry, getPersistedLedgerEntry(expectedLedgerEntry));
    }

    protected void assertPersistedLedgerEntryToMatchUpdatableProperties(LedgerEntry expectedLedgerEntry) {
        assertLedgerEntryAllUpdatablePropertiesEquals(expectedLedgerEntry, getPersistedLedgerEntry(expectedLedgerEntry));
    }
}
