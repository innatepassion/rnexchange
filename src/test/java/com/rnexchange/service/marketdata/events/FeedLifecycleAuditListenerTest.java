package com.rnexchange.service.marketdata.events;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class FeedLifecycleAuditListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private FeedLifecycleAuditListener listener;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        listener = new FeedLifecycleAuditListener(objectMapper);
        Logger auditLogger = (Logger) LoggerFactory.getLogger("com.rnexchange.service.trading.TraderAuditLogger");
        listAppender = new ListAppender<>();
        listAppender.start();
        auditLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        Logger auditLogger = (Logger) LoggerFactory.getLogger("com.rnexchange.service.trading.TraderAuditLogger");
        auditLogger.detachAppender(listAppender);
        listAppender.stop();
        SecurityContextHolder.clearContext();
    }

    @Test
    void logsFeedStartedEventWithAuthenticatedUser() throws Exception {
        SecurityContextHolder.getContext()
            .setAuthentication(
                new UsernamePasswordAuthenticationToken("exchange_op", "n/a", List.of(new SimpleGrantedAuthority("ROLE_EXCHANGE_OPERATOR")))
            );

        listener.onFeedStarted(new FeedStartedEvent(List.of("NSE", "BSE"), "manual", Instant.parse("2025-11-14T10:00:00Z")));

        JsonNode payload = latestPayload();
        assertThat(payload.get("action").asText()).isEqualTo("FEED_STARTED");
        assertThat(payload.get("actorId").asText()).isEqualTo("exchange_op");
        assertThat(payload.get("actorRole").asText()).isEqualTo("EXCHANGE_OPERATOR");
        assertThat(payload.get("exchanges")).hasSize(2);
    }

    @Test
    void logsFeedStoppedEventWithSystemFallback() throws Exception {
        SecurityContextHolder.clearContext();

        listener.onFeedStopped(new FeedStoppedEvent(List.of("NSE"), "system-auto", Instant.parse("2025-11-14T11:00:00Z"), "HOLIDAY_GUARD"));

        JsonNode payload = latestPayload();
        assertThat(payload.get("action").asText()).isEqualTo("FEED_STOPPED");
        assertThat(payload.get("actorId").asText()).isEqualTo("system-auto");
        assertThat(payload.get("actorRole").asText()).isEqualTo("SYSTEM");
        assertThat(payload.get("reason").asText()).isEqualTo("HOLIDAY_GUARD");
    }

    private JsonNode latestPayload() throws Exception {
        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).isNotEmpty();
        String message = events.get(events.size() - 1).getFormattedMessage();
        return objectMapper.readTree(message);
    }
}
