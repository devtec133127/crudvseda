package de.demo.lending.payment.domain.event;

import java.time.Instant;
import java.util.UUID;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.payment.domain.PaymentId;

public class PaymentInitiated extends BaseDomainEvent {

    private final PaymentId paymentId;
    
    public PaymentInitiated(PaymentId paymentId, LoanId loanId, UserId userId, String correlationId, Instant occurredAt) {
        super(UUID.randomUUID(), loanId, correlationId, correlationId, occurredAt, userId);
        this.paymentId = paymentId;
    }
}
