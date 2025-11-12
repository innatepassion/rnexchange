package com.rnexchange.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A MarginRule.
 */
@Entity
@Table(name = "margin_rule")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MarginRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "scope", nullable = false)
    private String scope;

    @Column(name = "initial_pct", precision = 21, scale = 2)
    private BigDecimal initialPct;

    @Column(name = "maint_pct", precision = 21, scale = 2)
    private BigDecimal maintPct;

    @Lob
    @Column(name = "span_json")
    private String spanJson;

    @ManyToOne(fetch = FetchType.LAZY)
    private Exchange exchange;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public MarginRule id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScope() {
        return this.scope;
    }

    public MarginRule scope(String scope) {
        this.setScope(scope);
        return this;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public BigDecimal getInitialPct() {
        return this.initialPct;
    }

    public MarginRule initialPct(BigDecimal initialPct) {
        this.setInitialPct(initialPct);
        return this;
    }

    public void setInitialPct(BigDecimal initialPct) {
        this.initialPct = initialPct;
    }

    public BigDecimal getMaintPct() {
        return this.maintPct;
    }

    public MarginRule maintPct(BigDecimal maintPct) {
        this.setMaintPct(maintPct);
        return this;
    }

    public void setMaintPct(BigDecimal maintPct) {
        this.maintPct = maintPct;
    }

    public String getSpanJson() {
        return this.spanJson;
    }

    public MarginRule spanJson(String spanJson) {
        this.setSpanJson(spanJson);
        return this;
    }

    public void setSpanJson(String spanJson) {
        this.spanJson = spanJson;
    }

    public Exchange getExchange() {
        return this.exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public MarginRule exchange(Exchange exchange) {
        this.setExchange(exchange);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MarginRule)) {
            return false;
        }
        return getId() != null && getId().equals(((MarginRule) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MarginRule{" +
            "id=" + getId() +
            ", scope='" + getScope() + "'" +
            ", initialPct=" + getInitialPct() +
            ", maintPct=" + getMaintPct() +
            ", spanJson='" + getSpanJson() + "'" +
            "}";
    }
}
