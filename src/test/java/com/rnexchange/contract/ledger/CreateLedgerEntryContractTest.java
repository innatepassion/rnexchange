package com.rnexchange.contract.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.TradingAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contract test for POST /api/ledger-entries endpoint.
 * Validates payload, status codes, and response shape per M6 User Story 2 (T022).
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "broker_demo", roles = { "BROKER_ADMIN" })
class CreateLedgerEntryContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TradingAccountRepository tradingAccountRepository;

    private Long anyTradingAccountId() {
        return tradingAccountRepository
            .findAll()
            .stream()
            .map(TradingAccount::getId)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No TradingAccount found for contract test"));
    }

    @Test
    void shouldAcceptValidLedgerEntryAndReturn201() throws Exception {
        Long accountId = anyTradingAccountId();
        String body =
            """
            {
              "createdAt": "2024-01-15T10:00:00Z",
              "type": "CREDIT",
              "amount": 100.50,
              "ccy": "USD",
              "description": "Test credit entry",
              "tradingAccount": {
                "id": %d
              }
            }
            """.formatted(accountId);

        var mvcResult = mockMvc
            .perform(post("/api/ledger-entries").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("id")).isTrue();
        assertThat(root.has("type")).isTrue();
        assertThat(root.has("amount")).isTrue();
        assertThat(root.has("ccy")).isTrue();
        assertThat(root.has("description")).isTrue();
        assertThat(root.has("tradingAccount")).isTrue();
    }

    @Test
    void shouldRejectInvalidPayloadWith400() throws Exception {
        String body =
            """
            {
              "type": "INVALID_TYPE",
              "amount": -10.00
            }
            """;

        mockMvc
            .perform(post("/api/ledger-entries").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowNegativeBalanceAfter() throws Exception {
        TradingAccount account = tradingAccountRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No TradingAccount found for contract test"));
        Long accountId = account.getId();
        java.math.BigDecimal debitAmount = account.getBalance().add(new java.math.BigDecimal("50.00"));
        String body =
            """
            {
              "createdAt": "2024-01-15T10:00:00Z",
              "type": "DEBIT",
              "amount": %s,
              "ccy": "USD",
              "description": "Test debit that results in negative balance",
              "balanceAfter": -50.00,
              "tradingAccount": {
                "id": %d
              }
            }
            """.formatted(debitAmount, accountId);

        var mvcResult = mockMvc
            .perform(post("/api/ledger-entries").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("balanceAfter")).isTrue();
        // Negative balance should be allowed
        assertThat(root.path("balanceAfter").asDouble()).isLessThan(0);
    }
}
