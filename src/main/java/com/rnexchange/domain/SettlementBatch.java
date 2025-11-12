package com.rnexchange.domain;

import com.rnexchange.domain.enumeration.SettlementKind;
import com.rnexchange.domain.enumeration.SettlementStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * A SettlementBatch.
 */
@Entity
@Table(name = "settlement_batch")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SettlementBatch implements Serializable {

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
    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private SettlementKind kind;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SettlementStatus status;

    @Column(name = "remarks")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    private Exchange exchange;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SettlementBatch id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRefDate() {
        return this.refDate;
    }

    public SettlementBatch refDate(LocalDate refDate) {
        this.setRefDate(refDate);
        return this;
    }

    public void setRefDate(LocalDate refDate) {
        this.refDate = refDate;
    }

    public SettlementKind getKind() {
        return this.kind;
    }

    public SettlementBatch kind(SettlementKind kind) {
        this.setKind(kind);
        return this;
    }

    public void setKind(SettlementKind kind) {
        this.kind = kind;
    }

    public SettlementStatus getStatus() {
        return this.status;
    }

    public SettlementBatch status(SettlementStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(SettlementStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public SettlementBatch remarks(String remarks) {
        this.setRemarks(remarks);
        return this;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Exchange getExchange() {
        return this.exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public SettlementBatch exchange(Exchange exchange) {
        this.setExchange(exchange);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SettlementBatch)) {
            return false;
        }
        return getId() != null && getId().equals(((SettlementBatch) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SettlementBatch{" +
            "id=" + getId() +
            ", refDate='" + getRefDate() + "'" +
            ", kind='" + getKind() + "'" +
            ", status='" + getStatus() + "'" +
            ", remarks='" + getRemarks() + "'" +
            "}";
    }
}
