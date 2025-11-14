package com.rnexchange.service.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Full watchlist DTO including ordered items.
 */
public class WatchlistDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private List<WatchlistItemDTO> items = new ArrayList<>();

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

    public List<WatchlistItemDTO> getItems() {
        return items;
    }

    public void setItems(List<WatchlistItemDTO> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WatchlistDTO)) {
            return false;
        }
        WatchlistDTO that = (WatchlistDTO) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "WatchlistDTO{" + "id=" + id + ", name='" + name + '\'' + ", items=" + items.size() + '}';
    }
}
