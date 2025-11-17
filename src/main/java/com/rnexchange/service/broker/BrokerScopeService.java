package com.rnexchange.service.broker;

import static com.rnexchange.security.AuthoritiesConstants.BROKER_ADMIN;

import com.rnexchange.repository.BrokerDeskRepository;
import com.rnexchange.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Utilities for enforcing broker scoping and role checks.
 * Phase 2: foundational helper to be used by broker services/resources.
 */
@Service
@Transactional(readOnly = true)
public class BrokerScopeService {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerScopeService.class);

    private final BrokerDeskRepository brokerDeskRepository;

    public BrokerScopeService(BrokerDeskRepository brokerDeskRepository) {
        this.brokerDeskRepository = brokerDeskRepository;
    }

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
     * Resolve broker ID from parameter or current user's BrokerDesk.
     * If brokerId is provided, return it. Otherwise, resolve from the current user's BrokerDesk.
     *
     * @param brokerId the broker ID from request parameter (may be null)
     * @return the resolved broker ID
     * @throws IllegalArgumentException if brokerId is null and cannot be resolved from current user
     */
    public Long requireBrokerId(Long brokerId) {
        if (brokerId != null) {
            return brokerId;
        }

        // Resolve broker ID from current user's BrokerDesk
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalArgumentException("No authenticated user found"));

        LOG.debug("Resolving broker ID for user: {}", currentUserLogin);

        return brokerDeskRepository
            .findByUserLogin(currentUserLogin)
            .map(brokerDesk -> {
                if (brokerDesk.getBroker() == null) {
                    throw new IllegalArgumentException("User " + currentUserLogin + " has a BrokerDesk but no associated Broker");
                }
                Long resolvedBrokerId = brokerDesk.getBroker().getId();
                LOG.debug("Resolved broker ID {} for user {}", resolvedBrokerId, currentUserLogin);
                return resolvedBrokerId;
            })
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "brokerId is required for broker-scoped operations. " +
                    "Either provide brokerId parameter or ensure user " +
                    currentUserLogin +
                    " has a BrokerDesk"
                )
            );
    }
}
