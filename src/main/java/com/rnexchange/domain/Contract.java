package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rnexchange.domain.enumeration.ContractType;
import com.rnexchange.domain.enumeration.OptionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A Contract.
 */
@Entity
@Table(name = "contract")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Contract implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "instrument_symbol", nullable = false)
    private String instrumentSymbol;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType;

    @NotNull
    @Column(name = "expiry", nullable = false)
    private LocalDate expiry;

    @Column(name = "strike", precision = 21, scale = 2)
    private BigDecimal strike;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type")
    private OptionType optionType;

    @NotNull
    @Column(name = "segment", nullable = false)
    private String segment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "exchange" }, allowSetters = true)
    private Instrument instrument;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Contract id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstrumentSymbol() {
        return this.instrumentSymbol;
    }

    public Contract instrumentSymbol(String instrumentSymbol) {
        this.setInstrumentSymbol(instrumentSymbol);
        return this;
    }

    public void setInstrumentSymbol(String instrumentSymbol) {
        this.instrumentSymbol = instrumentSymbol;
    }

    public ContractType getContractType() {
        return this.contractType;
    }

    public Contract contractType(ContractType contractType) {
        this.setContractType(contractType);
        return this;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public LocalDate getExpiry() {
        return this.expiry;
    }

    public Contract expiry(LocalDate expiry) {
        this.setExpiry(expiry);
        return this;
    }

    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }

    public BigDecimal getStrike() {
        return this.strike;
    }

    public Contract strike(BigDecimal strike) {
        this.setStrike(strike);
        return this;
    }

    public void setStrike(BigDecimal strike) {
        this.strike = strike;
    }

    public OptionType getOptionType() {
        return this.optionType;
    }

    public Contract optionType(OptionType optionType) {
        this.setOptionType(optionType);
        return this;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }

    public String getSegment() {
        return this.segment;
    }

    public Contract segment(String segment) {
        this.setSegment(segment);
        return this;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public Contract instrument(Instrument instrument) {
        this.setInstrument(instrument);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Contract)) {
            return false;
        }
        return getId() != null && getId().equals(((Contract) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Contract{" +
            "id=" + getId() +
            ", instrumentSymbol='" + getInstrumentSymbol() + "'" +
            ", contractType='" + getContractType() + "'" +
            ", expiry='" + getExpiry() + "'" +
            ", strike=" + getStrike() +
            ", optionType='" + getOptionType() + "'" +
            ", segment='" + getSegment() + "'" +
            "}";
    }
}
