package com.rnexchange.service.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class BaselineSeedLoggingIT extends AbstractBaselineSeedIT {

    private static final Set<String> REQUIRED_FIELDS = Set.of(
        "phase",
        "entityType",
        "status",
        "durationMs",
        "failureReason",
        "actorId",
        "actorRole",
        "instrument",
        "outcome"
    );

    @Autowired
    private BaselineSeedService baselineSeedService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ListAppender<ILoggingEvent> serviceAppender;
    private ListAppender<ILoggingEvent> cleanupAppender;
    private ListAppender<ILoggingEvent> validationAppender;

    @BeforeEach
    void attachAppenders() {
        serviceAppender = attachAppender("com.rnexchange.service.seed.BaselineSeedService");
        cleanupAppender = attachAppender("com.rnexchange.config.BaselineSeedCleanupRunner");
        validationAppender = attachAppender("com.rnexchange.service.startup.BaselineValidationRunner");
    }

    @AfterEach
    void detachAppenders() {
        detachAppender("com.rnexchange.service.seed.BaselineSeedService", serviceAppender);
        detachAppender("com.rnexchange.config.BaselineSeedCleanupRunner", cleanupAppender);
        detachAppender("com.rnexchange.service.startup.BaselineValidationRunner", validationAppender);
    }

    @Test
    @Transactional
    void structuredLogsPresentAllRequiredFieldsForSuccessAndFailurePaths() {
        baselineSeedService.runBaselineSeedBlocking(BaselineSeedRequest.builder().invocationId(UUID.randomUUID()).build());

        List<JsonNode> successEvents = Stream.of(serviceAppender.list, cleanupAppender.list, validationAppender.list)
            .flatMap(List::stream)
            .map(ILoggingEvent::getFormattedMessage)
            .map(this::toJsonNode)
            .filter(json -> json.hasNonNull("phase"))
            .toList();

        assertThat(successEvents).isNotEmpty();
        successEvents.forEach(json -> REQUIRED_FIELDS.forEach(field -> assertThat(json.has(field)).isTrue()));

        clearCapturedLogs();
        Exchange nse = exchangeRepository.findAll().stream().filter(exchange -> "NSE".equals(exchange.getCode())).findFirst().orElseThrow();
        Instrument duplicate = new Instrument()
            .symbol("RELIANCE")
            .name("Reliance Duplicate")
            .assetClass(AssetClass.EQUITY)
            .exchangeCode(nse.getCode())
            .tickSize(new BigDecimal("0.05"))
            .lotSize(1L)
            .currency(Currency.INR)
            .status("ACTIVE")
            .exchange(nse);
        instrumentRepository.saveAndFlush(duplicate);

        assertThatThrownBy(() -> baselineSeedService.runBaselineSeedBlocking(BaselineSeedRequest.builder().force(true).build())
        ).isInstanceOf(BaselineSeedVerificationException.class);

        List<JsonNode> failureEvents = Stream.of(serviceAppender.list, cleanupAppender.list, validationAppender.list)
            .flatMap(List::stream)
            .map(ILoggingEvent::getFormattedMessage)
            .map(this::toJsonNode)
            .filter(json -> json.hasNonNull("status") && "FAILED".equalsIgnoreCase(json.get("status").asText()))
            .toList();

        assertThat(failureEvents).isNotEmpty();
        failureEvents.forEach(json -> REQUIRED_FIELDS.forEach(field -> assertThat(json.has(field)).isTrue()));
    }

    private void clearCapturedLogs() {
        serviceAppender.list.clear();
        cleanupAppender.list.clear();
        validationAppender.list.clear();
    }

    private ListAppender<ILoggingEvent> attachAppender(String loggerName) {
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detachAppender(String loggerName, ListAppender<ILoggingEvent> appender) {
        if (appender == null) {
            return;
        }
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        logger.detachAppender(appender);
        appender.stop();
    }

    private JsonNode toJsonNode(String message) {
        try {
            return objectMapper.readTree(message);
        } catch (Exception e) {
            throw new AssertionError("Expected JSON log message but got: " + message, e);
        }
    }
}
