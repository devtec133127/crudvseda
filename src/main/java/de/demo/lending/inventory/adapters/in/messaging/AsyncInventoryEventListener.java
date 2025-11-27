package de.demo.lending.inventory.adapters.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.async.AsyncEventBus;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventRepository;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventUtil;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.ReserveBookService;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.read.application.port.LoanStatusReadPort;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Async-basierter Event Listener für Inventory (ohne Kafka).
 * Registriert sich beim AsyncEventBus statt @KafkaListener.
 * <p>
 * Aktiviert durch Profile "async".
 */
@Component
@Profile("async")
public class AsyncInventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(AsyncInventoryEventListener.class);

    private final ObjectMapper om = new ObjectMapper();
    private final ReserveBookService reserveBookUseCase;
    private final AsyncEventBus eventBus;
    private final ProcessedEventRepository processedRepo;
    private final LoanStatusReadPort loanStatusReadPort;
    private final DemoEventSSEPublisher uiPublisher;

    public AsyncInventoryEventListener(
            ReserveBookService reserveBookUseCase,
            AsyncEventBus eventBus,
            ProcessedEventRepository processedRepo,
            LoanStatusReadPort loanStatusReadPort,
            DemoEventSSEPublisher uiPublisher) {
        this.reserveBookUseCase = reserveBookUseCase;
        this.eventBus = eventBus;
        this.processedRepo = processedRepo;
        this.loanStatusReadPort = loanStatusReadPort;
        this.uiPublisher = uiPublisher;
    }

    @PostConstruct
    public void subscribeToEvents() {
        // Registriere Handler beim Event Bus
        eventBus.subscribe(Topics.LOAN_REQUESTED_V1, this::onLoanRequested);
        log.info("Subscribed to {} via AsyncEventBus", Topics.LOAN_REQUESTED_V1);
    }

    @Transactional
    public void onLoanRequested(String json) throws Exception {
        log.info("Received async event on {}: {}", Topics.LOAN_REQUESTED_V1, json);
        JsonNode node = om.readTree(json);
        String incomingEventId = node.has("eventId") ? node.get("eventId").asText(null) : null;

        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        ProcessedEventUtil.checkEvent(AsyncInventoryEventListener.class, incomingEventId);

        // mandatory fields expected: loanId, bookId
        if (!node.has("loanId") || !node.has("bookTitle")) {
            log.warn("Received loan.requested without loanId/bookId: {}", json);
            return;
        }

        UUID loanUuid = UUID.fromString(node.get("loanId").asText());
        UUID userUuid = UUID.fromString(node.get("userId").asText());
        String bookTitle = node.get("bookTitle").asText();
        long durationDays = node.get("duration").asLong();
        String corralationId = node.get("correlationId").asText();
        String causationId = incomingEventId;

        uiPublisher.publishLoanCreatedToUI(loanUuid, userUuid, bookTitle, durationDays);

        Duration duration = Duration.ofDays(durationDays);

        reserveBookUseCase.handle(UserId.of(userUuid), LoanId.of(loanUuid), bookTitle, duration, corralationId, causationId);

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(AsyncInventoryEventListener.class, incomingEventId);
    }
}