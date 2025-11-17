package com.rnexchange.contract.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "exchange-operator", roles = { "EXCHANGE_OPERATOR" })
class ListSettlementBatchesContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnSettlementBatchesList() throws Exception {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        var mvcResult = mockMvc
            .perform(get("/api/settlements").param("from", from.toString()).param("to", to.toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.isArray()).isTrue();

        // If array has elements, verify structure
        if (root.size() > 0) {
            JsonNode first = root.get(0);
            assertThat(first.has("id")).isTrue();
            assertThat(first.has("refDate")).isTrue();
            assertThat(first.has("kind")).isTrue();
            assertThat(first.has("status")).isTrue();
            assertThat(first.has("accountsProcessed")).isTrue();
            assertThat(first.has("positionsProcessed")).isTrue();
            assertThat(first.has("netPnl")).isTrue();
        }
    }

    @Test
    @WithMockUser(username = "some-user", roles = { "TRADER" })
    void shouldRejectRequestWithoutExchangeOperatorRole() throws Exception {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        mockMvc
            .perform(get("/api/settlements").param("from", from.toString()).param("to", to.toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
}
