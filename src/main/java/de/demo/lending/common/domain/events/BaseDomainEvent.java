package de.demo.lending.common.domain.events;

import de.demo.lending.common.valueobjects.UserId;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public abstract class BaseDomainEvent {
    private final String eventId;
    private final String correlationId; // meist requestId
    private final String causationId;   // id des direkten auslösers (eventId/commandId)
    private final Instant occurredAt;
    private final UserId userId;

    // falls du beim Rehydration/Deserializing eine signatur brauchst:
    protected BaseDomainEvent(String eventId, String correlationId, String causationId, Instant occurredAt, UserId userId) {
        this.eventId = Objects.requireNonNull(eventId);
        this.correlationId = Objects.requireNonNull(correlationId);
        this.causationId = Objects.requireNonNull(causationId);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.userId = Objects.requireNonNull(userId);
    }
}
