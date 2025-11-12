package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.ContractType;
import com.rnexchange.domain.enumeration.OptionType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.Contract} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ContractDTO implements Serializable {

    private Long id;

    @NotNull
    private String instrumentSymbol;

    @NotNull
    private ContractType contractType;

    @NotNull
    private LocalDate expiry;

    private BigDecimal strike;

    private OptionType optionType;

    @NotNull
    private String segment;

    private InstrumentDTO instrument;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstrumentSymbol() {
        return instrumentSymbol;
    }

    public void setInstrumentSymbol(String instrumentSymbol) {
        this.instrumentSymbol = instrumentSymbol;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }

    public BigDecimal getStrike() {
        return strike;
    }

    public void setStrike(BigDecimal strike) {
        this.strike = strike;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
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
        if (!(o instanceof ContractDTO)) {
            return false;
        }

        ContractDTO contractDTO = (ContractDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, contractDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ContractDTO{" +
            "id=" + getId() +
            ", instrumentSymbol='" + getInstrumentSymbol() + "'" +
            ", contractType='" + getContractType() + "'" +
            ", expiry='" + getExpiry() + "'" +
            ", strike=" + getStrike() +
            ", optionType='" + getOptionType() + "'" +
            ", segment='" + getSegment() + "'" +
            ", instrument=" + getInstrument() +
            "}";
    }
}
