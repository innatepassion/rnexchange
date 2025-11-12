package com.rnexchange.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.Position} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PositionDTO implements Serializable {

    private Long id;

    @NotNull
    private BigDecimal qty;

    @NotNull
    private BigDecimal avgCost;

    private BigDecimal lastPx;

    private BigDecimal unrealizedPnl;

    private BigDecimal realizedPnl;

    private TradingAccountDTO tradingAccount;

    private InstrumentDTO instrument;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getAvgCost() {
        return avgCost;
    }

    public void setAvgCost(BigDecimal avgCost) {
        this.avgCost = avgCost;
    }

    public BigDecimal getLastPx() {
        return lastPx;
    }

    public void setLastPx(BigDecimal lastPx) {
        this.lastPx = lastPx;
    }

    public BigDecimal getUnrealizedPnl() {
        return unrealizedPnl;
    }

    public void setUnrealizedPnl(BigDecimal unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    public BigDecimal getRealizedPnl() {
        return realizedPnl;
    }

    public void setRealizedPnl(BigDecimal realizedPnl) {
        this.realizedPnl = realizedPnl;
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
        if (!(o instanceof PositionDTO)) {
            return false;
        }

        PositionDTO positionDTO = (PositionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, positionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PositionDTO{" +
            "id=" + getId() +
            ", qty=" + getQty() +
            ", avgCost=" + getAvgCost() +
            ", lastPx=" + getLastPx() +
            ", unrealizedPnl=" + getUnrealizedPnl() +
            ", realizedPnl=" + getRealizedPnl() +
            ", tradingAccount=" + getTradingAccount() +
            ", instrument=" + getInstrument() +
            "}";
    }
}
