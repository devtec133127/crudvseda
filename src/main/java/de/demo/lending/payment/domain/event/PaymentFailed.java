package de.demo.lending.payment.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;

public class PaymentFailed extends BaseDomainEvent {

    private String id;
    private LoanId loanId;
    private UserId userId;
    private Instant occurredAt;
    private String reason;
    private BookId bookId;

    public PaymentFailed(String eventId, LoanId loanId, BookId bookId, UserId userId, String correlationId, Instant occurredAt, String reason) {
        super(eventId, correlationId, correlationId, occurredAt, userId);
        this.loanId = loanId;
        this.bookId = bookId;
        this.reason = reason;
    }

    public LoanId getLoanId() {
        return loanId;
    }

    public BookId getBookId() {
        return bookId;
    }

    public String getReason() {
        return reason;
    }
}
