package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.IntegrationStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.ExchangeIntegration} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExchangeIntegrationDTO implements Serializable {

    private Long id;

    @NotNull
    private String provider;

    private String apiKey;

    private String apiSecret;

    @NotNull
    private IntegrationStatus status;

    private Instant lastHeartbeat;

    private ExchangeDTO exchange;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public IntegrationStatus getStatus() {
        return status;
    }

    public void setStatus(IntegrationStatus status) {
        this.status = status;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
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
        if (!(o instanceof ExchangeIntegrationDTO)) {
            return false;
        }

        ExchangeIntegrationDTO exchangeIntegrationDTO = (ExchangeIntegrationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, exchangeIntegrationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExchangeIntegrationDTO{" +
            "id=" + getId() +
            ", provider='" + getProvider() + "'" +
            ", apiKey='" + getApiKey() + "'" +
            ", apiSecret='" + getApiSecret() + "'" +
            ", status='" + getStatus() + "'" +
            ", lastHeartbeat='" + getLastHeartbeat() + "'" +
            ", exchange=" + getExchange() +
            "}";
    }
}
