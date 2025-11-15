package com.rnexchange.web.rest;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.*;
import com.rnexchange.domain.enumeration.*;
import com.rnexchange.repository.*;
import com.rnexchange.service.TradingService;
import com.rnexchange.service.dto.TraderOrderRequest;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * T023: Integration tests for Broker Admin portfolio resource.
 *
 * Verifies that Broker Admin users can only see data for Traders under their broker
 * (Phase 5, User Story 3).
 *
 * Test scenarios:
 * - Broker Admin can view orders for their traders
 * - Broker Admin can view positions for their traders
 * - Broker Admin can view ledger entries for their traders
 * - Non-BROKER_ADMIN users cannot access these endpoints
 * - Broker Admin for Broker A cannot see Broker B's data
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "test-broker-admin", roles = "BROKER_ADMIN")
class BrokerAdminPortfolioResourceIT {

    private static final String BROKER_ADMIN_URL = "/api/admin/portfolio";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerDeskRepository brokerDeskRepository;

    @Autowired
    private TraderProfileRepository traderProfileRepository;

    @Autowired
    private TradingAccountRepository tradingAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private TradingService tradingService;

    private Broker brokerA;
    private Broker brokerB;
    private User brokerAdminUserA;
    private User traderUserA;
    private User traderUserB;
    private TraderProfile traderProfileA;
    private TraderProfile traderProfileB;
    private TradingAccount tradingAccountA;
    private TradingAccount tradingAccountB;
    private Instrument instrument;

    @BeforeEach
    void setUp() {
        // Create two brokers
        brokerA = new Broker();
        brokerA.setCode("BROKER_A");
        brokerA.setName("Broker A");
        brokerA.setStatus("ACTIVE");
        brokerA.setCreatedDate(Instant.now());
        brokerA = brokerRepository.saveAndFlush(brokerA);

        brokerB = new Broker();
        brokerB.setCode("BROKER_B");
        brokerB.setName("Broker B");
        brokerB.setStatus("ACTIVE");
        brokerB.setCreatedDate(Instant.now());
        brokerB = brokerRepository.saveAndFlush(brokerB);

        // Create broker admin for Broker A
        brokerAdminUserA = new User();
        brokerAdminUserA.setLogin("test-broker-admin");
        brokerAdminUserA.setPassword("$2a$10$VEjxo0jq3/lySevSYWyve.xtarI36OvZnCp68fMbVVDE8viKR8.4u"); // hashed "password"
        brokerAdminUserA.setEmail("admin-a@broker.com");
        brokerAdminUserA.setActivated(true);
        brokerAdminUserA = userRepository.saveAndFlush(brokerAdminUserA);

        // Link broker admin to Broker A via BrokerDesk
        BrokerDesk brokerDeskA = new BrokerDesk();
        brokerDeskA.setName("Broker A Desk");
        brokerDeskA.setUser(brokerAdminUserA);
        brokerDeskA.setBroker(brokerA);
        brokerDeskRepository.saveAndFlush(brokerDeskA);

        // Create traders for Broker A and B
        traderUserA = new User();
        traderUserA.setLogin("trader-a");
        traderUserA.setPassword("$2a$10$VEjxo0jq3/lySevSYWyve.xtarI36OvZnCp68fMbVVDE8viKR8.4u");
        traderUserA.setEmail("trader-a@broker.com");
        traderUserA.setActivated(true);
        traderUserA = userRepository.saveAndFlush(traderUserA);

        traderUserB = new User();
        traderUserB.setLogin("trader-b");
        traderUserB.setPassword("$2a$10$VEjxo0jq3/lySevSYWyve.xtarI36OvZnCp68fMbVVDE8viKR8.4u");
        traderUserB.setEmail("trader-b@broker.com");
        traderUserB.setActivated(true);
        traderUserB = userRepository.saveAndFlush(traderUserB);

        // Create trader profiles
        traderProfileA = new TraderProfile();
        traderProfileA.setUser(traderUserA);
        traderProfileA = traderProfileRepository.saveAndFlush(traderProfileA);

        traderProfileB = new TraderProfile();
        traderProfileB.setUser(traderUserB);
        traderProfileB = traderProfileRepository.saveAndFlush(traderProfileB);

        // Create trading accounts for different brokers
        tradingAccountA = new TradingAccount();
        tradingAccountA.setType(AccountType.CASH);
        tradingAccountA.setBaseCcy(Currency.INR);
        tradingAccountA.setBalance(new BigDecimal("100000.00"));
        tradingAccountA.setStatus(AccountStatus.ACTIVE);
        tradingAccountA.setBroker(brokerA);
        tradingAccountA.setTrader(traderProfileA);
        tradingAccountA = tradingAccountRepository.saveAndFlush(tradingAccountA);

        tradingAccountB = new TradingAccount();
        tradingAccountB.setType(AccountType.CASH);
        tradingAccountB.setBaseCcy(Currency.INR);
        tradingAccountB.setBalance(new BigDecimal("100000.00"));
        tradingAccountB.setStatus(AccountStatus.ACTIVE);
        tradingAccountB.setBroker(brokerB);
        tradingAccountB.setTrader(traderProfileB);
        tradingAccountB = tradingAccountRepository.saveAndFlush(tradingAccountB);

        // Create instrument for trading
        instrument = new Instrument();
        instrument.setSymbol("RELIANCE");
        instrument.setExchange("NSE");
        instrument.setStatus(InstrumentStatus.ACTIVE);
        instrument.setLotSize(new BigDecimal("1"));
        instrument.setTickSize(new BigDecimal("0.05"));
        instrument = instrumentRepository.saveAndFlush(instrument);

        em.flush();
    }

    /**
     * T023: Test that Broker Admin can view orders for their broker's traders
     */
    @Test
    @Transactional
    void testBrokerAdminCanViewOwnBrokerOrders() throws Exception {
        // Create an order for Trader A (under Broker A)
        Order order = new Order();
        order.setSide(OrderSide.BUY);
        order.setType(OrderType.MARKET);
        order.setQty(new BigDecimal("10"));
        order.setTif(Tif.IOC);
        order.setStatus(OrderStatus.FILLED);
        order.setVenue("NSE");
        order.setTradingAccount(tradingAccountA);
        order.setInstrument(instrument);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        orderRepository.saveAndFlush(order);

        // Broker Admin A should see this order
        mockMvc
            .perform(get(BROKER_ADMIN_URL + "/orders"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].side").value("BUY"))
            .andExpect(jsonPath("$[0].qty").value(10));
    }

    /**
     * T023: Test that Broker Admin cannot see orders from other brokers
     */
    @Test
    @Transactional
    void testBrokerAdminCannotViewOtherBrokerOrders() throws Exception {
        // Create an order for Trader B (under Broker B)
        Order order = new Order();
        order.setSide(OrderSide.BUY);
        order.setType(OrderType.MARKET);
        order.setQty(new BigDecimal("10"));
        order.setTif(Tif.IOC);
        order.setStatus(OrderStatus.FILLED);
        order.setVenue("NSE");
        order.setTradingAccount(tradingAccountB);
        order.setInstrument(instrument);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        orderRepository.saveAndFlush(order);

        // Broker Admin A should not see this order from Broker B
        mockMvc
            .perform(get(BROKER_ADMIN_URL + "/orders"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * T023: Test that Broker Admin can view positions for their broker's traders
     */
    @Test
    @Transactional
    void testBrokerAdminCanViewOwnBrokerPositions() throws Exception {
        // Create a position for Trader A (under Broker A)
        Position position = new Position();
        position.setQty(new BigDecimal("10"));
        position.setAvgCost(new BigDecimal("100.00"));
        position.setTradingAccount(tradingAccountA);
        position.setInstrument(instrument);
        position.setCreatedAt(Instant.now());
        position.setUpdatedAt(Instant.now());
        positionRepository.saveAndFlush(position);

        // Broker Admin A should see this position
        mockMvc
            .perform(get(BROKER_ADMIN_URL + "/positions"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].qty").value(10))
            .andExpect(jsonPath("$[0].avgCost").value(100));
    }

    /**
     * T023: Test that Broker Admin cannot see positions from other brokers
     */
    @Test
    @Transactional
    void testBrokerAdminCannotViewOtherBrokerPositions() throws Exception {
        // Create a position for Trader B (under Broker B)
        Position position = new Position();
        position.setQty(new BigDecimal("10"));
        position.setAvgCost(new BigDecimal("100.00"));
        position.setTradingAccount(tradingAccountB);
        position.setInstrument(instrument);
        position.setCreatedAt(Instant.now());
        position.setUpdatedAt(Instant.now());
        positionRepository.saveAndFlush(position);

        // Broker Admin A should not see this position from Broker B
        mockMvc
            .perform(get(BROKER_ADMIN_URL + "/positions"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * T023: Test that Broker Admin can view ledger entries for their broker's traders
     */
    @Test
    @Transactional
    void testBrokerAdminCanViewOwnBrokerLedgerEntries() throws Exception {
        // Create a ledger entry for Trader A (under Broker A)
        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setAmount(new BigDecimal("1000.00"));
        ledgerEntry.setType(LedgerEntryType.DEBIT);
        ledgerEntry.setDescription("Order BUY 10 @ 100.00");
        ledgerEntry.setTradingAccount(tradingAccountA);
        ledgerEntry.setCreatedAt(Instant.now());
        ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Broker Admin A should see this ledger entry
        mockMvc
            .perform(get(BROKER_ADMIN_URL + "/ledger-entries"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].amount").value(1000))
            .andExpect(jsonPath("$[0].type").value("DEBIT"));
    }

    /**
     * T023: Test that Broker Admin cannot see ledger entries from other brokers
     */
    @Test
    @Transactional
    void testBrokerAdminCannotViewOtherBrokerLedgerEntries() throws Exception {
        // Create a ledger entry for Trader B (under Broker B)
        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setAmount(new BigDecimal("1000.00"));
        ledgerEntry.setType(LedgerEntryType.DEBIT);
        ledgerEntry.setDescription("Order BUY 10 @ 100.00");
        ledgerEntry.setTradingAccount(tradingAccountB);
        ledgerEntry.setCreatedAt(Instant.now());
        ledgerEntryRepository.saveAndFlush(ledgerEntry);

        // Broker Admin A should not see this ledger entry from Broker B
        mockMvc
            .perform(get(BROKER_ADMIN_URL + "/ledger-entries"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * T023: Test that non-BROKER_ADMIN users cannot access portfolio endpoints
     */
    @Test
    @Transactional
    @WithMockUser(roles = "USER")
    void testNonBrokerAdminCannotAccessPortfolio() throws Exception {
        mockMvc.perform(get(BROKER_ADMIN_URL + "/orders")).andExpect(status().isForbidden());

        mockMvc.perform(get(BROKER_ADMIN_URL + "/positions")).andExpect(status().isForbidden());

        mockMvc.perform(get(BROKER_ADMIN_URL + "/ledger-entries")).andExpect(status().isForbidden());
    }
}
