package de.demo.lending.common.domain.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import lombok.Getter;

@Getter
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
}
