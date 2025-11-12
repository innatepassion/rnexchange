package com.rnexchange.domain;

import com.rnexchange.domain.enumeration.IntegrationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * A ExchangeIntegration.
 */
@Entity
@Table(name = "exchange_integration")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ExchangeIntegration implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "api_secret")
    private String apiSecret;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IntegrationStatus status;

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @ManyToOne(fetch = FetchType.LAZY)
    private Exchange exchange;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ExchangeIntegration id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return this.provider;
    }

    public ExchangeIntegration provider(String provider) {
        this.setProvider(provider);
        return this;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public ExchangeIntegration apiKey(String apiKey) {
        this.setApiKey(apiKey);
        return this;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return this.apiSecret;
    }

    public ExchangeIntegration apiSecret(String apiSecret) {
        this.setApiSecret(apiSecret);
        return this;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public IntegrationStatus getStatus() {
        return this.status;
    }

    public ExchangeIntegration status(IntegrationStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(IntegrationStatus status) {
        this.status = status;
    }

    public Instant getLastHeartbeat() {
        return this.lastHeartbeat;
    }

    public ExchangeIntegration lastHeartbeat(Instant lastHeartbeat) {
        this.setLastHeartbeat(lastHeartbeat);
        return this;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Exchange getExchange() {
        return this.exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public ExchangeIntegration exchange(Exchange exchange) {
        this.setExchange(exchange);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExchangeIntegration)) {
            return false;
        }
        return getId() != null && getId().equals(((ExchangeIntegration) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ExchangeIntegration{" +
            "id=" + getId() +
            ", provider='" + getProvider() + "'" +
            ", apiKey='" + getApiKey() + "'" +
            ", apiSecret='" + getApiSecret() + "'" +
            ", status='" + getStatus() + "'" +
            ", lastHeartbeat='" + getLastHeartbeat() + "'" +
            "}";
    }
}
