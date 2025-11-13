package com.rnexchange.service.seed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BaselineSeedStructuredLogger {

    private final ObjectMapper objectMapper;

    public BaselineSeedStructuredLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String build(
        String phase,
        String entityType,
        String status,
        long durationMs,
        String failureReason,
        UUID actorId,
        String actorRole,
        String instrument,
        String outcome
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("phase", phase);
        payload.put("entityType", entityType);
        payload.put("status", status);
        payload.put("durationMs", durationMs);
        payload.put("failureReason", failureReason);
        payload.put("actorId", actorId != null ? actorId.toString() : "N/A");
        payload.put("actorRole", actorRole);
        payload.put("instrument", instrument);
        payload.put("outcome", outcome);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize baseline seed log payload", e);
        }
    }
}
