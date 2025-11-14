package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Watchlist.
 */
@Entity
@Table(name = "watchlist")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Watchlist implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trader_profile_id", nullable = false)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private TraderProfile traderProfile;

    @OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties(value = { "watchlist" }, allowSetters = true)
    private Set<WatchlistItem> items = new LinkedHashSet<>();

    public Long getId() {
        return this.id;
    }

    public Watchlist id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Watchlist name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TraderProfile getTraderProfile() {
        return this.traderProfile;
    }

    public void setTraderProfile(TraderProfile traderProfile) {
        this.traderProfile = traderProfile;
    }

    public Watchlist traderProfile(TraderProfile traderProfile) {
        this.setTraderProfile(traderProfile);
        return this;
    }

    public Set<WatchlistItem> getItems() {
        return this.items;
    }

    public void setItems(Set<WatchlistItem> watchlistItems) {
        if (this.items != null) {
            this.items.forEach(item -> item.setWatchlist(null));
        }
        if (watchlistItems != null) {
            watchlistItems.forEach(item -> item.setWatchlist(this));
        }
        this.items = watchlistItems;
    }

    public Watchlist items(Set<WatchlistItem> watchlistItems) {
        this.setItems(watchlistItems);
        return this;
    }

    public Watchlist addItem(WatchlistItem watchlistItem) {
        this.items.add(watchlistItem);
        watchlistItem.setWatchlist(this);
        return this;
    }

    public Watchlist removeItem(WatchlistItem watchlistItem) {
        if (watchlistItem != null) {
            this.items.remove(watchlistItem);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Watchlist)) {
            return false;
        }
        return getId() != null && getId().equals(((Watchlist) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Watchlist{" + "id=" + getId() + ", name='" + getName() + "'" + "}";
    }
}
