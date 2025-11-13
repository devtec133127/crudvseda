package de.demo.lending.payment.domain.event;

import java.time.Instant;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

public class PaymentAuthorized extends BaseDomainEvent {

    private UserId userId;
    private Instant occurredAt;

    public PaymentAuthorized(String eventId, LoanId loanId, UserId userId, String correlationId, Instant occurredAt) {
        super(eventId, loanId, correlationId, correlationId, occurredAt, userId);
    }
}