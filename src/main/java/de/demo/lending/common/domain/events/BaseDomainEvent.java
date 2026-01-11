package de.demo.lending.common.domain.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.common.valueobjects.UserId;

public abstract class BaseDomainEvent {
    private final UUID eventId;
    private final String correlationId; // meist requestId
    private final String causationId;   // id des direkten auslösers (eventId/commandId)
    private final Instant occurredAt;
    private final LoanId loanId;
    private final UserId userId;

    // falls du beim Rehydration/Deserializing eine signatur brauchst:
    protected BaseDomainEvent(UUID eventId, LoanId loanId, String correlationId, String causationId, Instant occurredAt, UserId userId) {
        this.eventId = Objects.requireNonNull(eventId);
        this.loanId = Objects.requireNonNull(loanId);
        this.correlationId = Objects.requireNonNull(correlationId);
        this.causationId = Objects.requireNonNull(causationId);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.userId = userId;
    }

    protected BaseDomainEvent(UUID eventId, LoanId loanId, String correlationId, String causationId, Instant occurredAt) {
        this(eventId, loanId, correlationId, causationId, occurredAt, null);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseDomainEvent that)) return false;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(eventId);
    }

    @Override
    public String toString() {
        return "BaseDomainEvent{" +
                "eventId=" + eventId +
                ", correlationId='" + correlationId + '\'' +
                ", causationId='" + causationId + '\'' +
                ", occurredAt=" + occurredAt +
                ", loanId=" + loanId +
                ", userId=" + userId +
                '}';
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public LoanId getLoanId() {
        return loanId;
    }

    public UserId getUserId() {
        return userId;
    }
}
