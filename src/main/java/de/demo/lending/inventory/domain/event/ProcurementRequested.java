package de.demo.lending.inventory.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ProcurementRequested extends BaseDomainEvent {

    private final String bookTitle;
    private final UserId userId;
    private final boolean available;

    public ProcurementRequested(LoanId loanId, String correlationId, String causationId,
                                boolean available, String bookTitle, UserId userId) {
        super(UUID.randomUUID(), loanId, correlationId, causationId, Instant.now(), userId);
        this.available = available;
        this.bookTitle = Objects.requireNonNull(bookTitle);
        this.userId = userId;
    }

    // getters
    public boolean isAvailable() {
        return available;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public UserId getUserId() {
        return userId;
    }
}
