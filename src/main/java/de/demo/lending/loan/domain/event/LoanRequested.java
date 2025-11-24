package de.demo.lending.loan.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class LoanRequested extends BaseDomainEvent {
    private final String bookTitle;
    private final Duration duration;

    public LoanRequested(UUID eventId, LoanId loanId, String correlationId, String causationId, Instant occurredAt,
                         UserId userId, String bookTitle, Duration duration) {
        super(eventId, loanId, correlationId, causationId, occurredAt, userId);
        this.bookTitle = bookTitle;
        this.duration = duration;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public Duration getDuration() {
        return duration;
    }
}