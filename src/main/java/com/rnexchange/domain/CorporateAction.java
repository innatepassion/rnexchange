package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rnexchange.domain.enumeration.CorporateActionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A CorporateAction.
 */
@Entity
@Table(name = "corporate_action")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CorporateAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CorporateActionType type;

    @NotNull
    @Column(name = "instrument_symbol", nullable = false)
    private String instrumentSymbol;

    @NotNull
    @Column(name = "ex_date", nullable = false)
    private LocalDate exDate;

    @Column(name = "pay_date")
    private LocalDate payDate;

    @Column(name = "ratio", precision = 21, scale = 2)
    private BigDecimal ratio;

    @Column(name = "cash_amount", precision = 21, scale = 2)
    private BigDecimal cashAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "exchange" }, allowSetters = true)
    private Instrument instrument;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CorporateAction id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CorporateActionType getType() {
        return this.type;
    }

    public CorporateAction type(CorporateActionType type) {
        this.setType(type);
        return this;
    }

    public void setType(CorporateActionType type) {
        this.type = type;
    }

    public String getInstrumentSymbol() {
        return this.instrumentSymbol;
    }

    public CorporateAction instrumentSymbol(String instrumentSymbol) {
        this.setInstrumentSymbol(instrumentSymbol);
        return this;
    }

    public void setInstrumentSymbol(String instrumentSymbol) {
        this.instrumentSymbol = instrumentSymbol;
    }

    public LocalDate getExDate() {
        return this.exDate;
    }

    public CorporateAction exDate(LocalDate exDate) {
        this.setExDate(exDate);
        return this;
    }

    public void setExDate(LocalDate exDate) {
        this.exDate = exDate;
    }

    public LocalDate getPayDate() {
        return this.payDate;
    }

    public CorporateAction payDate(LocalDate payDate) {
        this.setPayDate(payDate);
        return this;
    }

    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }

    public BigDecimal getRatio() {
        return this.ratio;
    }

    public CorporateAction ratio(BigDecimal ratio) {
        this.setRatio(ratio);
        return this;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public BigDecimal getCashAmount() {
        return this.cashAmount;
    }

    public CorporateAction cashAmount(BigDecimal cashAmount) {
        this.setCashAmount(cashAmount);
        return this;
    }

    public void setCashAmount(BigDecimal cashAmount) {
        this.cashAmount = cashAmount;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public CorporateAction instrument(Instrument instrument) {
        this.setInstrument(instrument);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CorporateAction)) {
            return false;
        }
        return getId() != null && getId().equals(((CorporateAction) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CorporateAction{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", instrumentSymbol='" + getInstrumentSymbol() + "'" +
            ", exDate='" + getExDate() + "'" +
            ", payDate='" + getPayDate() + "'" +
            ", ratio=" + getRatio() +
            ", cashAmount=" + getCashAmount() +
            "}";
    }
}
