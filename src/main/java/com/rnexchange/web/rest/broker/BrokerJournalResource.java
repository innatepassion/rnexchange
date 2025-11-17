package com.rnexchange.web.rest.broker;

import com.rnexchange.service.broker.BrokerJournalService;
import com.rnexchange.service.dto.broker.JournalRequestDTO;
import com.rnexchange.service.dto.broker.JournalResultDTO;
import java.util.UUID;
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
public class BrokerJournalResource {

    private final BrokerJournalService brokerJournalService;

    public BrokerJournalResource(BrokerJournalService brokerJournalService) {
        this.brokerJournalService = brokerJournalService;
    }

    @PostMapping("/{tradingAccountId}/journal")
    public ResponseEntity<JournalResultDTO> createJournalEntry(
        @PathVariable("tradingAccountId") UUID tradingAccountId,
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody JournalRequestDTO request
    ) {
        JournalResultDTO result = brokerJournalService.applyJournal(tradingAccountId, idempotencyKey, request, null);
        return ResponseEntity.ok(result);
    }
}
