package com.rnexchange.service.trading;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.service.dto.MarginAssessment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TraderAuditStructuredLogger {

    private final ObjectMapper objectMapper;

    public TraderAuditStructuredLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String build(TraderAuditPayload payload) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("timestamp", Instant.now().toString());
        json.put("actorId", payload.actorId());
        json.put("actorRole", payload.actorRole());
        json.put("instrument", payload.instrument());
        json.put("status", payload.status());
        json.put("outcome", payload.outcome());
        json.put("quantity", safeToString(payload.quantity()));
        json.put("price", safeToString(payload.price()));

        MarginAssessment assessment = payload.assessment();
        if (assessment != null) {
            json.put("initialRequirement", assessment.initialRequirement().toPlainString());
            json.put("maintenanceRequirement", assessment.maintenanceRequirement().toPlainString());
            json.put("availableBalance", assessment.availableBalance().toPlainString());
            json.put("remainingBalance", assessment.remainingBalance().toPlainString());
            json.put("marginSufficient", assessment.sufficient());
        }

        try {
            return objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize trader audit payload", e);
        }
    }

    private String safeToString(BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_UP).toPlainString() : null;
    }

    public record TraderAuditPayload(
        String actorId,
        String actorRole,
        String instrument,
        String status,
        String outcome,
        MarginAssessment assessment,
        BigDecimal quantity,
        BigDecimal price
    ) {}
}
