package de.demo.lending.common.adapters.out.outbox.messaging;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
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
                    .type("loan.requested.v1")
                    .payload(json)                 // ObjectMapper.writeValueAsString(...)
                    .headers(null)                 // oder JSON mit correlationId etc.
                    .createdAt(Instant.now())
                    .attempt(0)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("serialize event failed", e);
        }
    }
}
