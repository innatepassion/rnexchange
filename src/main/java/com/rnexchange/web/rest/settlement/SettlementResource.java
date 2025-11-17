package com.rnexchange.web.rest.settlement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.domain.SettlementBatch;
import com.rnexchange.domain.enumeration.SettlementKind;
import com.rnexchange.repository.SettlementBatchRepository;
import com.rnexchange.service.dto.SettlementBatchDTO;
import com.rnexchange.service.mapper.SettlementBatchMapper;
import com.rnexchange.service.settlement.SettlementService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements")
public class SettlementResource {

    private static final Logger LOG = LoggerFactory.getLogger(SettlementResource.class);
    private static final String CORRELATION_ID_KEY = "correlationId";

    private final SettlementService settlementService;
    private final SettlementBatchRepository settlementBatchRepository;
    private final SettlementBatchMapper settlementBatchMapper;
    private final ObjectMapper objectMapper;

    public SettlementResource(
        SettlementService settlementService,
        SettlementBatchRepository settlementBatchRepository,
        SettlementBatchMapper settlementBatchMapper,
        ObjectMapper objectMapper
    ) {
        this.settlementService = settlementService;
        this.settlementBatchRepository = settlementBatchRepository;
        this.settlementBatchMapper = settlementBatchMapper;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/eod")
    @PreAuthorize("hasRole('EXCHANGE_OPERATOR')")
    public ResponseEntity<SettlementBatchDTO> runEod(@RequestParam("date") String date) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        try {
            LOG.info("[correlationId={}] EOD settlement requested for date: {}", correlationId, date);
            LocalDate tradeDate = LocalDate.parse(date);

            // Run settlement asynchronously (for now, synchronously - can be enhanced later)
            LOG.debug("[correlationId={}] Starting EOD settlement process for date: {}", correlationId, tradeDate);
            settlementService.runEod(tradeDate);

            // Find the created batch
            SettlementBatch batch = settlementBatchRepository
                .findAll()
                .stream()
                .filter(b -> b.getRefDate().equals(tradeDate) && b.getKind() == SettlementKind.EOD)
                .max((a, b) -> Long.compare(b.getId(), a.getId()))
                .orElseThrow(() -> new RuntimeException("Settlement batch not found after creation"));

            SettlementBatchDTO dto = settlementBatchMapper.toDto(batch);
            enrichDtoWithMetrics(dto, batch);

            LOG.info(
                "[correlationId={}] EOD settlement completed successfully. Batch ID: {}, Date: {}",
                correlationId,
                batch.getId(),
                tradeDate
            );
            return ResponseEntity.accepted().body(dto);
        } catch (Exception e) {
            LOG.error("[correlationId={}] EOD settlement failed for date {}: {}", correlationId, date, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('EXCHANGE_OPERATOR')")
    public ResponseEntity<List<SettlementBatchDTO>> list(@RequestParam("from") String from, @RequestParam("to") String to) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        try {
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);

            LOG.info("[correlationId={}] Listing settlement batches from {} to {}", correlationId, fromDate, toDate);

            List<SettlementBatch> batches = settlementBatchRepository
                .findAll()
                .stream()
                .filter(b -> b.getKind() == SettlementKind.EOD && !b.getRefDate().isBefore(fromDate) && !b.getRefDate().isAfter(toDate))
                .sorted((a, b) -> b.getRefDate().compareTo(a.getRefDate()))
                .collect(Collectors.toList());

            List<SettlementBatchDTO> dtos = batches
                .stream()
                .map(batch -> {
                    SettlementBatchDTO dto = settlementBatchMapper.toDto(batch);
                    enrichDtoWithMetrics(dto, batch);
                    return dto;
                })
                .collect(Collectors.toList());

            LOG.debug("[correlationId={}] Found {} settlement batches in date range", correlationId, dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            LOG.error("[correlationId={}] Failed to list settlement batches: {}", correlationId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    private void enrichDtoWithMetrics(SettlementBatchDTO dto, SettlementBatch batch) {
        // Parse remarks JSON to extract metrics
        if (batch.getRemarks() != null && !batch.getRemarks().isEmpty()) {
            try {
                JsonNode remarks = objectMapper.readTree(batch.getRemarks());
                if (remarks.has("accountsProcessed")) {
                    dto.setAccountsProcessed(remarks.get("accountsProcessed").asInt());
                }
                if (remarks.has("positionsProcessed")) {
                    dto.setPositionsProcessed(remarks.get("positionsProcessed").asInt());
                }
                if (remarks.has("netPnl")) {
                    dto.setNetPnl(remarks.get("netPnl").decimalValue());
                }
            } catch (Exception e) {
                LOG.warn("Failed to parse remarks JSON for batch {}: {}", batch.getId(), e.getMessage());
            }
        }
    }
}
