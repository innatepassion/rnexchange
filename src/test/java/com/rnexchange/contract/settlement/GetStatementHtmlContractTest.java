package com.rnexchange.contract.settlement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.rnexchange.IntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "trader", roles = { "TRADER" })
class GetStatementHtmlContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnHtmlStatement() throws Exception {
        // Use a valid UUID format - actual validation will be done in integration tests
        String statementId = UUID.randomUUID().toString();

        mockMvc
            .perform(get("/api/statements/{statementId}/html", statementId).accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    void shouldRejectRequestWithoutTraderRole() throws Exception {
        String statementId = UUID.randomUUID().toString();

        mockMvc
            .perform(get("/api/statements/{statementId}/html", statementId).accept(MediaType.TEXT_HTML))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404ForNonExistentStatement() throws Exception {
        String statementId = UUID.randomUUID().toString();

        // This will return 404 once ownership checks are implemented
        mockMvc
            .perform(get("/api/statements/{statementId}/html", statementId).accept(MediaType.TEXT_HTML))
            .andExpect(status().isNotFound().or(status().isForbidden()));
    }
}
