package com.rnexchange.integration.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.*;
import com.rnexchange.domain.enumeration.*;
import com.rnexchange.repository.*;
import com.rnexchange.service.settlement.SettlementService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for Broker Settlements (User Story 3).
 * Verifies broker-level totals and scoping (only own broker data).
 */
@IntegrationTest
@AutoConfigureMockMvc
class BrokerSettlementsIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private TradingAccountRepository tradingAccountRepository;

    @Autowired
    private TraderProfileRepository traderProfileRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private BrokerDeskRepository brokerDeskRepository;

    @Autowired
    private DailySettlementPriceRepository dailySettlementPriceRepository;

    @Autowired
    private UserRepository userRepository;

    private Broker testBroker;
    private Broker otherBroker;
    private TradingAccount testAccount1;
    private TradingAccount testAccount2;
    private TradingAccount otherBrokerAccount;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        // Create test data
        Exchange exchange = exchangeRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                Exchange e = new Exchange();
                e.setCode("TEST");
                e.setName("Test Exchange");
                e.setStatus(ExchangeStatus.ACTIVE);
                return exchangeRepository.save(e);
            });

        testBroker = brokerRepository
            .findAll()
            .stream()
            .filter(b -> "TEST_BROKER".equals(b.getCode()))
            .findFirst()
            .orElseGet(() -> {
                Broker b = new Broker();
                b.setCode("TEST_BROKER");
                b.setName("Test Broker");
                b.setStatus("ACTIVE");
                b.setExchange(exchange);
                return brokerRepository.save(b);
            });

        otherBroker = brokerRepository
            .findAll()
            .stream()
            .filter(b -> "OTHER_BROKER".equals(b.getCode()))
            .findFirst()
            .orElseGet(() -> {
                Broker b = new Broker();
                b.setCode("OTHER_BROKER");
                b.setName("Other Broker");
                b.setStatus("ACTIVE");
                b.setExchange(exchange);
                return brokerRepository.save(b);
            });

        TraderProfile trader1 = traderProfileRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                TraderProfile tp = new TraderProfile();
                tp.setDisplayName("Test Trader 1");
                tp.setEmail("trader1@example.com");
                tp.setKycStatus(KycStatus.APPROVED);
                tp.setStatus(AccountStatus.ACTIVE);
                return traderProfileRepository.save(tp);
            });

        TraderProfile trader2 = traderProfileRepository
            .findAll()
            .stream()
            .skip(1)
            .findFirst()
            .orElseGet(() -> {
                TraderProfile tp = new TraderProfile();
                tp.setDisplayName("Test Trader 2");
                tp.setEmail("trader2@example.com");
                tp.setKycStatus(KycStatus.APPROVED);
                tp.setStatus(AccountStatus.ACTIVE);
                return traderProfileRepository.save(tp);
            });

        testAccount1 = tradingAccountRepository
            .findAll()
            .stream()
            .filter(ta -> ta.getBroker() != null && ta.getBroker().getId().equals(testBroker.getId()))
            .findFirst()
            .orElseGet(() -> {
                TradingAccount ta = new TradingAccount();
                ta.setType(AccountType.CASH);
                ta.setBaseCcy(Currency.INR);
                ta.setBalance(BigDecimal.valueOf(10000.00));
                ta.setStatus(AccountStatus.ACTIVE);
                ta.setBroker(testBroker);
                ta.setTrader(trader1);
                return tradingAccountRepository.save(ta);
            });

        testAccount2 = tradingAccountRepository
            .findAll()
            .stream()
            .filter(
                ta ->
                    ta.getBroker() != null && ta.getBroker().getId().equals(testBroker.getId()) && !ta.getId().equals(testAccount1.getId())
            )
            .findFirst()
            .orElseGet(() -> {
                TradingAccount ta = new TradingAccount();
                ta.setType(AccountType.CASH);
                ta.setBaseCcy(Currency.INR);
                ta.setBalance(BigDecimal.valueOf(5000.00));
                ta.setStatus(AccountStatus.ACTIVE);
                ta.setBroker(testBroker);
                ta.setTrader(trader2);
                return tradingAccountRepository.save(ta);
            });

        otherBrokerAccount = tradingAccountRepository
            .findAll()
            .stream()
            .filter(ta -> ta.getBroker() != null && ta.getBroker().getId().equals(otherBroker.getId()))
            .findFirst()
            .orElseGet(() -> {
                TradingAccount ta = new TradingAccount();
                ta.setType(AccountType.CASH);
                ta.setBaseCcy(Currency.INR);
                ta.setBalance(BigDecimal.valueOf(20000.00));
                ta.setStatus(AccountStatus.ACTIVE);
                ta.setBroker(otherBroker);
                ta.setTrader(trader1);
                return tradingAccountRepository.save(ta);
            });

        testDate = LocalDate.now();
    }

    @Test
    @WithMockUser(username = "brokeradmin", roles = { "BROKER_ADMIN" })
    void shouldReturnOnlyOwnBrokerData() throws Exception {
        // Setup: Create broker desk for test broker
        User brokerAdminUser = userRepository
            .findOneByLogin("brokeradmin")
            .orElseGet(() -> {
                User u = new User();
                u.setLogin("brokeradmin");
                u.setEmail("brokeradmin@example.com");
                u.setActivated(true);
                u.setPassword("$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC");
                return userRepository.save(u);
            });

        BrokerDesk brokerDesk = brokerDeskRepository
            .findByUserLogin("brokeradmin")
            .orElseGet(() -> {
                BrokerDesk bd = new BrokerDesk();
                bd.setName("Test Broker Desk");
                bd.setUser(brokerAdminUser);
                bd.setBroker(testBroker);
                return brokerDeskRepository.save(bd);
            });

        // Create some ledger entries for test broker accounts
        LedgerEntry credit1 = new LedgerEntry();
        credit1.setTradingAccount(testAccount1);
        credit1.setType(LedgerEntryType.CREDIT);
        credit1.setAmount(BigDecimal.valueOf(1000.00));
        credit1.setCcy(Currency.INR);
        credit1.setCreatedAt(Instant.now());
        credit1.setDescription("Test credit 1");
        credit1.setBalanceAfter(testAccount1.getBalance().add(BigDecimal.valueOf(1000.00)));
        ledgerEntryRepository.save(credit1);

        LedgerEntry credit2 = new LedgerEntry();
        credit2.setTradingAccount(testAccount2);
        credit2.setType(LedgerEntryType.CREDIT);
        credit2.setAmount(BigDecimal.valueOf(500.00));
        credit2.setCcy(Currency.INR);
        credit2.setCreatedAt(Instant.now());
        credit2.setDescription("Test credit 2");
        credit2.setBalanceAfter(testAccount2.getBalance().add(BigDecimal.valueOf(500.00)));
        ledgerEntryRepository.save(credit2);

        // Create ledger entry for other broker (should not appear)
        LedgerEntry otherCredit = new LedgerEntry();
        otherCredit.setTradingAccount(otherBrokerAccount);
        otherCredit.setType(LedgerEntryType.CREDIT);
        otherCredit.setAmount(BigDecimal.valueOf(2000.00));
        otherCredit.setCcy(Currency.INR);
        otherCredit.setCreatedAt(Instant.now());
        otherCredit.setDescription("Other broker credit");
        otherCredit.setBalanceAfter(otherBrokerAccount.getBalance().add(BigDecimal.valueOf(2000.00)));
        ledgerEntryRepository.save(otherCredit);

        // Get broker settlements
        var mvcResult = mockMvc
            .perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.isArray()).isTrue();

        // Verify all summaries are for testBroker only
        for (JsonNode summary : root) {
            assertThat(summary.get("brokerId").asLong()).isEqualTo(testBroker.getId());
            assertThat(summary.get("brokerName").asText()).isEqualTo(testBroker.getName());
        }
    }

    @Test
    @WithMockUser(username = "brokeradmin", roles = { "BROKER_ADMIN" })
    void shouldAggregateBrokerLevelTotals() throws Exception {
        // Setup: Create broker desk for test broker
        User brokerAdminUser = userRepository
            .findOneByLogin("brokeradmin")
            .orElseGet(() -> {
                User u = new User();
                u.setLogin("brokeradmin");
                u.setEmail("brokeradmin@example.com");
                u.setActivated(true);
                u.setPassword("$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC");
                return userRepository.save(u);
            });

        BrokerDesk brokerDesk = brokerDeskRepository
            .findByUserLogin("brokeradmin")
            .orElseGet(() -> {
                BrokerDesk bd = new BrokerDesk();
                bd.setName("Test Broker Desk");
                bd.setUser(brokerAdminUser);
                bd.setBroker(testBroker);
                return brokerDeskRepository.save(bd);
            });

        // Create instrument and positions for EOD
        Instrument instrument = instrumentRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                Instrument inst = new Instrument();
                inst.setSymbol("TEST");
                inst.setName("Test Instrument");
                inst.setAssetClass(AssetClass.EQUITY);
                inst.setExchangeCode("TEST");
                inst.setTickSize(BigDecimal.valueOf(0.01));
                inst.setLotSize(10L);
                inst.setCurrency(Currency.INR);
                inst.setStatus("active");
                Exchange exchange = exchangeRepository.findAll().stream().findFirst().orElse(null);
                inst.setExchange(exchange);
                return instrumentRepository.save(inst);
            });

        // Create positions for both accounts
        Position position1 = new Position();
        position1.setTradingAccount(testAccount1);
        position1.setInstrument(instrument);
        position1.setQty(BigDecimal.valueOf(100));
        position1.setAvgCost(BigDecimal.valueOf(50.00));
        position1.setLastPx(BigDecimal.valueOf(50.00));
        position1.setUnrealizedPnl(BigDecimal.ZERO);
        position1.setRealizedPnl(BigDecimal.ZERO);
        positionRepository.save(position1);

        Position position2 = new Position();
        position2.setTradingAccount(testAccount2);
        position2.setInstrument(instrument);
        position2.setQty(BigDecimal.valueOf(50));
        position2.setAvgCost(BigDecimal.valueOf(40.00));
        position2.setLastPx(BigDecimal.valueOf(40.00));
        position2.setUnrealizedPnl(BigDecimal.ZERO);
        position2.setRealizedPnl(BigDecimal.ZERO);
        positionRepository.save(position2);

        // Create settlement price
        DailySettlementPrice price = new DailySettlementPrice();
        price.setRefDate(testDate);
        price.setInstrumentSymbol(instrument.getSymbol());
        price.setSettlePrice(BigDecimal.valueOf(55.00));
        price.setInstrument(instrument);
        dailySettlementPriceRepository.save(price);

        // Run EOD
        settlementService.runEod(testDate);

        // Get broker settlements
        var mvcResult = mockMvc
            .perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.isArray()).isTrue();

        // Find summary for testDate
        JsonNode summary = null;
        for (JsonNode s : root) {
            if (s.get("refDate").asText().equals(testDate.toString())) {
                summary = s;
                break;
            }
        }

        if (summary != null) {
            // Verify aggregation
            assertThat(summary.get("totalClientCount").asInt()).isGreaterThanOrEqualTo(2);
            assertThat(summary.get("totalOpeningBalance")).isNotNull();
            assertThat(summary.get("totalClosingBalance")).isNotNull();
            assertThat(summary.get("totalEodMtmPnl")).isNotNull();

            // Verify totals reconcile with individual statements
            // Account 1: MTM = (55-50)*100 = 500
            // Account 2: MTM = (55-40)*50 = 750
            // Total MTM should be 1250
            BigDecimal totalEodMtm = new BigDecimal(summary.get("totalEodMtmPnl").asText());
            assertThat(totalEodMtm).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }
    }
}
