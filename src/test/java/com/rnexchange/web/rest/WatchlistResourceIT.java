package com.rnexchange.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.Watchlist;
import com.rnexchange.domain.WatchlistItem;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.repository.WatchlistRepository;
import com.rnexchange.service.marketdata.WatchlistAuthorizationService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class WatchlistResourceIT {

    private static final String BASE_URL = "/api/watchlists";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private TraderProfileRepository traderProfileRepository;

    @Autowired
    private WatchlistAuthorizationService authorizationService;

    private TraderProfile traderOne;
    private TraderProfile traderTwo;

    @BeforeEach
    void setUp() {
        authorizationService.reset();
        traderOne = traderProfileRepository.findOneByUserLogin("trader-one").orElseThrow();
        traderTwo = traderProfileRepository.findOneByUserLogin("trader-two").orElseThrow();
        watchlistRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "trader-one", authorities = "TRADER")
    void shouldReturnWatchlistSummaries() throws Exception {
        createWatchlist(traderOne, "Primary", List.of("RELIANCE", "INFY"));

        mockMvc
            .perform(get(BASE_URL).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Primary"))
            .andExpect(jsonPath("$[0].symbolCount").value(2));

        assertThat(authorizationService.getGrantedSymbols("trader-one")).containsExactlyInAnyOrder("RELIANCE", "INFY");
    }

    @Test
    @WithMockUser(username = "trader-one", authorities = "TRADER")
    void shouldReturnSingleWatchlist() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("RELIANCE"));

        mockMvc
            .perform(get(BASE_URL + "/{id}", watchlist.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Primary"))
            .andExpect(jsonPath("$.items[0].symbol").value("RELIANCE"));
    }

    @Test
    @WithMockUser(username = "trader-one", authorities = "TRADER")
    void shouldAddSymbolToWatchlist() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY"));
        String payload =
            """
            {"symbol":"RELIANCE"}
            """;

        mockMvc
            .perform(post(BASE_URL + "/{id}/items", watchlist.getId()).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[1].symbol").value("RELIANCE"));

        assertThat(authorizationService.getGrantedSymbols("trader-one")).contains("RELIANCE");
    }

    @Test
    @WithMockUser(username = "trader-one", authorities = "TRADER")
    void shouldRejectDuplicateSymbol() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY"));
        String payload =
            """
            {"symbol":"INFY"}
            """;

        mockMvc
            .perform(post(BASE_URL + "/{id}/items", watchlist.getId()).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "trader-one", authorities = "TRADER")
    void shouldRejectUnknownSymbol() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of());
        String payload =
            """
            {"symbol":"UNKNOWNX"}
            """;

        mockMvc
            .perform(post(BASE_URL + "/{id}/items", watchlist.getId()).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "trader-one", authorities = "TRADER")
    void shouldRemoveSymbolFromWatchlist() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY", "RELIANCE"));

        mockMvc
            .perform(delete(BASE_URL + "/{id}/items/{symbol}", watchlist.getId(), "INFY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].symbol").value("RELIANCE"));

        assertThat(authorizationService.getGrantedSymbols("trader-one")).containsExactly("RELIANCE");
    }

    @Test
    @WithMockUser(username = "trader-one", authorities = "TRADER")
    void removalOfMissingSymbolReturnsNotFound() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY"));

        mockMvc.perform(delete(BASE_URL + "/{id}/items/{symbol}", watchlist.getId(), "RELIANCE")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "trader-two", authorities = "TRADER")
    void traderCannotModifyAnotherWatchlist() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY"));
        String payload =
            """
            {"symbol":"RELIANCE"}
            """;

        mockMvc
            .perform(post(BASE_URL + "/{id}/items", watchlist.getId()).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "trader-one", authorities = "TRADER")
    void authorizationSetUpdatesAfterAddAndRemove() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY"));

        mockMvc
            .perform(
                post(BASE_URL + "/{id}/items", watchlist.getId()).contentType(MediaType.APPLICATION_JSON).content("{\"symbol\":\"TCS\"}")
            )
            .andExpect(status().isOk());
        assertThat(authorizationService.getGrantedSymbols("trader-one")).containsExactlyInAnyOrder("INFY", "TCS");

        mockMvc.perform(delete(BASE_URL + "/{id}/items/{symbol}", watchlist.getId(), "INFY")).andExpect(status().isOk());
        assertThat(authorizationService.getGrantedSymbols("trader-one")).containsExactly("TCS");
    }

    private Watchlist createWatchlist(TraderProfile owner, String name, List<String> symbols) {
        Watchlist watchlist = new Watchlist().name(name).traderProfile(owner);
        AtomicInteger order = new AtomicInteger();
        symbols.stream().map(symbol -> new WatchlistItem().symbol(symbol).sortOrder(order.getAndIncrement())).forEach(watchlist::addItem);
        return watchlistRepository.saveAndFlush(watchlist);
    }
}
