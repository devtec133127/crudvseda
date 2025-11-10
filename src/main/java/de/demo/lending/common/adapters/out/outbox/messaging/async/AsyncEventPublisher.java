package de.demo.lending.common.adapters.out.outbox.messaging.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.adapters.out.outbox.messaging.OutboxMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * In-Memory Event Publisher für lokale Performance-Tests.
 * Nutzt Spring's @Async statt Kafka → kein Kafka-Overhead (serialization, network, offset management).
 * <p>
 * Austauschbar mit OutboxEventPublisher durch Spring Profile "async".
 */
@Component
@Profile("async")
public class AsyncEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AsyncEventPublisher.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AsyncEventBus eventBus;
    private final OutboxMarker marker;

    public AsyncEventPublisher(AsyncEventBus eventBus, OutboxMarker marker) {
        this.eventBus = eventBus;
        this.marker = marker;
    }

    @Override
    @Async  // Asynchrone Verarbeitung wie bei Kafka
    public void enqueue(String topic, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            log.debug("Publishing async event to topic '{}': {}", topic, json);

            // Direktes Dispatching an registrierte Handler (in-memory)
            eventBus.publish(topic, json);

            // ############# Outbox hier nicht nötig, da in gleicher VM
            //marker.markAsSent(eventId, Instant.now());

        } catch (Exception e) {
            log.error("Failed to publish async event to topic '{}'", topic, e);
            // ############# Outbox hier nicht nötig, da in gleicher VM
            //marker.incrementAttempt(eventId, e.getMessage());
            throw new RuntimeException("Async event publishing failed", e);
        }
    }
}
