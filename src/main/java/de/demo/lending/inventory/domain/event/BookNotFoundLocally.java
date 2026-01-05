package de.demo.lending.inventory.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.common.valueobjects.LoanId;

public class BookNotFoundLocally extends BaseDomainEvent {
    private final Isbn isbn;

    public BookNotFoundLocally(LoanId loanId, String correlationId, String causationId,
                               Isbn isbn, UserId userId) {
        super(UUID.randomUUID(), loanId, correlationId, causationId, Instant.now(), userId);
        this.isbn = Objects.requireNonNull(isbn);
    }

    public Isbn getIsbn() {
        return isbn;
    }
}