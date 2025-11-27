package de.demo.lending.inventory.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class BookNotFoundLocally extends BaseDomainEvent {
    private final String bookTitle;

    public BookNotFoundLocally(LoanId loanId, String correlationId, String causationId,
                               String bookTitle, UserId userId) {
        super(UUID.randomUUID(), loanId, correlationId, causationId, Instant.now(), userId);
        this.bookTitle = Objects.requireNonNull(bookTitle);
    }

    public String getBookTitle() {
        return bookTitle;
    }
}
