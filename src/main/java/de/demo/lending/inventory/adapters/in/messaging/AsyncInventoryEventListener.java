package de.demo.lending.inventory.adapters.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.adapters.out.outbox.messaging.async.AsyncEventBus;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.inventory.application.InventoryRepository;
import de.demo.lending.loan.domain.LoanId;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
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
    //private final SpringProcessedEventRepository processedRepo;
    private final AsyncEventBus eventBus;

    public AsyncInventoryEventListener(
            InventoryRepository repo,
            EventPublisher events,
            //SpringProcessedEventRepository processedRepo,
            AsyncEventBus eventBus) {
        this.repo = repo;
        this.events = events;
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
        String bookTitle = node.get("bookTitle").asText();

        LoanId loanId = LoanId.of(loanUuid);
        //BookId bookId = BookId.of(bookUuid);

        // Try reserve via port
        Optional<CopyId> reserved = repo.reserveFirstAvailable(bookId);

        String correlationId = node.has("correlationId") ? node.get("correlationId").asText() : UUID.randomUUID().toString();
        String causationId = incomingEventId != null ? incomingEventId : null;

        // Reserve-Policy: pick first AVAILABLE
        if (reserved.isPresent()) {
            // Erfolg: publish inventory.reserved.v1
            Map<String, Object> payload = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "occurredAt", Instant.now().toString(),
                    "correlationId", correlationId,
                    "causationId", causationId,
                    "loanId", loanId.toString(),
                    "copyId", reserved.get().value().toString());
            events.enqueue(Topics.INVENTORY_RESERVED_V1, payload);
            log.info("Reserved copy {} for loan {}", reserved.get().value(), loanUuid);
        } else {
            // Keine verfügbare Kopie: publish inventory.rejected.v1
            Map<String, Object> payload = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "occurredAt", Instant.now().toString(),
                    "correlationId", node.path("correlationId").asText(UUID.randomUUID().toString()),
                    "causationId", incomingEventId != null ? incomingEventId : null,
                    "loanId", loanId.toString(),
                    "reason", "NO_COPY_AVAILABLE"
            );
            events.enqueue(Topics.INVENTORY_REJECTED_V1, payload);
            log.info("No copy available for book {} (loan {})", bookUuid, loanUuid);
        }

        // speichern: processed_event (Idempotenz)
        /*if (incomingEventId != null) {
            processedRepo.save(new ProcessedEventEntity(
                    incomingEventId, consumer, Instant.now()
            ));
        }*/
    }
}
