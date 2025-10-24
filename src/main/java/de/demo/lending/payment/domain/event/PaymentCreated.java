package de.demo.lending.payment.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;

public class PaymentCreated extends BaseDomainEvent {

    private String id;
    private LoanId loanId;
    private BookId bookId;

    private UserId userId;
    private Instant occurredAt;

    public PaymentCreated(String eventId, LoanId loanId, BookId bookId, UserId userId, String correlationId, Instant occurredAt) {
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
