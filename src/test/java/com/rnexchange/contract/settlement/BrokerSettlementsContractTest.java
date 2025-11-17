package com.rnexchange.contract.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "brokeradmin", roles = { "BROKER_ADMIN" })
class BrokerSettlementsContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnListOfBrokerSettlements() throws Exception {
        var mvcResult = mockMvc
            .perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.isArray()).isTrue();
    }

    @Test
    void shouldReturnSettlementsWithRequiredFields() throws Exception {
        var mvcResult = mockMvc
            .perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        if (root.isArray() && root.size() > 0) {
            JsonNode firstSummary = root.get(0);
            assertThat(firstSummary.has("refDate")).isTrue();
            assertThat(firstSummary.has("brokerId")).isTrue();
            assertThat(firstSummary.has("brokerName")).isTrue();
            assertThat(firstSummary.has("totalClientCount")).isTrue();
            assertThat(firstSummary.has("totalOpeningBalance")).isTrue();
            assertThat(firstSummary.has("totalClosingBalance")).isTrue();
            assertThat(firstSummary.has("totalEodMtmPnl")).isTrue();
            assertThat(firstSummary.has("summaryUrl")).isTrue();
        }
    }

    @Test
    @WithMockUser(username = "some-user", roles = { "TRADER" })
    void shouldRejectRequestWithoutBrokerAdminRole() throws Exception {
        mockMvc.perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    void shouldFilterByDateRange() throws Exception {
        String fromDate = "2024-01-01";
        String toDate = "2024-12-31";

        var mvcResult = mockMvc
            .perform(get("/api/broker/settlements").param("from", fromDate).param("to", toDate).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.isArray()).isTrue();
    }
}
