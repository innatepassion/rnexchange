package com.rnexchange.service.marketdata.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FeedLifecycleAuditListener {

    private static final Logger LOG = LoggerFactory.getLogger(FeedLifecycleAuditListener.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("com.rnexchange.service.trading.TraderAuditLogger");

    private final ObjectMapper objectMapper;

    public FeedLifecycleAuditListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void onFeedStarted(FeedStartedEvent event) {
        writeEntry("FEED_STARTED", event.exchangeCodes(), event.timestamp(), event.triggeredBy(), null);
    }

    @EventListener
    public void onFeedStopped(FeedStoppedEvent event) {
        writeEntry("FEED_STOPPED", event.exchangeCodes(), event.timestamp(), event.triggeredBy(), event.reason());
    }

    private void writeEntry(String action, List<String> exchanges, Instant feedTimestamp, String triggeredBy, String reason) {
        Map<String, Object> payload = new LinkedHashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        payload.put("timestamp", Instant.now().toString());
        payload.put("action", action);
        payload.put("feedTimestamp", feedTimestamp.toString());
        payload.put("triggeredBy", triggeredBy);
        payload.put("exchanges", exchanges);
        if (StringUtils.hasText(reason)) {
            payload.put("reason", reason);
        }
        payload.put("actorId", resolveActorId(authentication, triggeredBy));
        payload.put("actorRole", resolveActorRole(authentication));

        try {
            AUDIT_LOG.info(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            LOG.warn("Unable to serialize feed lifecycle audit payload", e);
        }
    }

    private String resolveActorId(Authentication authentication, String fallback) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return StringUtils.hasText(fallback) ? fallback : "system";
    }

    private String resolveActorRole(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority != null && StringUtils.hasText(authority.getAuthority())) {
                    return normalizeRole(authority.getAuthority());
                }
            }
        }
        return "SYSTEM";
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return "UNKNOWN";
        }
        if (role.startsWith("ROLE_")) {
            return role.substring(5);
        }
        return role;
    }
}
