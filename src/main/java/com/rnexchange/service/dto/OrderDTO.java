package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.OrderSide;
import com.rnexchange.domain.enumeration.OrderStatus;
import com.rnexchange.domain.enumeration.OrderType;
import com.rnexchange.domain.enumeration.Tif;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.Order} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OrderDTO implements Serializable {

    private Long id;

    @NotNull
    private OrderSide side;

    @NotNull
    private OrderType type;

    @NotNull
    private BigDecimal qty;

    private BigDecimal limitPx;

    private BigDecimal stopPx;

    @NotNull
    private Tif tif;

    @NotNull
    private OrderStatus status;

    private String rejectionReason;

    @NotNull
    private String venue;

    private Instant createdAt;

    private Instant updatedAt;

    private TradingAccountDTO tradingAccount;

    private InstrumentDTO instrument;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getLimitPx() {
        return limitPx;
    }

    public void setLimitPx(BigDecimal limitPx) {
        this.limitPx = limitPx;
    }

    public BigDecimal getStopPx() {
        return stopPx;
    }

    public void setStopPx(BigDecimal stopPx) {
        this.stopPx = stopPx;
    }

    public Tif getTif() {
        return tif;
    }

    public void setTif(Tif tif) {
        this.tif = tif;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public TradingAccountDTO getTradingAccount() {
        return tradingAccount;
    }

    public void setTradingAccount(TradingAccountDTO tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    public InstrumentDTO getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentDTO instrument) {
        this.instrument = instrument;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderDTO)) {
            return false;
        }

        OrderDTO orderDTO = (OrderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, orderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OrderDTO{" +
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
            ", tradingAccount=" + getTradingAccount() +
            ", instrument=" + getInstrument() +
            "}";
    }
}
