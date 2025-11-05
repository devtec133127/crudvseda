package de.demo.lending.common.adapters.out.outbox.messaging.async;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Zentraler In-Memory Event Bus für asynchrone Event-Verteilung.
 * <p>
 * Ersetzt Kafka für lokale Tests:
 * - Keine Serialisierung/Deserialisierung über Netzwerk
 * - Kein Offset-Management
 * - Direkte Handler-Aufrufe im Thread-Pool
 * <p>
 * Thread-safe durch ConcurrentHashMap + CopyOnWriteArrayList.
 */
@Component
@Profile("async")
public class AsyncEventBus {

    private static final Logger log = LoggerFactory.getLogger(AsyncEventBus.class);

    // Topic -> List of Handlers
    private final Map<String, List<EventHandler>> handlers = new ConcurrentHashMap<>();

    /**
     * Registriert einen Handler für einen Topic.
     */
    public void subscribe(String topic, EventHandler handler) {
        handlers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                .add(handler);
        log.info("Subscribed handler to topic '{}'", topic);
    }

    /**
     * Publiziert ein Event an alle registrierten Handler des Topics.
     * Asynchrone Ausführung pro Handler (simuliert Kafka-Consumer-Threads).
     */
    @Async
    public void publish(String topic, String eventJson) {
        List<EventHandler> topicHandlers = handlers.get(topic);

        if (topicHandlers == null || topicHandlers.isEmpty()) {
            log.warn("No handlers registered for topic '{}'", topic);
            return;
        }

        log.debug("Dispatching event to {} handler(s) for topic '{}'", topicHandlers.size(), topic);

        for (EventHandler handler : topicHandlers) {
            try {
                handler.handle(eventJson);
            } catch (Exception e) {
                log.error("Handler failed for topic '{}': {}", topic, e.getMessage(), e);
                // Fehler-Handling: Bei Kafka würde der Consumer retry/DLQ nutzen
                // Hier: Log + continue (kann erweitert werden)
            }
        }
    }

    @FunctionalInterface
    public interface EventHandler {
        void handle(String eventJson) throws Exception;
    }
}
