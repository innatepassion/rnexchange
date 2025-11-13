package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.OrderSide;
import com.rnexchange.domain.enumeration.OrderType;
import com.rnexchange.domain.enumeration.Tif;
import java.math.BigDecimal;
import java.util.Objects;

public final class TraderOrderRequest {

    private final String traderLogin;
    private final String instrumentSymbol;
    private final Long instrumentId;
    private final OrderSide side;
    private final OrderType type;
    private final Tif tif;
    private final BigDecimal quantity;
    private final BigDecimal price;

    private TraderOrderRequest(Builder builder) {
        this.traderLogin = builder.traderLogin;
        this.instrumentSymbol = builder.instrumentSymbol;
        this.instrumentId = builder.instrumentId;
        this.side = builder.side;
        this.type = builder.type;
        this.tif = builder.tif;
        this.quantity = builder.quantity;
        this.price = builder.price;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTraderLogin() {
        return traderLogin;
    }

    public String getInstrumentSymbol() {
        return instrumentSymbol;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public Tif getTif() {
        return tif;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public static final class Builder {

        private String traderLogin;
        private String instrumentSymbol;
        private Long instrumentId;
        private OrderSide side;
        private OrderType type;
        private Tif tif;
        private BigDecimal quantity;
        private BigDecimal price;

        public Builder traderLogin(String value) {
            this.traderLogin = value;
            return this;
        }

        public Builder instrumentSymbol(String value) {
            this.instrumentSymbol = value;
            return this;
        }

        public Builder instrumentId(Long value) {
            this.instrumentId = value;
            return this;
        }

        public Builder side(OrderSide value) {
            this.side = value;
            return this;
        }

        public Builder type(OrderType value) {
            this.type = value;
            return this;
        }

        public Builder tif(Tif value) {
            this.tif = value;
            return this;
        }

        public Builder quantity(BigDecimal value) {
            this.quantity = value;
            return this;
        }

        public Builder price(BigDecimal value) {
            this.price = value;
            return this;
        }

        public TraderOrderRequest build() {
            Objects.requireNonNull(traderLogin, "traderLogin must not be null");
            if (instrumentSymbol == null && instrumentId == null) {
                throw new IllegalArgumentException("instrumentSymbol or instrumentId must be provided");
            }
            Objects.requireNonNull(side, "side must not be null");
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(tif, "tif must not be null");
            Objects.requireNonNull(quantity, "quantity must not be null");
            Objects.requireNonNull(price, "price must not be null");
            return new TraderOrderRequest(this);
        }
    }
}
