package com.rnexchange.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.Execution} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExecutionDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant execTs;

    @NotNull
    private BigDecimal px;

    @NotNull
    private BigDecimal qty;

    private String liquidity;

    private BigDecimal fee;

    private OrderDTO order;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getExecTs() {
        return execTs;
    }

    public void setExecTs(Instant execTs) {
        this.execTs = execTs;
    }

    public BigDecimal getPx() {
        return px;
    }

    public void setPx(BigDecimal px) {
        this.px = px;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public String getLiquidity() {
        return liquidity;
    }

    public void setLiquidity(String liquidity) {
        this.liquidity = liquidity;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public OrderDTO getOrder() {
        return order;
    }

    public void setOrder(OrderDTO order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExecutionDTO)) {
            return false;
        }

        ExecutionDTO executionDTO = (ExecutionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, executionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExecutionDTO{" +
            "id=" + getId() +
            ", execTs='" + getExecTs() + "'" +
            ", px=" + getPx() +
            ", qty=" + getQty() +
            ", liquidity='" + getLiquidity() + "'" +
            ", fee=" + getFee() +
            ", order=" + getOrder() +
            "}";
    }
}
