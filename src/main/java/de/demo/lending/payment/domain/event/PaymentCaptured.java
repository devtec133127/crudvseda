package de.demo.lending.payment.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;

public class PaymentCaptured extends BaseDomainEvent {

    private String id;
    private LoanId loanId;
    private BookId bookId;
    private UserId userId;
    private Instant occurredAt;

    public PaymentCaptured(String eventId, LoanId loanId, UserId userId, BookId bookId, String correlationId, Instant occurredAt) {
        super(eventId, correlationId, correlationId, occurredAt, userId);
        this.loanId = loanId;
        this.bookId = bookId;
    }

    public LoanId getLoanId() {
        return loanId;
    }

    public BookId getBookId() {
        return bookId;
    }
}
