package com.rnexchange.web.rest.broker;

import com.rnexchange.service.broker.BrokerJournalService;
import com.rnexchange.service.dto.broker.JournalRequestDTO;
import com.rnexchange.service.dto.broker.JournalResultDTO;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for broker funds journal entries.
 *
 * Exposes POST /api/broker/traders/{tradingAccountId}/journal to align with
 * the M6 broker journal contract and delegates to {@link BrokerJournalService}.
 */
@RestController
@RequestMapping("/api/broker/traders")
@PreAuthorize("hasRole('BROKER_ADMIN')")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BrokerJournalResource {

    private final BrokerJournalService brokerJournalService;

    public BrokerJournalResource(BrokerJournalService brokerJournalService) {
        this.brokerJournalService = brokerJournalService;
    }

    @PostMapping("/{tradingAccountId}/journal")
    public ResponseEntity<JournalResultDTO> createJournalEntry(
        @PathVariable("tradingAccountId") Long tradingAccountId,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @RequestBody JournalRequestDTO request
    ) {
        String idempotency = idempotencyKey != null ? idempotencyKey : "ui-" + System.currentTimeMillis();
        JournalResultDTO result = brokerJournalService.applyJournal(tradingAccountId, idempotency, request, null);
        return ResponseEntity.ok(result);
    }
}
