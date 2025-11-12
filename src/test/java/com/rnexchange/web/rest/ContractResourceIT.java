package com.rnexchange.web.rest;

import static com.rnexchange.domain.ContractAsserts.*;
import static com.rnexchange.web.rest.TestUtil.createUpdateProxyForBean;
import static com.rnexchange.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Contract;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.enumeration.ContractType;
import com.rnexchange.domain.enumeration.OptionType;
import com.rnexchange.repository.ContractRepository;
import com.rnexchange.service.ContractService;
import com.rnexchange.service.dto.ContractDTO;
import com.rnexchange.service.mapper.ContractMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link ContractResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ContractResourceIT {

    private static final String DEFAULT_INSTRUMENT_SYMBOL = "AAAAAAAAAA";
    private static final String UPDATED_INSTRUMENT_SYMBOL = "BBBBBBBBBB";

    private static final ContractType DEFAULT_CONTRACT_TYPE = ContractType.FUTURE;
    private static final ContractType UPDATED_CONTRACT_TYPE = ContractType.OPTION;

    private static final LocalDate DEFAULT_EXPIRY = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EXPIRY = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_EXPIRY = LocalDate.ofEpochDay(-1L);

    private static final BigDecimal DEFAULT_STRIKE = new BigDecimal(1);
    private static final BigDecimal UPDATED_STRIKE = new BigDecimal(2);
    private static final BigDecimal SMALLER_STRIKE = new BigDecimal(1 - 1);

    private static final OptionType DEFAULT_OPTION_TYPE = OptionType.CE;
    private static final OptionType UPDATED_OPTION_TYPE = OptionType.PE;

    private static final String DEFAULT_SEGMENT = "AAAAAAAAAA";
    private static final String UPDATED_SEGMENT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/contracts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ContractRepository contractRepository;

    @Mock
    private ContractRepository contractRepositoryMock;

    @Autowired
    private ContractMapper contractMapper;

    @Mock
    private ContractService contractServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restContractMockMvc;

    private Contract contract;

    private Contract insertedContract;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Contract createEntity() {
        return new Contract()
            .instrumentSymbol(DEFAULT_INSTRUMENT_SYMBOL)
            .contractType(DEFAULT_CONTRACT_TYPE)
            .expiry(DEFAULT_EXPIRY)
            .strike(DEFAULT_STRIKE)
            .optionType(DEFAULT_OPTION_TYPE)
            .segment(DEFAULT_SEGMENT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Contract createUpdatedEntity() {
        return new Contract()
            .instrumentSymbol(UPDATED_INSTRUMENT_SYMBOL)
            .contractType(UPDATED_CONTRACT_TYPE)
            .expiry(UPDATED_EXPIRY)
            .strike(UPDATED_STRIKE)
            .optionType(UPDATED_OPTION_TYPE)
            .segment(UPDATED_SEGMENT);
    }

    @BeforeEach
    void initTest() {
        contract = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedContract != null) {
            contractRepository.delete(insertedContract);
            insertedContract = null;
        }
    }

    @Test
    @Transactional
    void createContract() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Contract
        ContractDTO contractDTO = contractMapper.toDto(contract);
        var returnedContractDTO = om.readValue(
            restContractMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contractDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ContractDTO.class
        );

        // Validate the Contract in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedContract = contractMapper.toEntity(returnedContractDTO);
        assertContractUpdatableFieldsEquals(returnedContract, getPersistedContract(returnedContract));

        insertedContract = returnedContract;
    }

    @Test
    @Transactional
    void createContractWithExistingId() throws Exception {
        // Create the Contract with an existing ID
        contract.setId(1L);
        ContractDTO contractDTO = contractMapper.toDto(contract);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restContractMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contractDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Contract in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkInstrumentSymbolIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        contract.setInstrumentSymbol(null);

        // Create the Contract, which fails.
        ContractDTO contractDTO = contractMapper.toDto(contract);

        restContractMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contractDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkContractTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        contract.setContractType(null);

        // Create the Contract, which fails.
        ContractDTO contractDTO = contractMapper.toDto(contract);

        restContractMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contractDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkExpiryIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        contract.setExpiry(null);

        // Create the Contract, which fails.
        ContractDTO contractDTO = contractMapper.toDto(contract);

        restContractMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contractDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSegmentIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        contract.setSegment(null);

        // Create the Contract, which fails.
        ContractDTO contractDTO = contractMapper.toDto(contract);

        restContractMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contractDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllContracts() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList
        restContractMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(contract.getId().intValue())))
            .andExpect(jsonPath("$.[*].instrumentSymbol").value(hasItem(DEFAULT_INSTRUMENT_SYMBOL)))
            .andExpect(jsonPath("$.[*].contractType").value(hasItem(DEFAULT_CONTRACT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].expiry").value(hasItem(DEFAULT_EXPIRY.toString())))
            .andExpect(jsonPath("$.[*].strike").value(hasItem(sameNumber(DEFAULT_STRIKE))))
            .andExpect(jsonPath("$.[*].optionType").value(hasItem(DEFAULT_OPTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].segment").value(hasItem(DEFAULT_SEGMENT)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllContractsWithEagerRelationshipsIsEnabled() throws Exception {
        when(contractServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restContractMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(contractServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllContractsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(contractServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restContractMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(contractRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getContract() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get the contract
        restContractMockMvc
            .perform(get(ENTITY_API_URL_ID, contract.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(contract.getId().intValue()))
            .andExpect(jsonPath("$.instrumentSymbol").value(DEFAULT_INSTRUMENT_SYMBOL))
            .andExpect(jsonPath("$.contractType").value(DEFAULT_CONTRACT_TYPE.toString()))
            .andExpect(jsonPath("$.expiry").value(DEFAULT_EXPIRY.toString()))
            .andExpect(jsonPath("$.strike").value(sameNumber(DEFAULT_STRIKE)))
            .andExpect(jsonPath("$.optionType").value(DEFAULT_OPTION_TYPE.toString()))
            .andExpect(jsonPath("$.segment").value(DEFAULT_SEGMENT));
    }

    @Test
    @Transactional
    void getContractsByIdFiltering() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        Long id = contract.getId();

        defaultContractFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultContractFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultContractFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllContractsByInstrumentSymbolIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where instrumentSymbol equals to
        defaultContractFiltering(
            "instrumentSymbol.equals=" + DEFAULT_INSTRUMENT_SYMBOL,
            "instrumentSymbol.equals=" + UPDATED_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllContractsByInstrumentSymbolIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where instrumentSymbol in
        defaultContractFiltering(
            "instrumentSymbol.in=" + DEFAULT_INSTRUMENT_SYMBOL + "," + UPDATED_INSTRUMENT_SYMBOL,
            "instrumentSymbol.in=" + UPDATED_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllContractsByInstrumentSymbolIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where instrumentSymbol is not null
        defaultContractFiltering("instrumentSymbol.specified=true", "instrumentSymbol.specified=false");
    }

    @Test
    @Transactional
    void getAllContractsByInstrumentSymbolContainsSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where instrumentSymbol contains
        defaultContractFiltering(
            "instrumentSymbol.contains=" + DEFAULT_INSTRUMENT_SYMBOL,
            "instrumentSymbol.contains=" + UPDATED_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllContractsByInstrumentSymbolNotContainsSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where instrumentSymbol does not contain
        defaultContractFiltering(
            "instrumentSymbol.doesNotContain=" + UPDATED_INSTRUMENT_SYMBOL,
            "instrumentSymbol.doesNotContain=" + DEFAULT_INSTRUMENT_SYMBOL
        );
    }

    @Test
    @Transactional
    void getAllContractsByContractTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where contractType equals to
        defaultContractFiltering("contractType.equals=" + DEFAULT_CONTRACT_TYPE, "contractType.equals=" + UPDATED_CONTRACT_TYPE);
    }

    @Test
    @Transactional
    void getAllContractsByContractTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where contractType in
        defaultContractFiltering(
            "contractType.in=" + DEFAULT_CONTRACT_TYPE + "," + UPDATED_CONTRACT_TYPE,
            "contractType.in=" + UPDATED_CONTRACT_TYPE
        );
    }

    @Test
    @Transactional
    void getAllContractsByContractTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where contractType is not null
        defaultContractFiltering("contractType.specified=true", "contractType.specified=false");
    }

    @Test
    @Transactional
    void getAllContractsByExpiryIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where expiry equals to
        defaultContractFiltering("expiry.equals=" + DEFAULT_EXPIRY, "expiry.equals=" + UPDATED_EXPIRY);
    }

    @Test
    @Transactional
    void getAllContractsByExpiryIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where expiry in
        defaultContractFiltering("expiry.in=" + DEFAULT_EXPIRY + "," + UPDATED_EXPIRY, "expiry.in=" + UPDATED_EXPIRY);
    }

    @Test
    @Transactional
    void getAllContractsByExpiryIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where expiry is not null
        defaultContractFiltering("expiry.specified=true", "expiry.specified=false");
    }

    @Test
    @Transactional
    void getAllContractsByExpiryIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where expiry is greater than or equal to
        defaultContractFiltering("expiry.greaterThanOrEqual=" + DEFAULT_EXPIRY, "expiry.greaterThanOrEqual=" + UPDATED_EXPIRY);
    }

    @Test
    @Transactional
    void getAllContractsByExpiryIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where expiry is less than or equal to
        defaultContractFiltering("expiry.lessThanOrEqual=" + DEFAULT_EXPIRY, "expiry.lessThanOrEqual=" + SMALLER_EXPIRY);
    }

    @Test
    @Transactional
    void getAllContractsByExpiryIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where expiry is less than
        defaultContractFiltering("expiry.lessThan=" + UPDATED_EXPIRY, "expiry.lessThan=" + DEFAULT_EXPIRY);
    }

    @Test
    @Transactional
    void getAllContractsByExpiryIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where expiry is greater than
        defaultContractFiltering("expiry.greaterThan=" + SMALLER_EXPIRY, "expiry.greaterThan=" + DEFAULT_EXPIRY);
    }

    @Test
    @Transactional
    void getAllContractsByStrikeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where strike equals to
        defaultContractFiltering("strike.equals=" + DEFAULT_STRIKE, "strike.equals=" + UPDATED_STRIKE);
    }

    @Test
    @Transactional
    void getAllContractsByStrikeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where strike in
        defaultContractFiltering("strike.in=" + DEFAULT_STRIKE + "," + UPDATED_STRIKE, "strike.in=" + UPDATED_STRIKE);
    }

    @Test
    @Transactional
    void getAllContractsByStrikeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where strike is not null
        defaultContractFiltering("strike.specified=true", "strike.specified=false");
    }

    @Test
    @Transactional
    void getAllContractsByStrikeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where strike is greater than or equal to
        defaultContractFiltering("strike.greaterThanOrEqual=" + DEFAULT_STRIKE, "strike.greaterThanOrEqual=" + UPDATED_STRIKE);
    }

    @Test
    @Transactional
    void getAllContractsByStrikeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where strike is less than or equal to
        defaultContractFiltering("strike.lessThanOrEqual=" + DEFAULT_STRIKE, "strike.lessThanOrEqual=" + SMALLER_STRIKE);
    }

    @Test
    @Transactional
    void getAllContractsByStrikeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where strike is less than
        defaultContractFiltering("strike.lessThan=" + UPDATED_STRIKE, "strike.lessThan=" + DEFAULT_STRIKE);
    }

    @Test
    @Transactional
    void getAllContractsByStrikeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where strike is greater than
        defaultContractFiltering("strike.greaterThan=" + SMALLER_STRIKE, "strike.greaterThan=" + DEFAULT_STRIKE);
    }

    @Test
    @Transactional
    void getAllContractsByOptionTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where optionType equals to
        defaultContractFiltering("optionType.equals=" + DEFAULT_OPTION_TYPE, "optionType.equals=" + UPDATED_OPTION_TYPE);
    }

    @Test
    @Transactional
    void getAllContractsByOptionTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where optionType in
        defaultContractFiltering(
            "optionType.in=" + DEFAULT_OPTION_TYPE + "," + UPDATED_OPTION_TYPE,
            "optionType.in=" + UPDATED_OPTION_TYPE
        );
    }

    @Test
    @Transactional
    void getAllContractsByOptionTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where optionType is not null
        defaultContractFiltering("optionType.specified=true", "optionType.specified=false");
    }

    @Test
    @Transactional
    void getAllContractsBySegmentIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where segment equals to
        defaultContractFiltering("segment.equals=" + DEFAULT_SEGMENT, "segment.equals=" + UPDATED_SEGMENT);
    }

    @Test
    @Transactional
    void getAllContractsBySegmentIsInShouldWork() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where segment in
        defaultContractFiltering("segment.in=" + DEFAULT_SEGMENT + "," + UPDATED_SEGMENT, "segment.in=" + UPDATED_SEGMENT);
    }

    @Test
    @Transactional
    void getAllContractsBySegmentIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where segment is not null
        defaultContractFiltering("segment.specified=true", "segment.specified=false");
    }

    @Test
    @Transactional
    void getAllContractsBySegmentContainsSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where segment contains
        defaultContractFiltering("segment.contains=" + DEFAULT_SEGMENT, "segment.contains=" + UPDATED_SEGMENT);
    }

    @Test
    @Transactional
    void getAllContractsBySegmentNotContainsSomething() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        // Get all the contractList where segment does not contain
        defaultContractFiltering("segment.doesNotContain=" + UPDATED_SEGMENT, "segment.doesNotContain=" + DEFAULT_SEGMENT);
    }

    @Test
    @Transactional
    void getAllContractsByInstrumentIsEqualToSomething() throws Exception {
        Instrument instrument;
        if (TestUtil.findAll(em, Instrument.class).isEmpty()) {
            contractRepository.saveAndFlush(contract);
            instrument = InstrumentResourceIT.createEntity();
        } else {
            instrument = TestUtil.findAll(em, Instrument.class).get(0);
        }
        em.persist(instrument);
        em.flush();
        contract.setInstrument(instrument);
        contractRepository.saveAndFlush(contract);
        Long instrumentId = instrument.getId();
        // Get all the contractList where instrument equals to instrumentId
        defaultContractShouldBeFound("instrumentId.equals=" + instrumentId);

        // Get all the contractList where instrument equals to (instrumentId + 1)
        defaultContractShouldNotBeFound("instrumentId.equals=" + (instrumentId + 1));
    }

    private void defaultContractFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultContractShouldBeFound(shouldBeFound);
        defaultContractShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultContractShouldBeFound(String filter) throws Exception {
        restContractMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(contract.getId().intValue())))
            .andExpect(jsonPath("$.[*].instrumentSymbol").value(hasItem(DEFAULT_INSTRUMENT_SYMBOL)))
            .andExpect(jsonPath("$.[*].contractType").value(hasItem(DEFAULT_CONTRACT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].expiry").value(hasItem(DEFAULT_EXPIRY.toString())))
            .andExpect(jsonPath("$.[*].strike").value(hasItem(sameNumber(DEFAULT_STRIKE))))
            .andExpect(jsonPath("$.[*].optionType").value(hasItem(DEFAULT_OPTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].segment").value(hasItem(DEFAULT_SEGMENT)));

        // Check, that the count call also returns 1
        restContractMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultContractShouldNotBeFound(String filter) throws Exception {
        restContractMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restContractMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingContract() throws Exception {
        // Get the contract
        restContractMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingContract() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the contract
        Contract updatedContract = contractRepository.findById(contract.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedContract are not directly saved in db
        em.detach(updatedContract);
        updatedContract
            .instrumentSymbol(UPDATED_INSTRUMENT_SYMBOL)
            .contractType(UPDATED_CONTRACT_TYPE)
            .expiry(UPDATED_EXPIRY)
            .strike(UPDATED_STRIKE)
            .optionType(UPDATED_OPTION_TYPE)
            .segment(UPDATED_SEGMENT);
        ContractDTO contractDTO = contractMapper.toDto(updatedContract);

        restContractMockMvc
            .perform(
                put(ENTITY_API_URL_ID, contractDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(contractDTO))
            )
            .andExpect(status().isOk());

        // Validate the Contract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedContractToMatchAllProperties(updatedContract);
    }

    @Test
    @Transactional
    void putNonExistingContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contract.setId(longCount.incrementAndGet());

        // Create the Contract
        ContractDTO contractDTO = contractMapper.toDto(contract);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restContractMockMvc
            .perform(
                put(ENTITY_API_URL_ID, contractDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(contractDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contract.setId(longCount.incrementAndGet());

        // Create the Contract
        ContractDTO contractDTO = contractMapper.toDto(contract);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContractMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(contractDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contract.setId(longCount.incrementAndGet());

        // Create the Contract
        ContractDTO contractDTO = contractMapper.toDto(contract);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContractMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contractDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Contract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateContractWithPatch() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the contract using partial update
        Contract partialUpdatedContract = new Contract();
        partialUpdatedContract.setId(contract.getId());

        partialUpdatedContract
            .contractType(UPDATED_CONTRACT_TYPE)
            .expiry(UPDATED_EXPIRY)
            .strike(UPDATED_STRIKE)
            .optionType(UPDATED_OPTION_TYPE)
            .segment(UPDATED_SEGMENT);

        restContractMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedContract.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedContract))
            )
            .andExpect(status().isOk());

        // Validate the Contract in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertContractUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedContract, contract), getPersistedContract(contract));
    }

    @Test
    @Transactional
    void fullUpdateContractWithPatch() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the contract using partial update
        Contract partialUpdatedContract = new Contract();
        partialUpdatedContract.setId(contract.getId());

        partialUpdatedContract
            .instrumentSymbol(UPDATED_INSTRUMENT_SYMBOL)
            .contractType(UPDATED_CONTRACT_TYPE)
            .expiry(UPDATED_EXPIRY)
            .strike(UPDATED_STRIKE)
            .optionType(UPDATED_OPTION_TYPE)
            .segment(UPDATED_SEGMENT);

        restContractMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedContract.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedContract))
            )
            .andExpect(status().isOk());

        // Validate the Contract in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertContractUpdatableFieldsEquals(partialUpdatedContract, getPersistedContract(partialUpdatedContract));
    }

    @Test
    @Transactional
    void patchNonExistingContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contract.setId(longCount.incrementAndGet());

        // Create the Contract
        ContractDTO contractDTO = contractMapper.toDto(contract);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restContractMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, contractDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(contractDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contract.setId(longCount.incrementAndGet());

        // Create the Contract
        ContractDTO contractDTO = contractMapper.toDto(contract);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContractMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(contractDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Contract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        contract.setId(longCount.incrementAndGet());

        // Create the Contract
        ContractDTO contractDTO = contractMapper.toDto(contract);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restContractMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(contractDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Contract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteContract() throws Exception {
        // Initialize the database
        insertedContract = contractRepository.saveAndFlush(contract);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the contract
        restContractMockMvc
            .perform(delete(ENTITY_API_URL_ID, contract.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return contractRepository.count();
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

    protected Contract getPersistedContract(Contract contract) {
        return contractRepository.findById(contract.getId()).orElseThrow();
    }

    protected void assertPersistedContractToMatchAllProperties(Contract expectedContract) {
        assertContractAllPropertiesEquals(expectedContract, getPersistedContract(expectedContract));
    }

    protected void assertPersistedContractToMatchUpdatableProperties(Contract expectedContract) {
        assertContractAllUpdatablePropertiesEquals(expectedContract, getPersistedContract(expectedContract));
    }
}
