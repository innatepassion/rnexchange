package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Lot.
 */
@Entity
@Table(name = "lot")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Lot implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "open_ts", nullable = false)
    private Instant openTs;

    @NotNull
    @Column(name = "open_px", precision = 21, scale = 2, nullable = false)
    private BigDecimal openPx;

    @NotNull
    @Column(name = "qty_open", precision = 21, scale = 2, nullable = false)
    private BigDecimal qtyOpen;

    @NotNull
    @Column(name = "qty_closed", precision = 21, scale = 2, nullable = false)
    private BigDecimal qtyClosed;

    @Column(name = "method")
    private String method;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "tradingAccount", "instrument" }, allowSetters = true)
    private Position position;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Lot id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getOpenTs() {
        return this.openTs;
    }

    public Lot openTs(Instant openTs) {
        this.setOpenTs(openTs);
        return this;
    }

    public void setOpenTs(Instant openTs) {
        this.openTs = openTs;
    }

    public BigDecimal getOpenPx() {
        return this.openPx;
    }

    public Lot openPx(BigDecimal openPx) {
        this.setOpenPx(openPx);
        return this;
    }

    public void setOpenPx(BigDecimal openPx) {
        this.openPx = openPx;
    }

    public BigDecimal getQtyOpen() {
        return this.qtyOpen;
    }

    public Lot qtyOpen(BigDecimal qtyOpen) {
        this.setQtyOpen(qtyOpen);
        return this;
    }

    public void setQtyOpen(BigDecimal qtyOpen) {
        this.qtyOpen = qtyOpen;
    }

    public BigDecimal getQtyClosed() {
        return this.qtyClosed;
    }

    public Lot qtyClosed(BigDecimal qtyClosed) {
        this.setQtyClosed(qtyClosed);
        return this;
    }

    public void setQtyClosed(BigDecimal qtyClosed) {
        this.qtyClosed = qtyClosed;
    }

    public String getMethod() {
        return this.method;
    }

    public Lot method(String method) {
        this.setMethod(method);
        return this;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Position getPosition() {
        return this.position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Lot position(Position position) {
        this.setPosition(position);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Lot)) {
            return false;
        }
        return getId() != null && getId().equals(((Lot) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Lot{" +
            "id=" + getId() +
            ", openTs='" + getOpenTs() + "'" +
            ", openPx=" + getOpenPx() +
            ", qtyOpen=" + getQtyOpen() +
            ", qtyClosed=" + getQtyClosed() +
            ", method='" + getMethod() + "'" +
            "}";
    }
}
