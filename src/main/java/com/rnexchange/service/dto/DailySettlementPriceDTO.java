package com.rnexchange.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.DailySettlementPrice} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DailySettlementPriceDTO implements Serializable {

    private Long id;

    @NotNull
    private LocalDate refDate;

    @NotNull
    private String instrumentSymbol;

    @NotNull
    private BigDecimal settlePrice;

    private InstrumentDTO instrument;

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

    public String getInstrumentSymbol() {
        return instrumentSymbol;
    }

    public void setInstrumentSymbol(String instrumentSymbol) {
        this.instrumentSymbol = instrumentSymbol;
    }

    public BigDecimal getSettlePrice() {
        return settlePrice;
    }

    public void setSettlePrice(BigDecimal settlePrice) {
        this.settlePrice = settlePrice;
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
        if (!(o instanceof DailySettlementPriceDTO)) {
            return false;
        }

        DailySettlementPriceDTO dailySettlementPriceDTO = (DailySettlementPriceDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, dailySettlementPriceDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DailySettlementPriceDTO{" +
            "id=" + getId() +
            ", refDate='" + getRefDate() + "'" +
            ", instrumentSymbol='" + getInstrumentSymbol() + "'" +
            ", settlePrice=" + getSettlePrice() +
            ", instrument=" + getInstrument() +
            "}";
    }
}
