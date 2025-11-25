package com.rnexchange.integration.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rnexchange.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for RBAC negative-path scenarios across all M6 endpoints.
 * Verifies that unauthorized/forbidden access is properly handled when users attempt
 * actions outside their role permissions.
 *
 * <p>M6 Phase 8 (T056): Comprehensive negative-path testing for RBAC enforcement.
 * Covers all new or modified endpoints from User Stories 1-5.</p>
 *
 * <p>Edge cases tested:
 * <ul>
 *   <li>Unauthenticated users attempting authenticated endpoints</li>
 *   <li>TRADER attempting BROKER_ADMIN or EXCHANGE_OPERATOR actions</li>
 *   <li>BROKER_ADMIN attempting TRADER or EXCHANGE_OPERATOR actions</li>
 *   <li>EXCHANGE_OPERATOR attempting TRADER or BROKER_ADMIN actions</li>
 * </ul>
 * </p>
 */
@IntegrationTest
@AutoConfigureMockMvc
class RbacNegativePathIT {

    private static final String LEDGER_ENTRY_REQUEST =
        """
        {
          "createdAt": "2024-01-15T09:00:00Z",
          "type": "CREDIT",
          "amount": 100.00,
          "ccy": "INR",
          "tradingAccount": { "id": 1 }
        }
        """;

    @Autowired
    private MockMvc mockMvc;

    // ========== LedgerEntryResource (BROKER_ADMIN only for POST) ==========

    @Test
    void testCreateLedgerEntryUnauthenticated() throws Exception {
        mockMvc
            .perform(post("/api/ledger-entries").contentType(MediaType.APPLICATION_JSON).content(LEDGER_ENTRY_REQUEST))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testCreateLedgerEntryAsTrader() throws Exception {
        mockMvc
            .perform(post("/api/ledger-entries").contentType(MediaType.APPLICATION_JSON).content(LEDGER_ENTRY_REQUEST))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testCreateLedgerEntryAsExchangeOperator() throws Exception {
        mockMvc
            .perform(post("/api/ledger-entries").contentType(MediaType.APPLICATION_JSON).content(LEDGER_ENTRY_REQUEST))
            .andExpect(status().isForbidden());
    }

    // ========== WatchlistResource (TRADER only) ==========

    @Test
    void testGetWatchlistsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/watchlists").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testGetWatchlistsAsBrokerAdmin() throws Exception {
        mockMvc.perform(get("/api/watchlists").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testGetWatchlistsAsExchangeOperator() throws Exception {
        mockMvc.perform(get("/api/watchlists").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testAddWatchlistItemAsBrokerAdmin() throws Exception {
        String body =
            """
            { "symbol": "RELIANCE" }
            """;

        mockMvc
            .perform(post("/api/watchlists/1/items").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testRemoveWatchlistItemAsExchangeOperator() throws Exception {
        mockMvc.perform(delete("/api/watchlists/1/items/RELIANCE").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

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
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testListStatementsAsBrokerAdmin() throws Exception {
        mockMvc.perform(get("/api/statements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testListStatementsAsExchangeOperator() throws Exception {
        mockMvc.perform(get("/api/statements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    void testGetStatementHtmlUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/statements/1/html").accept(MediaType.TEXT_HTML)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testGetStatementHtmlAsBrokerAdmin() throws Exception {
        mockMvc.perform(get("/api/statements/1/html").accept(MediaType.TEXT_HTML)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testGetStatementHtmlAsExchangeOperator() throws Exception {
        mockMvc.perform(get("/api/statements/1/html").accept(MediaType.TEXT_HTML)).andExpect(status().isForbidden());
    }

    // ========== BrokerSettlementResource (BROKER_ADMIN only) ==========

    @Test
    void testListBrokerSettlementsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testListBrokerSettlementsAsTrader() throws Exception {
        mockMvc.perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testListBrokerSettlementsAsExchangeOperator() throws Exception {
        mockMvc.perform(get("/api/broker/settlements").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    void testGetBrokerSummaryHtmlUnauthenticated() throws Exception {
        mockMvc
            .perform(get("/api/broker/settlements/1/summary").param("date", "2025-01-15").accept(MediaType.TEXT_HTML))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testGetBrokerSummaryHtmlAsTrader() throws Exception {
        mockMvc
            .perform(get("/api/broker/settlements/1/summary").param("date", "2025-01-15").accept(MediaType.TEXT_HTML))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testGetBrokerSummaryHtmlAsExchangeOperator() throws Exception {
        mockMvc
            .perform(get("/api/broker/settlements/1/summary").param("date", "2025-01-15").accept(MediaType.TEXT_HTML))
            .andExpect(status().isForbidden());
    }

    // ========== BrokerJournalResource (BROKER_ADMIN only) ==========

    @Test
    void testCreateBrokerJournalUnauthenticated() throws Exception {
        String body =
            """
            { "direction": "credit", "amount": 100.00, "reason": "test" }
            """;

        mockMvc
            .perform(post("/api/broker/traders/{tradingAccountId}/journal", 1L).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testCreateBrokerJournalAsTrader() throws Exception {
        String body =
            """
            { "direction": "credit", "amount": 100.00, "reason": "test" }
            """;

        mockMvc
            .perform(post("/api/broker/traders/{tradingAccountId}/journal", 1L).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testCreateBrokerJournalAsExchangeOperator() throws Exception {
        String body =
            """
            { "direction": "credit", "amount": 100.00, "reason": "test" }
            """;

        mockMvc
            .perform(post("/api/broker/traders/{tradingAccountId}/journal", 1L).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
    }

    // ========== BrokerOverviewResource (BROKER_ADMIN only) ==========

    @Test
    void testGetBrokerOverviewUnauthenticated() throws Exception {
        mockMvc
            .perform(get("/api/broker/overview").param("brokerId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testGetBrokerOverviewAsTrader() throws Exception {
        mockMvc
            .perform(get("/api/broker/overview").param("brokerId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testGetBrokerOverviewAsExchangeOperator() throws Exception {
        mockMvc
            .perform(get("/api/broker/overview").param("brokerId", "1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    // ========== BrokerTradersResource (BROKER_ADMIN only) ==========

    @Test
    void testListBrokerTradersUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/broker/traders").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testListBrokerTradersAsTrader() throws Exception {
        mockMvc.perform(get("/api/broker/traders").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "EXCHANGE_OPERATOR" })
    void testListBrokerTradersAsExchangeOperator() throws Exception {
        mockMvc.perform(get("/api/broker/traders").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    // ========== MarketDataControlResource (EXCHANGE_OPERATOR only) ==========

    @Test
    void testStartMarketDataFeedUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/marketdata/mock/start").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testStartMarketDataFeedAsTrader() throws Exception {
        mockMvc.perform(post("/api/marketdata/mock/start").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testStartMarketDataFeedAsBrokerAdmin() throws Exception {
        mockMvc.perform(post("/api/marketdata/mock/start").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testStopMarketDataFeedAsTrader() throws Exception {
        mockMvc.perform(post("/api/marketdata/mock/stop").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testGetMarketDataFeedStatusAsBrokerAdmin() throws Exception {
        mockMvc.perform(get("/api/marketdata/mock/status").accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    // ========== BaselineSeedResource (EXCHANGE_OPERATOR only) ==========

    @Test
    void testRunBaselineSeedUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/admin/baseline-seed/run").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "TRADER" })
    void testRunBaselineSeedAsTrader() throws Exception {
        mockMvc.perform(post("/api/admin/baseline-seed/run").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = { "BROKER_ADMIN" })
    void testRunBaselineSeedAsBrokerAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/baseline-seed/run").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }
}
