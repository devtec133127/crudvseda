package de.demo.lending.common.application.ports.out;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.OutboxEntity;
import de.demo.lending.common.adapters.out.outbox.messaging.OutboxRepository;
import de.demo.lending.common.application.dto.DtoPayload;
import org.springframework.stereotype.Component;

/**
 * Outbox-basierter Event Publisher für Kafka.
 * <p>
 * Aktiviert durch Profile "kafka" (Standard für Produktion).
 * Wird durch AsyncEventPublisher ersetzt bei Profile "async".
 */
@Component
public class OutboxEventPublisher implements EventPublisher {
    private final OutboxRepository repo;
    private final ObjectMapper om = new ObjectMapper();

    public OutboxEventPublisher(OutboxRepository repo) {
        this.repo = repo;
    }

    @Override
    public void enqueue(String type, DtoPayload payload) {
        try {
            String json = om.writeValueAsString(payload);
            repo.save(OutboxEntity.builder()
                    .type(type)  // FIX: type dynamisch nutzen statt hardcoded "loan.requested.v1"
                    .eventId(payload.getEventId())
                    .aggregate_type(payload.getType())
                    .loanId(payload.getLoanId())
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
