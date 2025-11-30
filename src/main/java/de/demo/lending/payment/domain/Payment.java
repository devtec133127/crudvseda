package de.demo.lending.payment.domain;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.payment.domain.event.PaymentInitiated;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


public class Payment extends AggregateRoot {
    private final LoanId loanId;       // referenz zur Domäne (z.B. loanId / reservationId)
    private final UserId userId;
    private Money amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String correlationId; // wichtig für wiederholte Requests
    private Instant updatedAt;


    private Payment(UUID id, LoanId loanId, UserId userId, Money amount, PaymentMethod method,
                    String correlationId) {
        super(id, correlationId);
        this.loanId = Objects.requireNonNull(loanId);
        this.userId = Objects.requireNonNull(userId);
        this.amount = Objects.requireNonNull(amount);
        this.method = Objects.requireNonNull(method);
        this.status = PaymentStatus.INITIATED;
        this.correlationId = correlationId;
    }

    public static Payment create(UUID id, LoanId loanId, UserId userId, Money amount, PaymentMethod method) {
        return new Payment(id, loanId, userId, amount, method, "");
    }

    /*public static Payment createNew(UUID id, LoanId loanId, UserId userId, Money amount, PaymentMethod method, String correlationId) {
        // invariants
        if (amount.getCents() <= 0) throw new IllegalArgumentException("Amount must be positive");

        Payment payment = new Payment(id, loanId, userId, amount, method, correlationId);
        payment.status = PaymentStatus.INITIATED;
        payment.raise(new PaymentCreated(UUID.randomUUID(), loanId, userId, correlationId, Instant.now()));
        return payment;
    }*/

    public static Payment initiate(LoanId loanId, UserId userId) {
        Payment payment = new Payment(
                PaymentId.newId().value(),
                loanId,
                userId,
                Money.zero(),
                PaymentMethod.BANK_TRANSFER,
                ""
        );

        payment.status = PaymentStatus.INITIATED;
        payment.amount = Money.zero();

        payment.raise(new PaymentInitiated(payment.getPaymentId(), payment.loanId, payment.userId, "", Instant.now()));

        return payment;
    }

    /*public void capture(Money captureAmount) {
        if (status != PaymentStatus.CREATED) throw new IllegalStateException("Can only capture from AUTHORIZED");
        if (!captureAmount.equals(this.amount)) {
            // optional: allow partial capture -> adjust invariants / record amountCaptured
            throw new IllegalArgumentException("Currently only full capture supported");
        }
        this.status = PaymentStatus.CAPTURED;
        this.updatedAt = Instant.now();
        this.raise(new PaymentCaptured(UUID.randomUUID(), loanId, userId, bookId, getCorrelationId(), Instant.now()));
    }

    public void fail(String reason) {
        if (status == PaymentStatus.CAPTURED || status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Cannot fail a completed payment");
        }
        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();

        this.raise(new PaymentFailed(UUID.randomUUID(), loanId, bookId, userId, correlationId, Instant.now(), reason));
    }*/

    /*public void refund(Money amountToRefund) {
        if (status != PaymentStatus.CAPTURED) throw new IllegalStateException("Only captured payments can be refunded");
        if (amountToRefund.cents() > this.amount.cents()) throw new IllegalArgumentException("Refund > captured");
        // for simplicity: full refund
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = OffsetDateTime.now();
        producedEvents.add(new PaymentRefundedEvent(id, amountToRefund));
    }*/

    public PaymentId getPaymentId() {
        return PaymentId.of(getId());
    }

    public LoanId getLoanId() {
        return loanId;
    }

    public UserId getUserId() {
        return userId;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}