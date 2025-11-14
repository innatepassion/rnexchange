package com.rnexchange.web.websocket;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.rnexchange.service.dto.BarDTO;
import com.rnexchange.service.dto.QuoteDTO;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class MarketDataWebSocketHandlerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private MarketDataWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MarketDataWebSocketHandler(messagingTemplate);
    }

    @Test
    void broadcastsQuotesToDestination() {
        QuoteDTO quote = new QuoteDTO("INFY", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, 1000L, Instant.now());

        handler.broadcastQuote(quote);

        verify(messagingTemplate).convertAndSend("/topic/quotes/INFY", quote);
    }

    @Test
    void continuesWhenMessagingFails() {
        QuoteDTO quote = new QuoteDTO("INFY", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, 1000L, Instant.now());
        doThrow(new IllegalStateException("broker down")).when(messagingTemplate).convertAndSend("/topic/quotes/" + quote.symbol(), quote);

        assertThatCode(() -> handler.broadcastQuote(quote)).doesNotThrowAnyException();
    }

    @Test
    void broadcastsBarsSafely() {
        BarDTO bar = new BarDTO("INFY", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, 5L, Instant.now());

        handler.broadcastBar(bar);

        verify(messagingTemplate).convertAndSend(eq("/topic/bars/INFY"), eq(bar));
    }
}
