package com.rnexchange.integration.settlement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rnexchange.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for RBAC and negative-path scenarios for settlement endpoints.
 * Verifies that unauthorized/forbidden access is properly handled for all settlement-related endpoints.
 */
@IntegrationTest
@AutoConfigureMockMvc
class SettlementRbacIT {

    @Autowired
    private MockMvc mockMvc;

    // ========== SettlementResource (EXCHANGE_OPERATOR only) ==========

    @Test
    void testRunEodUnauthenticated() throws Exception {
        mockMvc
            .perform(post("/api/settlements/eod").param("date", "2025-01-15").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testRunEodAsTrader() throws Exception {
        mockMvc
            .perform(post("/api/settlements/eod").param("date", "2025-01-15").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testRunEodAsBrokerAdmin() throws Exception {
        mockMvc
            .perform(post("/api/settlements/eod").param("date", "2025-01-15").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void testListSettlementsUnauthenticated() throws Exception {
        mockMvc
            .perform(get("/api/settlements").param("from", "2025-01-01").param("to", "2025-01-31").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testListSettlementsAsTrader() throws Exception {
        mockMvc
            .perform(get("/api/settlements").param("from", "2025-01-01").param("to", "2025-01-31").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testListSettlementsAsBrokerAdmin() throws Exception {
        mockMvc
            .perform(get("/api/settlements").param("from", "2025-01-01").param("to", "2025-01-31").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    // ========== StatementResource (TRADER only) ==========

    @Test
    void testListStatementsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/statements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testListStatementsAsExchangeOperator() throws Exception {
        mockMvc.perform(get("/api/statements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testListStatementsAsBrokerAdmin() throws Exception {
        mockMvc.perform(get("/api/statements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    void testGetStatementHtmlUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/statements/1/html").accept(MediaType.TEXT_HTML)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testGetStatementHtmlAsExchangeOperator() throws Exception {
        mockMvc.perform(get("/api/statements/1/html").accept(MediaType.TEXT_HTML)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testGetStatementHtmlAsBrokerAdmin() throws Exception {
        mockMvc.perform(get("/api/statements/1/html").accept(MediaType.TEXT_HTML)).andExpect(status().isForbidden());
    }

    // ========== BrokerSettlementResource (BROKER_ADMIN only) ==========

    @Test
    void testListBrokerSettlementsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testListBrokerSettlementsAsExchangeOperator() throws Exception {
        mockMvc.perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testListBrokerSettlementsAsTrader() throws Exception {
        mockMvc.perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    void testGetBrokerSummaryHtmlUnauthenticated() throws Exception {
        mockMvc
            .perform(get("/api/broker/settlements/1/summary").param("date", "2025-01-15").accept(MediaType.TEXT_HTML))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testGetBrokerSummaryHtmlAsExchangeOperator() throws Exception {
        mockMvc
            .perform(get("/api/broker/settlements/1/summary").param("date", "2025-01-15").accept(MediaType.TEXT_HTML))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testGetBrokerSummaryHtmlAsTrader() throws Exception {
        mockMvc
            .perform(get("/api/broker/settlements/1/summary").param("date", "2025-01-15").accept(MediaType.TEXT_HTML))
            .andExpect(status().isForbidden());
    }
}
