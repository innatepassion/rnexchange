package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.AccountType;
import com.rnexchange.domain.enumeration.Currency;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.TradingAccount} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TradingAccountDTO implements Serializable {

    private Long id;

    @NotNull
    private AccountType type;

    @NotNull
    private Currency baseCcy;

    @NotNull
    private BigDecimal balance;

    @NotNull
    private AccountStatus status;

    private BrokerDTO broker;

    private TraderProfileDTO trader;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public Currency getBaseCcy() {
        return baseCcy;
    }

    public void setBaseCcy(Currency baseCcy) {
        this.baseCcy = baseCcy;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public BrokerDTO getBroker() {
        return broker;
    }

    public void setBroker(BrokerDTO broker) {
        this.broker = broker;
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
        if (!(o instanceof TradingAccountDTO)) {
            return false;
        }

        TradingAccountDTO tradingAccountDTO = (TradingAccountDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, tradingAccountDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TradingAccountDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", baseCcy='" + getBaseCcy() + "'" +
            ", balance=" + getBalance() +
            ", status='" + getStatus() + "'" +
            ", broker=" + getBroker() +
            ", trader=" + getTrader() +
            "}";
    }
}
