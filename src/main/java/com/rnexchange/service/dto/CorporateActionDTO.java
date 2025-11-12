package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.CorporateActionType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.CorporateAction} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CorporateActionDTO implements Serializable {

    private Long id;

    @NotNull
    private CorporateActionType type;

    @NotNull
    private String instrumentSymbol;

    @NotNull
    private LocalDate exDate;

    private LocalDate payDate;

    private BigDecimal ratio;

    private BigDecimal cashAmount;

    private InstrumentDTO instrument;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CorporateActionType getType() {
        return type;
    }

    public void setType(CorporateActionType type) {
        this.type = type;
    }

    public String getInstrumentSymbol() {
        return instrumentSymbol;
    }

    public void setInstrumentSymbol(String instrumentSymbol) {
        this.instrumentSymbol = instrumentSymbol;
    }

    public LocalDate getExDate() {
        return exDate;
    }

    public void setExDate(LocalDate exDate) {
        this.exDate = exDate;
    }

    public LocalDate getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public BigDecimal getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(BigDecimal cashAmount) {
        this.cashAmount = cashAmount;
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
        if (!(o instanceof CorporateActionDTO)) {
            return false;
        }

        CorporateActionDTO corporateActionDTO = (CorporateActionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, corporateActionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CorporateActionDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", instrumentSymbol='" + getInstrumentSymbol() + "'" +
            ", exDate='" + getExDate() + "'" +
            ", payDate='" + getPayDate() + "'" +
            ", ratio=" + getRatio() +
            ", cashAmount=" + getCashAmount() +
            ", instrument=" + getInstrument() +
            "}";
    }
}
