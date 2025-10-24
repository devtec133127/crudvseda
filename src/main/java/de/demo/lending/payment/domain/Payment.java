package de.demo.lending.payment.domain;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.payment.domain.event.PaymentAuthorized;
import de.demo.lending.payment.domain.event.PaymentCaptured;
import de.demo.lending.payment.domain.event.PaymentCreated;
import de.demo.lending.payment.domain.event.PaymentFailed;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


public class Payment extends AggregateRoot {
    private final String id;
    private final LoanId loanId;       // referenz zur Domäne (z.B. loanId / reservationId)
    private final UserId userId;
    private final BookId bookId;
    private Money amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String correlationId; // wichtig für wiederholte Requests
    private Instant createdAt;
    private Instant updatedAt;

    // Domain-Events nur intern sammeln (keine Framework-Abh.)
    private final java.util.List<Object> domainEvents = new java.util.ArrayList<>();

    private Payment(String id, LoanId loanId, BookId bookId, UserId userId, Money amount, PaymentMethod method,
                    String correlationId) {
        super(id, correlationId);
        this.id = Objects.requireNonNull(id);
        this.loanId = Objects.requireNonNull(loanId);
        this.bookId = Objects.requireNonNull(bookId);
        this.userId = Objects.requireNonNull(userId);
        this.amount = Objects.requireNonNull(amount);
        this.method = Objects.requireNonNull(method);
        this.status = PaymentStatus.CREATED;
        this.correlationId = correlationId;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public static Payment create(String id, LoanId loanId, BookId bookId, UserId userId, Money amount, PaymentMethod method) {
        return new Payment(id, loanId, bookId, userId, amount, method, null);
    }

    public static Payment createNew(String id, LoanId loanId, BookId bookId, UserId userId, Money amount, PaymentMethod method, String correlationId) {
        // invariants
        if (amount.getCents() <= 0) throw new IllegalArgumentException("Amount must be positive");

        Payment payment = new Payment(id, loanId, bookId, userId, amount, method, correlationId);
        payment.status = PaymentStatus.CREATED;
        payment.raise(new PaymentCreated(id, loanId, bookId, userId, correlationId, Instant.now()));
        return payment;
    }

    // Domain actions
    public void authorize(String providerTxId) {
        if (status != PaymentStatus.CREATED) throw new IllegalStateException("Can only authorize from CREATED");
        //this.providerTransactionId = providerTxId;
        this.status = PaymentStatus.AUTHORIZED;
        this.updatedAt = Instant.now();

        this.raise(new PaymentAuthorized(id, loanId, userId, getCorrelationId(), Instant.now()));
    }

    public void capture(Money captureAmount) {
        if (status != PaymentStatus.AUTHORIZED) throw new IllegalStateException("Can only capture from AUTHORIZED");
        if (!captureAmount.equals(this.amount)) {
            // optional: allow partial capture -> adjust invariants / record amountCaptured
            throw new IllegalArgumentException("Currently only full capture supported");
        }
        this.status = PaymentStatus.CAPTURED;
        this.updatedAt = Instant.now();
        this.raise(new PaymentCaptured(id, loanId, userId, getCorrelationId(), Instant.now()));
    }

    public void fail(String reason) {
        if (status == PaymentStatus.CAPTURED || status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Cannot fail a completed payment");
        }
        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();

        this.raise(new PaymentFailed(id, loanId, bookId, userId, correlationId, Instant.now(), reason));
    }

    /*public void refund(Money amountToRefund) {
        if (status != PaymentStatus.CAPTURED) throw new IllegalStateException("Only captured payments can be refunded");
        if (amountToRefund.cents() > this.amount.cents()) throw new IllegalArgumentException("Refund > captured");
        // for simplicity: full refund
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = OffsetDateTime.now();
        producedEvents.add(new PaymentRefundedEvent(id, amountToRefund));
    }*/

    public PaymentId getPaymentId() {
        UUID uuid = UUID.fromString(super.getId());
        return PaymentId.of(uuid);
    }

    public LoanId getLoanId() {
        return loanId;
    }

    public UserId getUserId() {
        return userId;
    }

    public BookId getBookId() {
        return bookId;
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

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}