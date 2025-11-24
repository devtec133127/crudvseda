package de.demo.lending.inventory.adapters.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventEntity;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventRepository;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.InventoryRepository;
import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.inventory.application.dto.event.BookReservedEventMapper;
import de.demo.lending.inventory.domain.ReservationId;
import de.demo.lending.inventory.domain.event.BookReserved;
import de.demo.lending.loan.domain.LoanId;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static de.demo.lending.common.events.Topics.INVENTORY_RESERVED_V1;

/**
 * Kafka-basierter Event Listener für Inventory.
 * <p>
 * Aktiviert durch Profile "kafka" (Standard für Produktion).
 * Wird durch AsyncInventoryEventListener ersetzt bei Profile "async".
 */
@Component
public class KafkaInventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaInventoryEventListener.class);

    private final ObjectMapper om = new ObjectMapper();
    private final InventoryRepository repo;
    private final EventPublisher events;
    private final ProcessedEventRepository processedRepo;

    public KafkaInventoryEventListener(InventoryRepository repo,
                                       EventPublisher events,
                                       ProcessedEventRepository processedRepo) {
        this.repo = repo;
        this.events = events;
        this.processedRepo = processedRepo;
    }

    @KafkaListener(
            topics = {Topics.LOAN_REQUESTED_V1},
            groupId = "inventory")
    @Transactional
    public void onLoanRequested(String json,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                @Header(KafkaHeaders.OFFSET) long offset) throws Exception {

        JsonNode node = om.readTree(json);
        String incomingEventId = node.has("eventId") ? node.get("eventId").asText(null) : null;

        // ########## Indempotenz - Event schon verarbeitet? ##########
        String consumer = AsyncInventoryEventListener.class.getCanonicalName();
        if (incomingEventId != null && processedRepo.existsByEventIdAndConsumer(incomingEventId, consumer)) {
            // already processed -> idempotent
            log.info("Skipping already processed event {} for consumer {}", incomingEventId, consumer);
            return;
        }

        // mandatory fields expected: loanId, bookId
        if (!node.has("loanId") || !node.has("bookId")) {
            log.warn("Received loan.requested without loanId/bookId: {}", json);
            return;
        }

        UUID loanUuid = UUID.fromString(node.get("loanId").asText());
        UUID userUuid = UUID.fromString(node.get("userId").asText());
        String bookUuid = node.get("bookId").asText();

        LoanId loanId = LoanId.of(loanUuid);
        UserId userId = UserId.of(userUuid);
        BookId bookId = BookId.of(bookUuid);

        // Try reserve via port
        Optional<CopyId> reserved = repo.reserveFirstAvailable(bookId);


        String correlationId = node.has("correlationId") ? node.get("correlationId").asText() : UUID.randomUUID().toString();
        String causationId = incomingEventId != null ? incomingEventId : null;

        // Reserve-Policy: pick first AVAILABLE
        if (reserved.isPresent()) {
            // Erfolg: publish inventory.reserved.v1
            BookReserved bookReserved = new BookReserved(loanId, correlationId, causationId,
                    reserved.get(), "title", userId, bookId, ReservationId.newId());

            BookReservedPayload payload = BookReservedEventMapper.toPayload((BookReserved) bookReserved,
                    correlationId, causationId);
            log.info("Publishing event to topic {}: {}", INVENTORY_RESERVED_V1, payload);
            events.enqueue(Topics.INVENTORY_RESERVED_V1, payload);
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

            // TODO: publish inventory.reserved.failed.v1
            /*BookReserved bookReserved = new BookReserved(loanId, correlationId, causationId,
                    reserved.get(), "title", userId, bookId, ReservationId.newId());

            BookReservedPayload payload = BookReservedEventMapper.toPayload((BookReserved) bookReserved,
                    correlationId, causationId);
            log.info("Publishing event to topic {}: {}", INVENTORY_RESERVED_V1, payload);

            events.enqueue(Topics.INVENTORY_REJECTED_V1, payload);
            log.info("No copy available for book {} (loan {})", bookUuid, loanUuid);*/
        }

        // ########## Indempotenz - Event verarbeitet -> spciehern  ##########
        if (incomingEventId != null) {
            ProcessedEventEntity eventEntity = new ProcessedEventEntity();
            eventEntity.setEventId(incomingEventId);
            eventEntity.setConsumer(consumer);
            eventEntity.setReceivedAt(Instant.now());
            processedRepo.save(eventEntity);
        }
    }
}
