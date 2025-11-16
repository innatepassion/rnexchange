package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rnexchange.domain.enumeration.OrderSide;
import com.rnexchange.domain.enumeration.OrderStatus;
import com.rnexchange.domain.enumeration.OrderType;
import com.rnexchange.domain.enumeration.Tif;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Order.
 */
@Entity
@Table(name = "jhi_order")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private OrderSide side;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OrderType type;

    @NotNull
    @Column(name = "qty", precision = 21, scale = 2, nullable = false)
    private BigDecimal qty;

    @Column(name = "limit_px", precision = 21, scale = 2)
    private BigDecimal limitPx;

    @Column(name = "stop_px", precision = 21, scale = 2)
    private BigDecimal stopPx;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tif", nullable = false)
    private Tif tif;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @NotNull
    @Column(name = "venue", nullable = false)
    private String venue;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

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

    public Order id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderSide getSide() {
        return this.side;
    }

    public Order side(OrderSide side) {
        this.setSide(side);
        return this;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public OrderType getType() {
        return this.type;
    }

    public Order type(OrderType type) {
        this.setType(type);
        return this;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public BigDecimal getQty() {
        return this.qty;
    }

    public Order qty(BigDecimal qty) {
        this.setQty(qty);
        return this;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getLimitPx() {
        return this.limitPx;
    }

    public Order limitPx(BigDecimal limitPx) {
        this.setLimitPx(limitPx);
        return this;
    }

    public void setLimitPx(BigDecimal limitPx) {
        this.limitPx = limitPx;
    }

    public BigDecimal getStopPx() {
        return this.stopPx;
    }

    public Order stopPx(BigDecimal stopPx) {
        this.setStopPx(stopPx);
        return this;
    }

    public void setStopPx(BigDecimal stopPx) {
        this.stopPx = stopPx;
    }

    public Tif getTif() {
        return this.tif;
    }

    public Order tif(Tif tif) {
        this.setTif(tif);
        return this;
    }

    public void setTif(Tif tif) {
        this.tif = tif;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public Order status(OrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return this.rejectionReason;
    }

    public Order rejectionReason(String rejectionReason) {
        this.setRejectionReason(rejectionReason);
        return this;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getVenue() {
        return this.venue;
    }

    public Order venue(String venue) {
        this.setVenue(venue);
        return this;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Order createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Order updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public TradingAccount getTradingAccount() {
        return this.tradingAccount;
    }

    public void setTradingAccount(TradingAccount tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    public Order tradingAccount(TradingAccount tradingAccount) {
        this.setTradingAccount(tradingAccount);
        return this;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public Order instrument(Instrument instrument) {
        this.setInstrument(instrument);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Order)) {
            return false;
        }
        return getId() != null && getId().equals(((Order) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Order{" +
            "id=" + getId() +
            ", side='" + getSide() + "'" +
            ", type='" + getType() + "'" +
            ", qty=" + getQty() +
            ", limitPx=" + getLimitPx() +
            ", stopPx=" + getStopPx() +
            ", tif='" + getTif() + "'" +
            ", status='" + getStatus() + "'" +
            ", rejectionReason='" + getRejectionReason() + "'" +
            ", venue='" + getVenue() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
