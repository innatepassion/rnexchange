package com.rnexchange.web.rest.broker;

import com.rnexchange.service.dto.BrokerSettlementSummary;
import com.rnexchange.service.settlement.BrokerSettlementService;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing broker settlement summaries.
 */
@RestController
@RequestMapping("/api/broker")
public class BrokerSettlementResource {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerSettlementResource.class);

    private final BrokerSettlementService brokerSettlementService;

    public BrokerSettlementResource(BrokerSettlementService brokerSettlementService) {
        this.brokerSettlementService = brokerSettlementService;
    }

    /**
     * {@code GET  /broker/settlements} : get all broker settlement summaries for the authenticated broker admin.
     *
     * @param from optional start date (inclusive)
     * @param to optional end date (inclusive)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of broker settlement summaries in body.
     */
    @GetMapping("/settlements")
    @PreAuthorize("hasRole('BROKER_ADMIN')")
    public ResponseEntity<List<BrokerSettlementSummary>> listBrokerSettlements(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LOG.debug("REST request to get broker settlements: from={}, to={}", from, to);
        List<BrokerSettlementSummary> summaries = brokerSettlementService.getBrokerSettlements(from, to);
        return ResponseEntity.ok().body(summaries);
    }

    /**
     * {@code GET  /broker/settlements/{brokerId}/summary} : get HTML summary for a specific broker and date.
     *
     * @param brokerId the broker ID
     * @param date the reference date
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and HTML content in body.
     */
    @GetMapping("/settlements/{brokerId}/summary")
    @PreAuthorize("hasRole('BROKER_ADMIN')")
    public ResponseEntity<String> getBrokerSummaryHtml(
        @PathVariable("brokerId") Long brokerId,
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LOG.debug("REST request to get broker summary HTML: brokerId={}, date={}", brokerId, date);
        String html = brokerSettlementService.getBrokerSummaryHtml(brokerId, date);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return ResponseEntity.ok().headers(headers).body(html);
    }
}
