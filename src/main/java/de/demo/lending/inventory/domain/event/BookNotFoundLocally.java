package de.demo.lending.inventory.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class BookNotFoundLocally extends BaseDomainEvent {
    private final BookId bookId;

    public BookNotFoundLocally(LoanId loanId, String correlationId, String causationId,
                               BookId bookId, UserId userId) {
        super(UUID.randomUUID(), loanId, correlationId, causationId, Instant.now(), userId);
        this.bookId = Objects.requireNonNull(bookId);
    }

    public BookId getBookId() {
        return bookId;
    }
}