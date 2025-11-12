package com.rnexchange.domain;

import com.rnexchange.domain.enumeration.ExchangeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * A Exchange.
 */
@Entity
@Table(name = "exchange")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Exchange implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false)
    private String code;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "timezone", nullable = false)
    private String timezone;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExchangeStatus status;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Exchange id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public Exchange code(String code) {
        this.setCode(code);
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public Exchange name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public Exchange timezone(String timezone) {
        this.setTimezone(timezone);
        return this;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public ExchangeStatus getStatus() {
        return this.status;
    }

    public Exchange status(ExchangeStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(ExchangeStatus status) {
        this.status = status;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Exchange)) {
            return false;
        }
        return getId() != null && getId().equals(((Exchange) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Exchange{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", name='" + getName() + "'" +
            ", timezone='" + getTimezone() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
