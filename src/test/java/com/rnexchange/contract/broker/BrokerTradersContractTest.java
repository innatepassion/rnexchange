package com.rnexchange.contract.broker;

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
@WithMockUser(username = "admin", roles = { "BROKER_ADMIN" })
class BrokerTradersContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnPagedTradersList() throws Exception {
        var mvcResult = mockMvc
            .perform(
                get("/api/broker/traders").param("page", "0").param("size", "10").param("brokerId", "1").accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("content")).isTrue();
        assertThat(root.has("page")).isTrue();
        assertThat(root.has("size")).isTrue();
        assertThat(root.has("totalElements")).isTrue();
    }
}
