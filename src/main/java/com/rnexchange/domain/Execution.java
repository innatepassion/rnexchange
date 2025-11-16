package com.rnexchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rnexchange.domain.enumeration.OrderSide;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Execution.
 */
@Entity
@Table(name = "execution")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Execution implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "exec_ts", nullable = false)
    private Instant execTs;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private OrderSide side;

    @NotNull
    @Column(name = "px", precision = 21, scale = 2, nullable = false)
    private BigDecimal px;

    @NotNull
    @Column(name = "qty", precision = 21, scale = 2, nullable = false)
    private BigDecimal qty;

    @Column(name = "liquidity")
    private String liquidity;

    @Column(name = "fee", precision = 21, scale = 2)
    private BigDecimal fee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "tradingAccount", "instrument" }, allowSetters = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "broker", "trader" }, allowSetters = true)
    private TradingAccount tradingAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "exchange" }, allowSetters = true)
    private Instrument instrument;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Execution id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getExecTs() {
        return this.execTs;
    }

    public Execution execTs(Instant execTs) {
        this.setExecTs(execTs);
        return this;
    }

    public void setExecTs(Instant execTs) {
        this.execTs = execTs;
    }

    public OrderSide getSide() {
        return this.side;
    }

    public Execution side(OrderSide side) {
        this.setSide(side);
        return this;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public BigDecimal getPx() {
        return this.px;
    }

    public Execution px(BigDecimal px) {
        this.setPx(px);
        return this;
    }

    public void setPx(BigDecimal px) {
        this.px = px;
    }

    public BigDecimal getQty() {
        return this.qty;
    }

    public Execution qty(BigDecimal qty) {
        this.setQty(qty);
        return this;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public String getLiquidity() {
        return this.liquidity;
    }

    public Execution liquidity(String liquidity) {
        this.setLiquidity(liquidity);
        return this;
    }

    public void setLiquidity(String liquidity) {
        this.liquidity = liquidity;
    }

    public BigDecimal getFee() {
        return this.fee;
    }

    public Execution fee(BigDecimal fee) {
        this.setFee(fee);
        return this;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public Order getOrder() {
        return this.order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Execution order(Order order) {
        this.setOrder(order);
        return this;
    }

    public TradingAccount getTradingAccount() {
        return this.tradingAccount;
    }

    public void setTradingAccount(TradingAccount tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    public Execution tradingAccount(TradingAccount tradingAccount) {
        this.setTradingAccount(tradingAccount);
        return this;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public Execution instrument(Instrument instrument) {
        this.setInstrument(instrument);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Execution)) {
            return false;
        }
        return getId() != null && getId().equals(((Execution) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Execution{" +
            "id=" + getId() +
            ", execTs='" + getExecTs() + "'" +
            ", side='" + getSide() + "'" +
            ", px=" + getPx() +
            ", qty=" + getQty() +
            ", liquidity='" + getLiquidity() + "'" +
            ", fee=" + getFee() +
            "}";
    }
}
