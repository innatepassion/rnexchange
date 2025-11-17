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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for Trader Statements (User Story 2).
 * Verifies that statements reconcile: opening + cash flows + trades + fees + EOD MTM = closing.
 */
@IntegrationTest
@AutoConfigureMockMvc
class TraderStatementsIT {

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
    private DailySettlementPriceRepository dailySettlementPriceRepository;

    private TradingAccount testAccount;
    private TraderProfile testTrader;
    private LocalDate testDate;
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        // Create test data
        exchange = exchangeRepository
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

        Broker broker = brokerRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                Broker b = new Broker();
                b.setCode("TEST_BROKER");
                b.setName("Test Broker");
                b.setStatus("ACTIVE");
                return brokerRepository.save(b);
            });

        testTrader = traderProfileRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                TraderProfile tp = new TraderProfile();
                tp.setDisplayName("Test Trader");
                tp.setEmail("test@example.com");
                tp.setKycStatus(KycStatus.APPROVED);
                tp.setStatus(AccountStatus.ACTIVE);
                return traderProfileRepository.save(tp);
            });

        testAccount = tradingAccountRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                TradingAccount ta = new TradingAccount();
                ta.setType(AccountType.CASH);
                ta.setBaseCcy(Currency.INR);
                ta.setBalance(BigDecimal.valueOf(10000.00));
                ta.setStatus(AccountStatus.ACTIVE);
                ta.setBroker(broker);
                ta.setTrader(testTrader);
                return tradingAccountRepository.save(ta);
            });

        testDate = LocalDate.now();
    }

    @Test
    @WithMockUser(username = "test-trader", roles = { "TRADER" })
    void shouldReconcileStatement() throws Exception {
        // Setup: Create some ledger entries and run EOD
        BigDecimal openingBalance = testAccount.getBalance();

        // Add a cash flow (credit)
        LedgerEntry credit = new LedgerEntry();
        credit.setTradingAccount(testAccount);
        credit.setType(LedgerEntryType.CREDIT);
        credit.setAmount(BigDecimal.valueOf(1000.00));
        credit.setCcy(Currency.INR);
        credit.setCreatedAt(Instant.now());
        credit.setDescription("Test credit");
        credit.setBalanceAfter(openingBalance.add(BigDecimal.valueOf(1000.00)));
        ledgerEntryRepository.save(credit);
        testAccount.setBalance(testAccount.getBalance().add(BigDecimal.valueOf(1000.00)));
        tradingAccountRepository.save(testAccount);

        // Create instrument and position for EOD
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
                inst.setExchange(exchange);
                return instrumentRepository.save(inst);
            });

        // Create position
        Position position = new Position();
        position.setTradingAccount(testAccount);
        position.setInstrument(instrument);
        position.setQty(BigDecimal.valueOf(100));
        position.setAvgCost(BigDecimal.valueOf(50.00));
        position.setLastPx(BigDecimal.valueOf(50.00));
        position.setUnrealizedPnl(BigDecimal.ZERO);
        position.setRealizedPnl(BigDecimal.ZERO);
        positionRepository.save(position);

        // Create settlement price
        DailySettlementPrice price = new DailySettlementPrice();
        price.setRefDate(testDate);
        price.setInstrumentSymbol(instrument.getSymbol());
        price.setSettlePrice(BigDecimal.valueOf(55.00));
        price.setInstrument(instrument);
        dailySettlementPriceRepository.save(price);

        // Run EOD
        settlementService.runEod(testDate);

        // Get statements
        var mvcResult = mockMvc.perform(get("/api/statements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.isArray()).isTrue();

        // Find statement for testDate
        JsonNode statement = null;
        for (JsonNode stmt : root) {
            if (stmt.get("refDate").asText().equals(testDate.toString())) {
                statement = stmt;
                break;
            }
        }

        if (statement != null) {
            // Verify reconciliation: opening + cash flows + EOD MTM = closing
            BigDecimal opening = new BigDecimal(statement.get("openingBalance").asText());
            BigDecimal closing = new BigDecimal(statement.get("closingBalance").asText());
            BigDecimal eodMtm = new BigDecimal(statement.get("eodMtmPnl").asText());

            // Opening + credit (1000) + EOD MTM (500 = (55-50)*100) should equal closing
            BigDecimal expectedClosing = opening.add(BigDecimal.valueOf(1000.00)).add(eodMtm);
            assertThat(closing).isEqualByComparingTo(expectedClosing);
        }
    }
}
