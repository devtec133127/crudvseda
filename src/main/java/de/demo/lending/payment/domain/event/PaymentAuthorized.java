package de.demo.lending.payment.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;
import java.util.UUID;

public class PaymentAuthorized extends BaseDomainEvent {


    public PaymentAuthorized(UUID eventId, LoanId loanId, UserId userId, String correlationId, Instant occurredAt) {
        super(eventId, loanId, correlationId, correlationId, occurredAt, userId);
    }
}