package com.rnexchange.web.rest.broker;

import com.rnexchange.service.dto.BrokerSettlementSummary;
import com.rnexchange.service.settlement.BrokerSettlementService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
    private static final String CORRELATION_ID_KEY = "correlationId";

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
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        try {
            LOG.info("[correlationId={}] REST request to get broker settlements: from={}, to={}", correlationId, from, to);
            List<BrokerSettlementSummary> summaries = brokerSettlementService.getBrokerSettlements(from, to);
            LOG.debug("[correlationId={}] Found {} broker settlement summaries", correlationId, summaries.size());
            return ResponseEntity.ok().body(summaries);
        } catch (Exception e) {
            LOG.error("[correlationId={}] Failed to get broker settlements: {}", correlationId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
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
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        try {
            LOG.info("[correlationId={}] REST request to get broker summary HTML: brokerId={}, date={}", correlationId, brokerId, date);
            String html = brokerSettlementService.getBrokerSummaryHtml(brokerId, date);
            LOG.debug(
                "[correlationId={}] Successfully generated broker summary HTML for brokerId: {}, date: {}",
                correlationId,
                brokerId,
                date
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            return ResponseEntity.ok().headers(headers).body(html);
        } catch (Exception e) {
            LOG.error(
                "[correlationId={}] Failed to get broker summary HTML for brokerId {} and date {}: {}",
                correlationId,
                brokerId,
                date,
                e.getMessage(),
                e
            );
            throw e;
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}
