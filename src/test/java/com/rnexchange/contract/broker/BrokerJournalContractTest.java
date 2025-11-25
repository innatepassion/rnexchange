package com.rnexchange.contract.broker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.repository.TradingAccountRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = { "BROKER_ADMIN" })
class BrokerJournalContractTest {

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
    void shouldAcceptJournalAndReturnResultShape() throws Exception {
        Long tradingAccountId = anyTradingAccountId();
        String idempotencyKey = "test-key-" + UUID.randomUUID();
        String body =
            """
            {
              "direction": "credit",
              "amount": 100.5,
              "reason": "Initial funding"
            }
            """;

        var mvcResult = mockMvc
            .perform(
                post("/api/broker/traders/{tradingAccountId}/journal", tradingAccountId)
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("ledgerEntry")).isTrue();
        assertThat(root.path("ledgerEntry").has("id")).isTrue();
        assertThat(root.path("ledgerEntry").has("type")).isTrue();
        assertThat(root.path("ledgerEntry").has("amount")).isTrue();
        assertThat(root.path("ledgerEntry").has("reason")).isTrue();
        assertThat(root.path("ledgerEntry").has("createdAt")).isTrue();
        assertThat(root.path("ledgerEntry").has("createdByUserId")).isTrue();
        assertThat(root.has("account")).isTrue();
        assertThat(root.path("account").has("tradingAccountId")).isTrue();
        assertThat(root.path("account").has("cash")).isTrue();
    }
}
