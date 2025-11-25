package com.rnexchange.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.User;
import com.rnexchange.domain.Watchlist;
import com.rnexchange.domain.WatchlistItem;
import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.KycStatus;
import com.rnexchange.repository.TraderProfileRepository;
import com.rnexchange.repository.UserRepository;
import com.rnexchange.repository.WatchlistRepository;
import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.marketdata.WatchlistAuthorizationService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
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
    private UserRepository userRepository;

    @Autowired
    private WatchlistAuthorizationService authorizationService;

    private TraderProfile traderOne;
    private TraderProfile traderTwo;

    @BeforeEach
    void setUp() {
        authorizationService.reset();
        traderOne = ensureTraderProfile("trader-one", "Trader One", "trader.one@rnexchange.test");
        traderTwo = ensureTraderProfile("trader-two", "Trader Two", "trader.two@rnexchange.test");
        watchlistRepository.deleteAll();
    }

    @Test
    void shouldReturnWatchlistSummaries() throws Exception {
        createWatchlist(traderOne, "Primary", List.of("RELIANCE", "INFY"));

        mockMvc
            .perform(get(BASE_URL).with(traderOneJwt()).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Primary"))
            .andExpect(jsonPath("$[0].symbolCount").value(2));

        assertThat(authorizationService.getGrantedSymbols("trader-one")).containsExactlyInAnyOrder("RELIANCE", "INFY");
    }

    @Test
    void shouldReturnSingleWatchlist() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("RELIANCE"));

        mockMvc
            .perform(get(BASE_URL + "/{id}", watchlist.getId()).with(traderOneJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Primary"))
            .andExpect(jsonPath("$.items[0].symbol").value("RELIANCE"));
    }

    @Test
    void shouldAddSymbolToWatchlist() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY"));
        String payload =
            """
            {"symbol":"RELIANCE"}
            """;

        mockMvc
            .perform(
                post(BASE_URL + "/{id}/items", watchlist.getId())
                    .with(traderOneJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[1].symbol").value("RELIANCE"));

        assertThat(authorizationService.getGrantedSymbols("trader-one")).contains("RELIANCE");
    }

    @Test
    void shouldRejectDuplicateSymbol() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY"));
        String payload =
            """
            {"symbol":"INFY"}
            """;

        mockMvc
            .perform(
                post(BASE_URL + "/{id}/items", watchlist.getId())
                    .with(traderOneJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUnknownSymbol() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of());
        String payload =
            """
            {"symbol":"UNKNOWNX"}
            """;

        mockMvc
            .perform(
                post(BASE_URL + "/{id}/items", watchlist.getId())
                    .with(traderOneJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRemoveSymbolFromWatchlist() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY", "RELIANCE"));

        mockMvc
            .perform(delete(BASE_URL + "/{id}/items/{symbol}", watchlist.getId(), "INFY").with(traderOneJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].symbol").value("RELIANCE"));

        assertThat(authorizationService.getGrantedSymbols("trader-one")).containsExactly("RELIANCE");
    }

    @Test
    void removalOfMissingSymbolReturnsNotFound() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY"));

        mockMvc
            .perform(delete(BASE_URL + "/{id}/items/{symbol}", watchlist.getId(), "RELIANCE").with(traderOneJwt()))
            .andExpect(status().isNotFound());
    }

    @Test
    void traderCannotModifyAnotherWatchlist() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY"));
        String payload =
            """
            {"symbol":"RELIANCE"}
            """;

        mockMvc
            .perform(
                post(BASE_URL + "/{id}/items", watchlist.getId())
                    .with(traderTwoJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void authorizationSetUpdatesAfterAddAndRemove() throws Exception {
        Watchlist watchlist = createWatchlist(traderOne, "Primary", List.of("INFY"));

        mockMvc
            .perform(
                post(BASE_URL + "/{id}/items", watchlist.getId())
                    .with(traderOneJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"symbol\":\"TCS\"}")
            )
            .andExpect(status().isOk());
        assertThat(authorizationService.getGrantedSymbols("trader-one")).containsExactlyInAnyOrder("INFY", "TCS");

        mockMvc
            .perform(delete(BASE_URL + "/{id}/items/{symbol}", watchlist.getId(), "INFY").with(traderOneJwt()))
            .andExpect(status().isOk());
        assertThat(authorizationService.getGrantedSymbols("trader-one")).containsExactly("TCS");
    }

    private Watchlist createWatchlist(TraderProfile owner, String name, List<String> symbols) {
        Watchlist watchlist = new Watchlist().name(name).traderProfile(owner);
        AtomicInteger order = new AtomicInteger();
        symbols.stream().map(symbol -> new WatchlistItem().symbol(symbol).sortOrder(order.getAndIncrement())).forEach(watchlist::addItem);
        return watchlistRepository.saveAndFlush(watchlist);
    }

    private TraderProfile ensureTraderProfile(String login, String displayName, String email) {
        return traderProfileRepository
            .findOneByUserLogin(login)
            .orElseGet(() -> {
                User user = userRepository.findOneByLogin(login).orElseThrow(() -> new IllegalStateException("Missing user " + login));
                TraderProfile profile = new TraderProfile()
                    .displayName(displayName)
                    .email(email)
                    .kycStatus(KycStatus.APPROVED)
                    .status(AccountStatus.ACTIVE)
                    .user(user);
                return traderProfileRepository.saveAndFlush(profile);
            });
    }

    private JwtRequestPostProcessor traderOneJwt() {
        return traderJwt("trader-one");
    }

    private JwtRequestPostProcessor traderTwoJwt() {
        return traderJwt("trader-two");
    }

    private JwtRequestPostProcessor traderJwt(String username) {
        return jwt().jwt(jwt -> jwt.subject(username)).authorities(new SimpleGrantedAuthority(AuthoritiesConstants.TRADER));
    }
}
