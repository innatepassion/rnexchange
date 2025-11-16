package com.rnexchange.web.rest.broker;

import com.rnexchange.service.broker.BrokerOverviewService;
import com.rnexchange.service.broker.BrokerScopeService;
import com.rnexchange.service.dto.BrokerOverviewDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 2 (T010): Broker overview controller stub guarded by BROKER_ADMIN.
 * Endpoints will be implemented in Phase 3.
 */
@RestController
@RequestMapping("/api/broker/overview")
@PreAuthorize("hasRole('BROKER_ADMIN')")
public class BrokerOverviewResource {

    private final BrokerScopeService brokerScopeService;
    private final BrokerOverviewService brokerOverviewService;

    public BrokerOverviewResource(BrokerScopeService brokerScopeService, BrokerOverviewService brokerOverviewService) {
        this.brokerScopeService = brokerScopeService;
        this.brokerOverviewService = brokerOverviewService;
    }

    @GetMapping
    public ResponseEntity<BrokerOverviewDTO> getOverview(@RequestParam("brokerId") Long brokerId) {
        brokerScopeService.assertBrokerAdmin();
        Long scopedBrokerId = brokerScopeService.requireBrokerId(brokerId);
        BrokerOverviewDTO overview = brokerOverviewService.computeOverview(scopedBrokerId);
        return ResponseEntity.ok(overview);
    }
}
