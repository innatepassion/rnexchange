package com.rnexchange.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.ExchangeOperator} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExchangeOperatorDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    private UserDTO user;

    private ExchangeDTO exchange;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
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
        if (!(o instanceof ExchangeOperatorDTO)) {
            return false;
        }

        ExchangeOperatorDTO exchangeOperatorDTO = (ExchangeOperatorDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, exchangeOperatorDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExchangeOperatorDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", user=" + getUser() +
            ", exchange=" + getExchange() +
            "}";
    }
}
