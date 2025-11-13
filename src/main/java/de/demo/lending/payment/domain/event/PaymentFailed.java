package de.demo.lending.payment.domain.event;

import java.time.Instant;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

public class PaymentFailed extends BaseDomainEvent {

    private UserId userId;
    private Instant occurredAt;
    private String reason;
    private BookId bookId;

    public PaymentFailed(String eventId, LoanId loanId, BookId bookId, UserId userId, String correlationId, Instant occurredAt, String reason) {
        super(eventId, loanId, correlationId, correlationId, occurredAt, userId);
        this.bookId = bookId;
        this.reason = reason;
    }

    public BookId getBookId() {
        return bookId;
    }

    public String getReason() {
        return reason;
    }
}
