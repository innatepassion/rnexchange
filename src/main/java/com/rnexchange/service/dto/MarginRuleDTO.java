package com.rnexchange.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.MarginRule} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MarginRuleDTO implements Serializable {

    private Long id;

    @NotNull
    private String scope;

    private BigDecimal initialPct;

    private BigDecimal maintPct;

    @Lob
    private String spanJson;

    private ExchangeDTO exchange;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public BigDecimal getInitialPct() {
        return initialPct;
    }

    public void setInitialPct(BigDecimal initialPct) {
        this.initialPct = initialPct;
    }

    public BigDecimal getMaintPct() {
        return maintPct;
    }

    public void setMaintPct(BigDecimal maintPct) {
        this.maintPct = maintPct;
    }

    public String getSpanJson() {
        return spanJson;
    }

    public void setSpanJson(String spanJson) {
        this.spanJson = spanJson;
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
        if (!(o instanceof MarginRuleDTO)) {
            return false;
        }

        MarginRuleDTO marginRuleDTO = (MarginRuleDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, marginRuleDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MarginRuleDTO{" +
            "id=" + getId() +
            ", scope='" + getScope() + "'" +
            ", initialPct=" + getInitialPct() +
            ", maintPct=" + getMaintPct() +
            ", spanJson='" + getSpanJson() + "'" +
            ", exchange=" + getExchange() +
            "}";
    }
}
