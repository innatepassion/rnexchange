package com.rnexchange.web.rest.broker;

import com.rnexchange.service.broker.BrokerScopeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 2 (T010): Broker traders controller stub guarded by BROKER_ADMIN.
 * Endpoints will be implemented in Phase 4.
 */
@RestController
@RequestMapping("/api/broker/traders")
@PreAuthorize("hasRole('BROKER_ADMIN')")
public class BrokerTradersResource {

    private final BrokerScopeService brokerScopeService;

    public BrokerTradersResource(BrokerScopeService brokerScopeService) {
        this.brokerScopeService = brokerScopeService;
    }
}
