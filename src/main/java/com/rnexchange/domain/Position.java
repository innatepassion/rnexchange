package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A Position.
 */
@Entity
@Table(name = "position")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Position implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "qty", precision = 21, scale = 2, nullable = false)
    private BigDecimal qty;

    @NotNull
    @Column(name = "avg_cost", precision = 21, scale = 2, nullable = false)
    private BigDecimal avgCost;

    @Column(name = "last_px", precision = 21, scale = 2)
    private BigDecimal lastPx;

    @Column(name = "unrealized_pnl", precision = 21, scale = 2)
    private BigDecimal unrealizedPnl;

    @Column(name = "realized_pnl", precision = 21, scale = 2)
    private BigDecimal realizedPnl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "broker", "trader" }, allowSetters = true)
    private TradingAccount tradingAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "exchange" }, allowSetters = true)
    private Instrument instrument;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Position id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getQty() {
        return this.qty;
    }

    public Position qty(BigDecimal qty) {
        this.setQty(qty);
        return this;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getAvgCost() {
        return this.avgCost;
    }

    public Position avgCost(BigDecimal avgCost) {
        this.setAvgCost(avgCost);
        return this;
    }

    public void setAvgCost(BigDecimal avgCost) {
        this.avgCost = avgCost;
    }

    public BigDecimal getLastPx() {
        return this.lastPx;
    }

    public Position lastPx(BigDecimal lastPx) {
        this.setLastPx(lastPx);
        return this;
    }

    public void setLastPx(BigDecimal lastPx) {
        this.lastPx = lastPx;
    }

    public BigDecimal getUnrealizedPnl() {
        return this.unrealizedPnl;
    }

    public Position unrealizedPnl(BigDecimal unrealizedPnl) {
        this.setUnrealizedPnl(unrealizedPnl);
        return this;
    }

    public void setUnrealizedPnl(BigDecimal unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    public BigDecimal getRealizedPnl() {
        return this.realizedPnl;
    }

    public Position realizedPnl(BigDecimal realizedPnl) {
        this.setRealizedPnl(realizedPnl);
        return this;
    }

    public void setRealizedPnl(BigDecimal realizedPnl) {
        this.realizedPnl = realizedPnl;
    }

    public TradingAccount getTradingAccount() {
        return this.tradingAccount;
    }

    public void setTradingAccount(TradingAccount tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    public Position tradingAccount(TradingAccount tradingAccount) {
        this.setTradingAccount(tradingAccount);
        return this;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public Position instrument(Instrument instrument) {
        this.setInstrument(instrument);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        return getId() != null && getId().equals(((Position) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Position{" +
            "id=" + getId() +
            ", qty=" + getQty() +
            ", avgCost=" + getAvgCost() +
            ", lastPx=" + getLastPx() +
            ", unrealizedPnl=" + getUnrealizedPnl() +
            ", realizedPnl=" + getRealizedPnl() +
            "}";
    }
}
