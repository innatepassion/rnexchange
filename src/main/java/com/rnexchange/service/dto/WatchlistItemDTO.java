package com.rnexchange.service.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing a symbol that belongs to a watchlist.
 */
public class WatchlistItemDTO implements Serializable {

    private Long id;

    @NotNull
    private String symbol;

    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WatchlistItemDTO)) {
            return false;
        }
        WatchlistItemDTO that = (WatchlistItemDTO) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "WatchlistItemDTO{" + "id=" + id + ", symbol='" + symbol + '\'' + '}';
    }
}
