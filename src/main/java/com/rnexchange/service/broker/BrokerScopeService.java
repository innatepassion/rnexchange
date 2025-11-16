package com.rnexchange.service.broker;

import static com.rnexchange.security.AuthoritiesConstants.BROKER_ADMIN;

import com.rnexchange.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Utilities for enforcing broker scoping and role checks.
 * Phase 2: foundational helper to be used by broker services/resources.
 */
@Service
public class BrokerScopeService {

    /**
     * Ensure the current user has the BROKER_ADMIN role.
     * Throws AccessDeniedException otherwise.
     */
    public void assertBrokerAdmin() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(BROKER_ADMIN)) {
            throw new AccessDeniedException("Broker Admin role required");
        }
    }

    /**
     * For now, require that brokerId is explicitly provided by caller.
     * Later phases may resolve broker from authenticated principal or header.
     */
    public Long requireBrokerId(Long brokerId) {
        if (brokerId == null) {
            throw new IllegalArgumentException("brokerId is required for broker-scoped operations");
        }
        return brokerId;
    }
}
