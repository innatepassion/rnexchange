package com.rnexchange.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lightweight DTO for populating watchlist selectors.
 */
public class WatchlistSummaryDTO implements Serializable {

    private Long id;
    private String name;
    private int symbolCount;
    private List<String> symbols = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSymbolCount() {
        return symbolCount;
    }

    public void setSymbolCount(int symbolCount) {
        this.symbolCount = symbolCount;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WatchlistSummaryDTO)) {
            return false;
        }
        WatchlistSummaryDTO that = (WatchlistSummaryDTO) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
