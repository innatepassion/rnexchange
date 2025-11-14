package com.rnexchange.web.websocket;

import com.rnexchange.service.dto.BarDTO;
import com.rnexchange.service.dto.QuoteDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MarketDataWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public MarketDataWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastQuote(QuoteDTO quote) {
        messagingTemplate.convertAndSend("/topic/quotes/" + quote.symbol(), quote);
    }

    public void broadcastBar(BarDTO bar) {
        messagingTemplate.convertAndSend("/topic/bars/" + bar.symbol(), bar);
    }
}
