package com.rnexchange.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.BrokerDesk} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BrokerDeskDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    private UserDTO user;

    private BrokerDTO broker;

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

    public BrokerDTO getBroker() {
        return broker;
    }

    public void setBroker(BrokerDTO broker) {
        this.broker = broker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BrokerDeskDTO)) {
            return false;
        }

        BrokerDeskDTO brokerDeskDTO = (BrokerDeskDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, brokerDeskDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BrokerDeskDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", user=" + getUser() +
            ", broker=" + getBroker() +
            "}";
    }
}
