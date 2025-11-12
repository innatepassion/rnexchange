package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A DailySettlementPrice.
 */
@Entity
@Table(name = "daily_settlement_price")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DailySettlementPrice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "ref_date", nullable = false)
    private LocalDate refDate;

    @NotNull
    @Column(name = "instrument_symbol", nullable = false)
    private String instrumentSymbol;

    @NotNull
    @Column(name = "settle_price", precision = 21, scale = 2, nullable = false)
    private BigDecimal settlePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "exchange" }, allowSetters = true)
    private Instrument instrument;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DailySettlementPrice id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRefDate() {
        return this.refDate;
    }

    public DailySettlementPrice refDate(LocalDate refDate) {
        this.setRefDate(refDate);
        return this;
    }

    public void setRefDate(LocalDate refDate) {
        this.refDate = refDate;
    }

    public String getInstrumentSymbol() {
        return this.instrumentSymbol;
    }

    public DailySettlementPrice instrumentSymbol(String instrumentSymbol) {
        this.setInstrumentSymbol(instrumentSymbol);
        return this;
    }

    public void setInstrumentSymbol(String instrumentSymbol) {
        this.instrumentSymbol = instrumentSymbol;
    }

    public BigDecimal getSettlePrice() {
        return this.settlePrice;
    }

    public DailySettlementPrice settlePrice(BigDecimal settlePrice) {
        this.setSettlePrice(settlePrice);
        return this;
    }

    public void setSettlePrice(BigDecimal settlePrice) {
        this.settlePrice = settlePrice;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public DailySettlementPrice instrument(Instrument instrument) {
        this.setInstrument(instrument);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DailySettlementPrice)) {
            return false;
        }
        return getId() != null && getId().equals(((DailySettlementPrice) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DailySettlementPrice{" +
            "id=" + getId() +
            ", refDate='" + getRefDate() + "'" +
            ", instrumentSymbol='" + getInstrumentSymbol() + "'" +
            ", settlePrice=" + getSettlePrice() +
            "}";
    }
}
