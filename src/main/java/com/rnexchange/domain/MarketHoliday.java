package com.rnexchange.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * A MarketHoliday.
 */
@Entity
@Table(name = "market_holiday")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MarketHoliday implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "reason")
    private String reason;

    @NotNull
    @Column(name = "is_holiday", nullable = false)
    private Boolean isHoliday;

    @ManyToOne(fetch = FetchType.LAZY)
    private Exchange exchange;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public MarketHoliday id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTradeDate() {
        return this.tradeDate;
    }

    public MarketHoliday tradeDate(LocalDate tradeDate) {
        this.setTradeDate(tradeDate);
        return this;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getReason() {
        return this.reason;
    }

    public MarketHoliday reason(String reason) {
        this.setReason(reason);
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getIsHoliday() {
        return this.isHoliday;
    }

    public MarketHoliday isHoliday(Boolean isHoliday) {
        this.setIsHoliday(isHoliday);
        return this;
    }

    public void setIsHoliday(Boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    public Exchange getExchange() {
        return this.exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public MarketHoliday exchange(Exchange exchange) {
        this.setExchange(exchange);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MarketHoliday)) {
            return false;
        }
        return getId() != null && getId().equals(((MarketHoliday) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MarketHoliday{" +
            "id=" + getId() +
            ", tradeDate='" + getTradeDate() + "'" +
            ", reason='" + getReason() + "'" +
            ", isHoliday='" + getIsHoliday() + "'" +
            "}";
    }
}
