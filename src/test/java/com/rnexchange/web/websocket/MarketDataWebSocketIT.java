package com.rnexchange.web.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.domain.enumeration.ExchangeStatus;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.security.jwt.JwtAuthenticationTestUtils;
import com.rnexchange.service.dto.BarDTO;
import com.rnexchange.service.dto.QuoteDTO;
import com.rnexchange.service.marketdata.MockMarketDataService;
import com.rnexchange.service.marketdata.WatchlistAuthorizationService;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@IntegrationTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "marketdata.mock.bar-interval-seconds=1")
class MarketDataWebSocketIT {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMarketDataService mockMarketDataService;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private WatchlistAuthorizationService watchlistAuthorizationService;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    private String jwtKey;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private CompletableFuture<Throwable> sessionErrorFuture;
    private String username;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMarketDataService.stop();
        watchlistAuthorizationService.reset();

        username = "ws-user";
        authToken = JwtAuthenticationTestUtils.createValidTokenForUser(jwtKey, username, List.of("TRADER"));
        sessionErrorFuture = new CompletableFuture<>();

        String exchangeCode = "WSX";
        com.rnexchange.domain.Exchange exchange = exchangeRepository
            .findOneByCode(exchangeCode)
            .orElseGet(() ->
                exchangeRepository.saveAndFlush(
                    new com.rnexchange.domain.Exchange()
                        .code(exchangeCode)
                        .name("WebSocket Exchange")
                        .timezone("Asia/Kolkata")
                        .status(ExchangeStatus.ACTIVE)
                )
            );

        String symbol = "WS_SYMBOL";
        if (instrumentRepository.findOneBySymbol(symbol).isEmpty()) {
            com.rnexchange.domain.Instrument instrument = new com.rnexchange.domain.Instrument()
                .symbol(symbol)
                .name("WebSocket Instrument")
                .assetClass(AssetClass.EQUITY)
                .exchangeCode(exchangeCode)
                .tickSize(new BigDecimal("0.05"))
                .lotSize(1L)
                .currency(Currency.INR)
                .status("ACTIVE")
                .exchange(exchange);
            instrumentRepository.saveAndFlush(instrument);
        }
        org.assertj.core.api.Assertions.assertThat(instrumentRepository.findOneBySymbol(symbol))
            .as("Test instrument WS_SYMBOL must be persisted before test runs")
            .isPresent();

        stompClient = new WebSocketStompClient(sockJsClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add(HttpHeaders.AUTHORIZATION, JwtAuthenticationTestUtils.BEARER + authToken);
        WebSocketHttpHeaders webSocketHeaders = new WebSocketHttpHeaders();

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void handleException(
                StompSession session,
                StompCommand command,
                StompHeaders headers,
                byte[] payload,
                Throwable exception
            ) {
                sessionErrorFuture.complete(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                sessionErrorFuture.complete(exception);
            }
        };

        stompSession = stompClient
            .connectAsync("ws://localhost:" + port + "/ws", webSocketHeaders, stompHeaders, sessionHandler)
            .get(5, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
        watchlistAuthorizationService.clearPermissions(username);
        sessionErrorFuture = null;
        mockMarketDataService.stop();
    }

    @Test
    void receivesQuotesOnSubscription() throws Exception {
        watchlistAuthorizationService.grantSymbols(username, List.of("WS_SYMBOL"));
        BlockingQueue<QuoteDTO> quotes = new LinkedBlockingDeque<>();

        // Subscribe to the WebSocket topic
        stompSession.subscribe(
            "/topic/quotes/WS_SYMBOL",
            new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return QuoteDTO.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    System.out.println("Received quote via WebSocket: " + payload);
                    quotes.add((QuoteDTO) payload);
                }
            }
        );

        // Give subscription time to establish
        TimeUnit.SECONDS.sleep(1);

        // Start the feed
        mockMarketDataService.start();
        System.out.println("Feed started, instruments loaded: " + mockMarketDataService.getStatus().exchanges().size());

        // Wait for the first quote to arrive via WebSocket
        // The feed generates ticks every 750ms, so we should receive one within 5 seconds
        QuoteDTO quote = quotes.poll(5, TimeUnit.SECONDS);

        // Debug: Print what we got
        System.out.println("Received quote: " + quote);
        System.out.println("Feed status: " + mockMarketDataService.getStatus());

        assertThat(quote).as("Should receive at least one quote via WebSocket subscription").isNotNull();
        assertThat(quote.symbol()).isEqualTo("WS_SYMBOL");
        assertThat(quote.lastPrice()).isNotNull();
        assertThat(quote.timestamp()).isNotNull();
    }

    @Test
    void receivesBarsOnSubscription() throws Exception {
        watchlistAuthorizationService.grantSymbols(username, List.of("WS_SYMBOL"));
        BlockingQueue<BarDTO> bars = new LinkedBlockingDeque<>();

        stompSession.subscribe(
            "/topic/bars/WS_SYMBOL",
            new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return BarDTO.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    bars.add((BarDTO) payload);
                }
            }
        );

        TimeUnit.SECONDS.sleep(1);
        mockMarketDataService.start();

        BarDTO bar = bars.poll(5, TimeUnit.SECONDS);
        assertThat(bar).as("Should receive at least one bar via WebSocket subscription").isNotNull();
        assertThat(bar.symbol()).isEqualTo("WS_SYMBOL");
    }

    @Test
    void rejectsConnectionWithoutToken() {
        WebSocketStompClient unauthClient = new WebSocketStompClient(sockJsClient());
        unauthClient.setMessageConverter(new MappingJackson2MessageConverter());
        assertThatThrownBy(() -> {
            try {
                unauthClient
                    .connectAsync(
                        "ws://localhost:" + port + "/ws",
                        new WebSocketHttpHeaders(),
                        new StompHeaders(),
                        new StompSessionHandlerAdapter() {}
                    )
                    .get(5, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw ie;
            }
        })
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(AuthenticationCredentialsNotFoundException.class);
        unauthClient.stop();
    }

    @Test
    void rejectsSubscriptionForUnauthorizedSymbol() throws Exception {
        sessionErrorFuture = new CompletableFuture<>();
        watchlistAuthorizationService.grantSymbols(username, List.of("OTHER_SYMBOL"));

        stompSession.subscribe(
            "/topic/quotes/WS_SYMBOL",
            new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return QuoteDTO.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {}
            }
        );

        Throwable error = sessionErrorFuture.get(5, TimeUnit.SECONDS);
        assertThat(error).isInstanceOf(AccessDeniedException.class);
    }

    private SockJsClient sockJsClient() {
        java.util.List<Transport> transports = new java.util.ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        transports.add(new org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport());
        return new SockJsClient(transports);
    }
}
