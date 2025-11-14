package com.rnexchange.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Request payload for adding a symbol to a watchlist.
 */
public class AddWatchlistItemRequest implements Serializable {

    @NotBlank
    private String symbol;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
