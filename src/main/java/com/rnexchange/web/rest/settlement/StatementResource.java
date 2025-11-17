package com.rnexchange.web.rest.settlement;

import com.rnexchange.service.dto.StatementSummary;
import com.rnexchange.service.settlement.StatementService;
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
 * REST controller for managing trader statements.
 */
@RestController
@RequestMapping("/api")
public class StatementResource {

    private static final Logger LOG = LoggerFactory.getLogger(StatementResource.class);
    private static final String CORRELATION_ID_KEY = "correlationId";

    private final StatementService statementService;

    public StatementResource(StatementService statementService) {
        this.statementService = statementService;
    }

    /**
     * {@code GET  /statements} : get all statements for the authenticated trader.
     *
     * @param fromDate optional start date (inclusive)
     * @param toDate optional end date (inclusive)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of statements in body.
     */
    @GetMapping("/statements")
    @PreAuthorize("hasRole('TRADER')")
    public ResponseEntity<List<StatementSummary>> listMyStatements(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        try {
            LOG.info("[correlationId={}] REST request to get statements for trader: from={}, to={}", correlationId, from, to);
            List<StatementSummary> statements = statementService.getStatementsForTrader(from, to);
            LOG.debug("[correlationId={}] Found {} statements for trader", correlationId, statements.size());
            return ResponseEntity.ok().body(statements);
        } catch (Exception e) {
            LOG.error("[correlationId={}] Failed to get statements for trader: {}", correlationId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    /**
     * {@code GET  /statements/{statementId}/html} : get HTML statement for a specific statement.
     *
     * @param statementId the statement identifier
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and HTML content in body.
     */
    @GetMapping("/statements/{statementId}/html")
    @PreAuthorize("hasRole('TRADER')")
    public ResponseEntity<String> getStatementHtml(@PathVariable("statementId") Long statementId) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        try {
            LOG.info("[correlationId={}] REST request to get HTML statement: {}", correlationId, statementId);
            String html = statementService.getStatementHtml(statementId);
            LOG.debug("[correlationId={}] Successfully generated HTML statement for statementId: {}", correlationId, statementId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            return ResponseEntity.ok().headers(headers).body(html);
        } catch (Exception e) {
            LOG.error(
                "[correlationId={}] Failed to get HTML statement for statementId {}: {}",
                correlationId,
                statementId,
                e.getMessage(),
                e
            );
            throw e;
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}
