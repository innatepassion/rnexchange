package com.rnexchange.service.dto;

import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.KycStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.rnexchange.domain.TraderProfile} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TraderProfileDTO implements Serializable {

    private Long id;

    @NotNull
    private String displayName;

    @NotNull
    private String email;

    private String mobile;

    @NotNull
    private KycStatus kycStatus;

    @NotNull
    private AccountStatus status;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TraderProfileDTO)) {
            return false;
        }

        TraderProfileDTO traderProfileDTO = (TraderProfileDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, traderProfileDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TraderProfileDTO{" +
            "id=" + getId() +
            ", displayName='" + getDisplayName() + "'" +
            ", email='" + getEmail() + "'" +
            ", mobile='" + getMobile() + "'" +
            ", kycStatus='" + getKycStatus() + "'" +
            ", status='" + getStatus() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
