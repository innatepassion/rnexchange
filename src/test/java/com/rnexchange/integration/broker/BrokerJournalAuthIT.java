package com.rnexchange.integration.broker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rnexchange.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class BrokerJournalAuthIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
        String body =
            """
            { "direction": "credit", "amount": 10.00, "reason": "auth test" }
            """;
        mockMvc
            .perform(
                post("/api/broker/traders/{tradingAccountId}/journal", "00000000-0000-0000-0000-000000000000")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    void shouldReturn403WhenNotBrokerAdmin() throws Exception {
        String body =
            """
            { "direction": \"debit\", \"amount\": 5.00, \"reason\": \"forbidden test\" }
            """;
        mockMvc
            .perform(
                post("/api/broker/traders/{tradingAccountId}/journal", "00000000-0000-0000-0000-000000000000")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isForbidden());
    }
}
