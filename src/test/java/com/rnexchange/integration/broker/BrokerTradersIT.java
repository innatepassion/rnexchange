package com.rnexchange.integration.broker;

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
class BrokerTradersIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = { "BROKER_ADMIN" })
    void list_shouldReturnPagedPayloadAnd200_forBrokerAdmin() throws Exception {
        var mvcResult = mockMvc
            .perform(
                get("/api/broker/traders").param("page", "0").param("size", "5").param("brokerId", "1").accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        assertThat(root.has("content")).isTrue();
        assertThat(root.has("page")).isTrue();
        assertThat(root.has("size")).isTrue();
        assertThat(root.has("totalElements")).isTrue();
    }

    @Test
    @WithMockUser(username = "admin", roles = { "BROKER_ADMIN" })
    void details_shouldReturnDetailsShapeAnd200_forBrokerAdmin() throws Exception {
        Long traderId = 1L;
        var mvcResult = mockMvc
            .perform(get("/api/broker/traders/{traderId}", traderId).param("brokerId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        assertThat(root.has("summary")).isTrue();
        assertThat(root.has("recentLedger")).isTrue();
    }

    @Test
    void list_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc
            .perform(get("/api/broker/traders").param("brokerId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }
}
