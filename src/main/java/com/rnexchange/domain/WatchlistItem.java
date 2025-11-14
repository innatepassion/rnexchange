package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A WatchlistItem.
 */
@Entity
@Table(name = "watchlist_item")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WatchlistItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "symbol", nullable = false, length = 40)
    private String symbol;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "watchlist_id", nullable = false)
    @JsonIgnoreProperties(value = { "items", "traderProfile" }, allowSetters = true)
    private Watchlist watchlist;

    public Long getId() {
        return this.id;
    }

    public WatchlistItem id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public WatchlistItem symbol(String symbol) {
        this.setSymbol(symbol);
        return this;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public WatchlistItem sortOrder(Integer sortOrder) {
        this.setSortOrder(sortOrder);
        return this;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Watchlist getWatchlist() {
        return this.watchlist;
    }

    public void setWatchlist(Watchlist watchlist) {
        this.watchlist = watchlist;
    }

    public WatchlistItem watchlist(Watchlist watchlist) {
        this.setWatchlist(watchlist);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WatchlistItem)) {
            return false;
        }
        return getId() != null && getId().equals(((WatchlistItem) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "WatchlistItem{" + "id=" + getId() + ", symbol='" + getSymbol() + "'" + "}";
    }
}
