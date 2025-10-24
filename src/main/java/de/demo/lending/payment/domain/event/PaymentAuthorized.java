package de.demo.lending.payment.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;

public class PaymentAuthorized extends BaseDomainEvent {

    private String id;
    private LoanId loanId;
    private UserId userId;
    private Instant occurredAt;

    public PaymentAuthorized(String eventId, LoanId loanId, UserId userId, String correlationId, Instant occurredAt) {
        super(eventId, correlationId, correlationId, occurredAt, userId);
        this.loanId = loanId;
    }
}
