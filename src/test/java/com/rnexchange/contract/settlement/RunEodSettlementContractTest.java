package com.rnexchange.contract.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class RunEodSettlementContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAcceptEodRequestAndReturnSettlementBatch() throws Exception {
        LocalDate tradeDate = LocalDate.now();
        String dateParam = tradeDate.toString();

        var mvcResult = mockMvc
            .perform(post("/api/settlements/eod").param("date", dateParam).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("id")).isTrue();
        assertThat(root.has("refDate")).isTrue();
        assertThat(root.has("kind")).isTrue();
        assertThat(root.has("status")).isTrue();
        assertThat(root.path("kind").asText()).isEqualTo("EOD");
    }

    @Test
    void shouldRejectRequestWithoutExchangeOperatorRole() throws Exception {
        LocalDate tradeDate = LocalDate.now();
        String dateParam = tradeDate.toString();

        mockMvc
            .perform(post("/api/settlements/eod").param("date", dateParam).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
}
