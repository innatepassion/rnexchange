package com.rnexchange.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rnexchange.IntegrationTest;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.seed.AbstractBaselineSeedIT;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class BaselineSeedResourceIT extends AbstractBaselineSeedIT {

    @Autowired
    private MockMvc restMockMvc;

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.EXCHANGE_OPERATOR)
    void runBaselineSeed_allowsExchangeOperator() throws Exception {
        restMockMvc.perform(post("/api/admin/baseline-seed/run").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isAccepted());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.BROKER_ADMIN)
    void runBaselineSeed_forbidsBrokerAdmin() throws Exception {
        restMockMvc.perform(post("/api/admin/baseline-seed/run").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.TRADER)
    void runBaselineSeed_forbidsTrader() throws Exception {
        restMockMvc.perform(post("/api/admin/baseline-seed/run").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.EXCHANGE_OPERATOR)
    void getBaselineSeedStatus_allowsExchangeOperator() throws Exception {
        restMockMvc
            .perform(get("/api/admin/baseline-seed/status/{jobId}", UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.BROKER_ADMIN)
    void getBaselineSeedStatus_forbidsBrokerAdmin() throws Exception {
        restMockMvc
            .perform(get("/api/admin/baseline-seed/status/{jobId}", UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.TRADER)
    void getBaselineSeedStatus_forbidsTrader() throws Exception {
        restMockMvc
            .perform(get("/api/admin/baseline-seed/status/{jobId}", UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
}
