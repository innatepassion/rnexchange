package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rnexchange.domain.enumeration.AlertType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * A RiskAlert.
 */
@Entity
@Table(name = "risk_alert")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RiskAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "broker", "trader" }, allowSetters = true)
    private TradingAccount tradingAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private TraderProfile trader;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public RiskAlert id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AlertType getAlertType() {
        return this.alertType;
    }

    public RiskAlert alertType(AlertType alertType) {
        this.setAlertType(alertType);
        return this;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public String getDescription() {
        return this.description;
    }

    public RiskAlert description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public RiskAlert createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public TradingAccount getTradingAccount() {
        return this.tradingAccount;
    }

    public void setTradingAccount(TradingAccount tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    public RiskAlert tradingAccount(TradingAccount tradingAccount) {
        this.setTradingAccount(tradingAccount);
        return this;
    }

    public TraderProfile getTrader() {
        return this.trader;
    }

    public void setTrader(TraderProfile traderProfile) {
        this.trader = traderProfile;
    }

    public RiskAlert trader(TraderProfile traderProfile) {
        this.setTrader(traderProfile);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RiskAlert)) {
            return false;
        }
        return getId() != null && getId().equals(((RiskAlert) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RiskAlert{" +
            "id=" + getId() +
            ", alertType='" + getAlertType() + "'" +
            ", description='" + getDescription() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
