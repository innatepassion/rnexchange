package com.rnexchange.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.Lot} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LotDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant openTs;

    @NotNull
    private BigDecimal openPx;

    @NotNull
    private BigDecimal qtyOpen;

    @NotNull
    private BigDecimal qtyClosed;

    private String method;

    private PositionDTO position;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getOpenTs() {
        return openTs;
    }

    public void setOpenTs(Instant openTs) {
        this.openTs = openTs;
    }

    public BigDecimal getOpenPx() {
        return openPx;
    }

    public void setOpenPx(BigDecimal openPx) {
        this.openPx = openPx;
    }

    public BigDecimal getQtyOpen() {
        return qtyOpen;
    }

    public void setQtyOpen(BigDecimal qtyOpen) {
        this.qtyOpen = qtyOpen;
    }

    public BigDecimal getQtyClosed() {
        return qtyClosed;
    }

    public void setQtyClosed(BigDecimal qtyClosed) {
        this.qtyClosed = qtyClosed;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public PositionDTO getPosition() {
        return position;
    }

    public void setPosition(PositionDTO position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LotDTO)) {
            return false;
        }

        LotDTO lotDTO = (LotDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, lotDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LotDTO{" +
            "id=" + getId() +
            ", openTs='" + getOpenTs() + "'" +
            ", openPx=" + getOpenPx() +
            ", qtyOpen=" + getQtyOpen() +
            ", qtyClosed=" + getQtyClosed() +
            ", method='" + getMethod() + "'" +
            ", position=" + getPosition() +
            "}";
    }
}
