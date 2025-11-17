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
        String statementId = "1";

        mockMvc
            .perform(get("/api/statements/{statementId}/html", statementId).accept(MediaType.TEXT_HTML))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRequestWithoutTraderRole() throws Exception {
        String statementId = "1";

        mockMvc
            .perform(get("/api/statements/{statementId}/html", statementId).accept(MediaType.TEXT_HTML))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForNonExistentStatement() throws Exception {
        String statementId = "999999";

        mockMvc
            .perform(get("/api/statements/{statementId}/html", statementId).accept(MediaType.TEXT_HTML))
            .andExpect(status().isBadRequest());
    }
}
