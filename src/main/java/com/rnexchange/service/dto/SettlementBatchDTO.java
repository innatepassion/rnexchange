package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.SettlementKind;
import com.rnexchange.domain.enumeration.SettlementStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.SettlementBatch} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SettlementBatchDTO implements Serializable {

    private Long id;

    @NotNull
    private LocalDate refDate;

    @NotNull
    private SettlementKind kind;

    @NotNull
    private SettlementStatus status;

    private String remarks;

    private ExchangeDTO exchange;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRefDate() {
        return refDate;
    }

    public void setRefDate(LocalDate refDate) {
        this.refDate = refDate;
    }

    public SettlementKind getKind() {
        return kind;
    }

    public void setKind(SettlementKind kind) {
        this.kind = kind;
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public void setStatus(SettlementStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public ExchangeDTO getExchange() {
        return exchange;
    }

    public void setExchange(ExchangeDTO exchange) {
        this.exchange = exchange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SettlementBatchDTO)) {
            return false;
        }

        SettlementBatchDTO settlementBatchDTO = (SettlementBatchDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, settlementBatchDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SettlementBatchDTO{" +
            "id=" + getId() +
            ", refDate='" + getRefDate() + "'" +
            ", kind='" + getKind() + "'" +
            ", status='" + getStatus() + "'" +
            ", remarks='" + getRemarks() + "'" +
            ", exchange=" + getExchange() +
            "}";
    }
}
