package com.rnexchange.web.rest;

import com.rnexchange.service.dto.FeedStatusDTO;
import com.rnexchange.service.marketdata.MockMarketDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketdata/mock")
public class MarketDataControlResource {

    private final MockMarketDataService mockMarketDataService;

    public MarketDataControlResource(MockMarketDataService mockMarketDataService) {
        this.mockMarketDataService = mockMarketDataService;
    }

    @PostMapping("/start")
    @PreAuthorize("hasAuthority('EXCHANGE_OPERATOR')")
    public ResponseEntity<FeedStatusDTO> startFeed() {
        mockMarketDataService.start();
        return ResponseEntity.ok(mockMarketDataService.getStatus());
    }

    @PostMapping("/stop")
    @PreAuthorize("hasAuthority('EXCHANGE_OPERATOR')")
    public ResponseEntity<FeedStatusDTO> stopFeed() {
        mockMarketDataService.stop();
        return ResponseEntity.ok(mockMarketDataService.getStatus());
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('EXCHANGE_OPERATOR')")
    public ResponseEntity<FeedStatusDTO> getStatus() {
        return ResponseEntity.ok(mockMarketDataService.getStatus());
    }
}
