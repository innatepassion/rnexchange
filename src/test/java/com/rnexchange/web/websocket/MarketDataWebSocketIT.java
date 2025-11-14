package com.rnexchange.web.websocket;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.domain.enumeration.ExchangeStatus;
import com.rnexchange.repository.ExchangeRepository;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.service.dto.QuoteDTO;
import com.rnexchange.service.marketdata.MockMarketDataService;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
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
class MarketDataWebSocketIT {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMarketDataService mockMarketDataService;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    @BeforeEach
    void setUp() throws Exception {
        mockMarketDataService.stop();

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
        WebSocketHttpHeaders webSocketHeaders = new WebSocketHttpHeaders();
        stompSession = stompClient
            .connectAsync(
                "ws://localhost:" + port + "/ws",
                webSocketHeaders,
                stompHeaders,
                new StompSessionHandlerAdapter() {},
                new Object[] {}
            )
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
        mockMarketDataService.stop();
    }

    @Test
    void receivesQuotesOnSubscription() throws Exception {
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

    private SockJsClient sockJsClient() {
        java.util.List<Transport> transports = new java.util.ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        transports.add(new org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport());
        return new SockJsClient(transports);
    }
}
