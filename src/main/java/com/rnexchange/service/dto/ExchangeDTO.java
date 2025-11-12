package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.ExchangeStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.Exchange} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExchangeDTO implements Serializable {

    private Long id;

    @NotNull
    private String code;

    @NotNull
    private String name;

    @NotNull
    private String timezone;

    @NotNull
    private ExchangeStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public ExchangeStatus getStatus() {
        return status;
    }

    public void setStatus(ExchangeStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExchangeDTO)) {
            return false;
        }

        ExchangeDTO exchangeDTO = (ExchangeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, exchangeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExchangeDTO{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", name='" + getName() + "'" +
            ", timezone='" + getTimezone() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
