package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.AlertType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.RiskAlert} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RiskAlertDTO implements Serializable {

    private Long id;

    @NotNull
    private AlertType alertType;

    private String description;

    private Instant createdAt;

    private TradingAccountDTO tradingAccount;

    private TraderProfileDTO trader;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public TradingAccountDTO getTradingAccount() {
        return tradingAccount;
    }

    public void setTradingAccount(TradingAccountDTO tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    public TraderProfileDTO getTrader() {
        return trader;
    }

    public void setTrader(TraderProfileDTO trader) {
        this.trader = trader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RiskAlertDTO)) {
            return false;
        }

        RiskAlertDTO riskAlertDTO = (RiskAlertDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, riskAlertDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RiskAlertDTO{" +
            "id=" + getId() +
            ", alertType='" + getAlertType() + "'" +
            ", description='" + getDescription() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", tradingAccount=" + getTradingAccount() +
            ", trader=" + getTrader() +
            "}";
    }
}
