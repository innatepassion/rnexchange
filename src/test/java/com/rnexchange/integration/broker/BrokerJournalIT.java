package com.rnexchange.integration.broker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Broker;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.AccountType;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.repository.BrokerRepository;
import com.rnexchange.repository.LedgerEntryRepository;
import com.rnexchange.repository.TradingAccountRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = { "BROKER_ADMIN" })
class BrokerJournalIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TradingAccountRepository tradingAccountRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private BrokerRepository brokerRepository;

    private BigDecimal startingBalance;
    private Long accountId;

    @BeforeEach
    void setup() {
        TradingAccount acc = tradingAccountRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseGet(() -> {
                Broker broker = brokerRepository.findAll().stream().findFirst().orElseThrow();
                TradingAccount ta = new TradingAccount();
                ta.setBroker(broker);
                ta.setType(AccountType.CASH);
                ta.setBaseCcy(Currency.USD);
                ta.setBalance(new BigDecimal("1000.00"));
                ta.setStatus(AccountStatus.ACTIVE);
                return tradingAccountRepository.saveAndFlush(ta);
            });
        startingBalance = acc.getBalance();
        accountId = acc.getId();
    }

    @Test
    @Transactional
    void journalCreditUpdatesBalanceAndCreatesLedger() throws Exception {
        long before = ledgerEntryRepository.count();

        String idempotencyKey = "it-" + UUID.randomUUID();
        String body =
            """
            { "direction": "credit", "amount": 50.00, "reason": "Top-up" }
            """;

        mockMvc
            .perform(
                post("/api/broker/traders/{tradingAccountId}/journal", accountId)
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk());

        TradingAccount afterAcc = tradingAccountRepository.findById(accountId).orElseThrow();
        assertThat(afterAcc.getBalance()).isEqualByComparingTo(startingBalance.add(new BigDecimal("50.00")));
        assertThat(ledgerEntryRepository.count()).isEqualTo(before + 1);
    }
}
