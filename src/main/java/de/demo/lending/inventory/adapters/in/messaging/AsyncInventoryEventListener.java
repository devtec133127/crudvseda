package de.demo.lending.inventory.adapters.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.adapters.out.outbox.messaging.async.AsyncEventBus;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.InventoryRepository;
import de.demo.lending.inventory.application.ReserveBook;
import de.demo.lending.loan.domain.LoanId;
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
 * 
 * Aktiviert durch Profile "async".
 */
@Component
@Profile("async")
//@ConditionalOnProperty(value = "service.role", havingValue = "inventory")
public class AsyncInventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(AsyncInventoryEventListener.class);

    private final ObjectMapper om = new ObjectMapper();
    private final InventoryRepository repo;
    private final EventPublisher events;
    private final ReserveBook reserveBookUseCase;
    //private final SpringProcessedEventRepository processedRepo;
    private final AsyncEventBus eventBus;

    public AsyncInventoryEventListener(
            InventoryRepository repo,
            EventPublisher events,
            ReserveBook reserveBookUseCase,
            //SpringProcessedEventRepository processedRepo,
            AsyncEventBus eventBus) {
        this.repo = repo;
        this.events = events;
        this.reserveBookUseCase = reserveBookUseCase;
        //this.processedRepo = processedRepo;
        this.eventBus = eventBus;
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

        String consumer = "inventory";
        /*if (incomingEventId != null && processedRepo.existsByEventIdAndConsumer(incomingEventId, consumer)) {
            // already processed -> idempotent
            log.info("Skipping already processed event {} for consumer {}", incomingEventId, consumer);
            return;
        }*/

        // mandatory fields expected: loanId, bookId
        if (!node.has("loanId") || !node.has("bookTitle")) {
            log.warn("Received loan.requested without loanId/bookId: {}", json);
            return;
        }

        UUID loanUuid = UUID.fromString(node.get("loanId").asText());
        UUID userUuid = UUID.fromString(node.get("userId").asText());
        String bookTitle = node.get("bookTitle").asText();
        long durationDays = node.get("duration").asLong();
        String corralationId = node.get("corralationId").asText();
        String causationId = incomingEventId;


        Duration duration = Duration.ofDays(durationDays);

        reserveBookUseCase.handle(UserId.of(userUuid), LoanId.of(loanUuid), bookTitle, duration, corralationId, causationId);


    }
}
