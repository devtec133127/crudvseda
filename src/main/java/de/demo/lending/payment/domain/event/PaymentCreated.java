package de.demo.lending.payment.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;
import java.util.UUID;

public class PaymentCreated extends BaseDomainEvent {

    private BookId bookId;

    public PaymentCreated(UUID eventId, LoanId loanId, BookId bookId, UserId userId, String correlationId, Instant occurredAt) {
        super(eventId, loanId, correlationId, correlationId, occurredAt, userId);
        this.bookId = bookId;
    }

    public BookId getBookId() {
        return bookId;
    }
}
