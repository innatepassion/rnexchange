package com.rnexchange.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.MarketHoliday} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MarketHolidayDTO implements Serializable {

    private Long id;

    @NotNull
    private LocalDate tradeDate;

    private String reason;

    @NotNull
    private Boolean isHoliday;

    private ExchangeDTO exchange;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getIsHoliday() {
        return isHoliday;
    }

    public void setIsHoliday(Boolean isHoliday) {
        this.isHoliday = isHoliday;
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
        if (!(o instanceof MarketHolidayDTO)) {
            return false;
        }

        MarketHolidayDTO marketHolidayDTO = (MarketHolidayDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, marketHolidayDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MarketHolidayDTO{" +
            "id=" + getId() +
            ", tradeDate='" + getTradeDate() + "'" +
            ", reason='" + getReason() + "'" +
            ", isHoliday='" + getIsHoliday() + "'" +
            ", exchange=" + getExchange() +
            "}";
    }
}
