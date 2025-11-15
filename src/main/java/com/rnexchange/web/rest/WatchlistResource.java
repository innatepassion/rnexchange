package com.rnexchange.web.rest;

import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.WatchlistService;
import com.rnexchange.service.dto.WatchlistDTO;
import com.rnexchange.service.dto.WatchlistSummaryDTO;
import com.rnexchange.web.rest.dto.AddWatchlistItemRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for trader watchlist management.
 */
@RestController
@RequestMapping("/api/watchlists")
public class WatchlistResource {

    private final WatchlistService watchlistService;

    public WatchlistResource(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.TRADER + "')")
    public ResponseEntity<List<WatchlistSummaryDTO>> getWatchlists() {
        return ResponseEntity.ok(watchlistService.getCurrentTraderSummaries());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.TRADER + "')")
    public ResponseEntity<WatchlistDTO> getWatchlist(@PathVariable("id") Long id) {
        return ResponseEntity.ok(watchlistService.getWatchlist(id));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.TRADER + "')")
    public ResponseEntity<WatchlistDTO> addWatchlistItem(@PathVariable("id") Long id, @Valid @RequestBody AddWatchlistItemRequest request) {
        return ResponseEntity.ok(watchlistService.addSymbol(id, request.getSymbol()));
    }

    @DeleteMapping("/{id}/items/{symbol}")
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.TRADER + "')")
    public ResponseEntity<WatchlistDTO> removeWatchlistItem(@PathVariable("id") Long id, @PathVariable("symbol") String symbol) {
        return ResponseEntity.ok(watchlistService.removeSymbol(id, symbol));
    }
}
