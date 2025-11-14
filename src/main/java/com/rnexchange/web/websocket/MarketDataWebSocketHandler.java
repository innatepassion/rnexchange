package com.rnexchange.web.websocket;

import com.rnexchange.service.dto.BarDTO;
import com.rnexchange.service.dto.QuoteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MarketDataWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MarketDataWebSocketHandler.class);

    private final SimpMessagingTemplate messagingTemplate;

    public MarketDataWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastQuote(QuoteDTO quote) {
        sendSafely(() -> messagingTemplate.convertAndSend("/topic/quotes/" + quote.symbol(), quote), "quote", quote.symbol());
    }

    public void broadcastBar(BarDTO bar) {
        sendSafely(() -> messagingTemplate.convertAndSend("/topic/bars/" + bar.symbol(), bar), "bar", bar.symbol());
    }

    private void sendSafely(Runnable operation, String payloadType, String symbol) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Broadcasting {} update for {}", payloadType, symbol);
            }
            operation.run();
        } catch (RuntimeException ex) {
            log.warn("Failed to broadcast {} update for {}: {}", payloadType, symbol, ex.getMessage(), ex);
        }
    }
}
