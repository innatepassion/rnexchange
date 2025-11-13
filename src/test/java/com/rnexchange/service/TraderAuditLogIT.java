package com.rnexchange.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.enumeration.OrderSide;
import com.rnexchange.domain.enumeration.OrderType;
import com.rnexchange.domain.enumeration.Tif;
import com.rnexchange.service.dto.TraderOrderRequest;
import com.rnexchange.service.seed.BaselineSeedService;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class TraderAuditLogIT extends com.rnexchange.service.seed.AbstractBaselineSeedIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Autowired
    private OrderService orderService;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        BaselineSeedRequest request = BaselineSeedRequest.builder().force(true).invocationId(UUID.randomUUID()).build();
        baselineSeedService.runBaselineSeedBlocking(request);

        Logger auditLogger = (Logger) LoggerFactory.getLogger("com.rnexchange.service.trading.TraderAuditLogger");
        listAppender = new ListAppender<>();
        listAppender.start();
        auditLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        if (listAppender != null) {
            listAppender.stop();
        }
        Logger auditLogger = (Logger) LoggerFactory.getLogger("com.rnexchange.service.trading.TraderAuditLogger");
        auditLogger.detachAppender(listAppender);
    }

    @Test
    void emitsStructuredAuditLogForSuccessfulOrder() {
        TraderOrderRequest request = TraderOrderRequest.builder()
            .traderLogin("trader-one")
            .instrumentSymbol("RELIANCE")
            .side(OrderSide.BUY)
            .type(OrderType.MARKET)
            .tif(Tif.DAY)
            .quantity(new BigDecimal("10"))
            .price(new BigDecimal("2200.00"))
            .build();

        orderService.submitTraderOrder(request);

        JsonNode payload = extractLatestAuditPayload().orElseThrow();
        assertThat(payload.get("actorId").asText()).isEqualTo("trader-one");
        assertThat(payload.get("actorRole").asText()).isEqualTo("TRADER");
        assertThat(payload.get("instrument").asText()).isEqualTo("RELIANCE");
        assertThat(payload.get("status").asText()).isEqualTo("ACCEPTED");
        assertThat(payload.get("outcome").asText()).isEqualTo("ACCEPTED");
    }

    @Test
    void emitsStructuredAuditLogForRejectedOrder() {
        TraderOrderRequest request = TraderOrderRequest.builder()
            .traderLogin("trader-one")
            .instrumentSymbol("RELIANCE")
            .side(OrderSide.BUY)
            .type(OrderType.MARKET)
            .tif(Tif.DAY)
            .quantity(new BigDecimal("10000"))
            .price(new BigDecimal("5000.00"))
            .build();

        Throwable thrown = catchThrowable(() -> orderService.submitTraderOrder(request));
        assertThat(thrown).isInstanceOf(InsufficientMarginException.class);

        JsonNode payload = extractLatestAuditPayload().orElseThrow();
        assertThat(payload.get("actorId").asText()).isEqualTo("trader-one");
        assertThat(payload.get("actorRole").asText()).isEqualTo("TRADER");
        assertThat(payload.get("instrument").asText()).isEqualTo("RELIANCE");
        assertThat(payload.get("status").asText()).isEqualTo("REJECTED");
        assertThat(payload.get("outcome").asText()).contains("Insufficient margin");
    }

    private Optional<JsonNode> extractLatestAuditPayload() {
        List<ILoggingEvent> events = listAppender.list;
        if (events.isEmpty()) {
            return Optional.empty();
        }
        String message = events.get(events.size() - 1).getFormattedMessage();
        try {
            return Optional.of(OBJECT_MAPPER.readTree(message));
        } catch (Exception e) {
            throw new AssertionError("Failed to parse audit log message as JSON: " + message, e);
        }
    }
}
