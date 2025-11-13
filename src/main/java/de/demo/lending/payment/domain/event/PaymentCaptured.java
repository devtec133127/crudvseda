package de.demo.lending.payment.domain.event;

import java.time.Instant;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

public class PaymentCaptured extends BaseDomainEvent {

    private BookId bookId;
    private UserId userId;
    private Instant occurredAt;

    public PaymentCaptured(String eventId, LoanId loanId, UserId userId, BookId bookId, String correlationId, Instant occurredAt) {
        super(eventId, loanId, correlationId, correlationId, occurredAt, userId);
        this.bookId = bookId;
    }

    public BookId getBookId() {
        return bookId;
    }
}
