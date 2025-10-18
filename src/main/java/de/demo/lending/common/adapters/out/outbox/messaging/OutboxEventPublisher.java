package de.demo.lending.common.adapters.out.outbox.messaging;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Outbox-basierter Event Publisher für Kafka.
 * 
 * Aktiviert durch Profile "kafka" (Standard für Produktion).
 * Wird durch AsyncEventPublisher ersetzt bei Profile "async".
 */
@Component
@Profile("kafka")  // NEU: Nur aktiv bei Kafka-Profil
@ConditionalOnProperty(value="service.role", havingValue="loan")
public class OutboxEventPublisher implements EventPublisher {
    private final OutboxRepository repo;
    private final ObjectMapper om = new ObjectMapper();
    public OutboxEventPublisher(OutboxRepository repo){ this.repo = repo; }

    @Override
    public void enqueue(String type, Object payload) {
        try {
            String json = om.writeValueAsString(payload);
            repo.save(OutboxEntity.builder()
                    .type(type)  // FIX: type dynamisch nutzen statt hardcoded "loan.requested.v1"
                    .payload(json)
                    .headers(null)
                    .createdAt(Instant.now())
                    .attempt(0)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("serialize event failed", e);
        }
    }
}
