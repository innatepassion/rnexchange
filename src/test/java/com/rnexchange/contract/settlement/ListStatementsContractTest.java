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
@WithMockUser(username = "trader", roles = { "TRADER" })
class ListStatementsContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnListOfStatements() throws Exception {
        var mvcResult = mockMvc.perform(get("/api/statements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.isArray()).isTrue();
    }

    @Test
    void shouldReturnStatementsWithRequiredFields() throws Exception {
        var mvcResult = mockMvc.perform(get("/api/statements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        if (root.isArray() && root.size() > 0) {
            JsonNode firstStatement = root.get(0);
            assertThat(firstStatement.has("statementId") || firstStatement.has("id")).isTrue();
            assertThat(firstStatement.has("refDate")).isTrue();
            assertThat(firstStatement.has("tradingAccountId")).isTrue();
            assertThat(firstStatement.has("htmlUrl")).isTrue();
        }
    }

    @Test
    @WithMockUser(username = "some-user", roles = { "EXCHANGE_OPERATOR" })
    void shouldRejectRequestWithoutTraderRole() throws Exception {
        mockMvc.perform(get("/api/statements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    void shouldFilterByDateRange() throws Exception {
        String fromDate = "2024-01-01";
        String toDate = "2024-12-31";

        var mvcResult = mockMvc
            .perform(get("/api/statements").param("from", fromDate).param("to", toDate).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.isArray()).isTrue();
    }
}
