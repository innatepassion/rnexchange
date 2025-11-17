package com.rnexchange.contract.broker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.service.dto.BrokerOverviewDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "brokeradmin", roles = { "BROKER_ADMIN" })
class BrokerOverviewContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnOverviewPayloadSchema() throws Exception {
        var mvcResult = mockMvc
            .perform(get("/api/broker/overview").param("brokerId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        BrokerOverviewDTO dto = objectMapper.readValue(json, BrokerOverviewDTO.class);
        assertThat(dto).isNotNull();
        // Basic shape assertions (fields present)
        dto.getActiveTraderCount();
        dto.getTotalCash();
        dto.getTotalEquityExposure();
        dto.getGeneratedAt();
    }
}
