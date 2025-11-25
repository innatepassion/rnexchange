package com.rnexchange.integration.broker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rnexchange.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class BrokerOverviewIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectWhenNotAuthenticated() throws Exception {
        mockMvc
            .perform(get("/api/broker/overview").param("brokerId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }
}
