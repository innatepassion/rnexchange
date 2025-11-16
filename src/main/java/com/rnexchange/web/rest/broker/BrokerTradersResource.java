package com.rnexchange.web.rest.broker;

import com.rnexchange.service.broker.BrokerScopeService;
import com.rnexchange.service.broker.BrokerTradersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final BrokerTradersService brokerTradersService;

    public BrokerTradersResource(BrokerScopeService brokerScopeService, BrokerTradersService brokerTradersService) {
        this.brokerScopeService = brokerScopeService;
        this.brokerTradersService = brokerTradersService;
    }

    @GetMapping
    @Operation(summary = "List traders for a broker (requires BROKER_ADMIN)")
    public ResponseEntity<Map<String, Object>> list(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size,
        @RequestParam(name = "brokerId", required = false) Long brokerId
    ) {
        return ResponseEntity.ok(brokerTradersService.listTraders(brokerId, page, size));
    }

    @GetMapping("/{traderId}")
    @Operation(summary = "Get trader details (requires BROKER_ADMIN)")
    public ResponseEntity<Map<String, Object>> details(
        @PathVariable("traderId") Long traderId,
        @RequestParam(name = "brokerId", required = false) Long brokerId
    ) {
        return ResponseEntity.ok(brokerTradersService.getTraderDetails(brokerId, traderId));
    }
}
