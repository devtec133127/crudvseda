package de.demo.lending.inventory.domain;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class PendingReservation extends AggregateRoot {
    private final Isbn isbn;
    private final LoanId loanId;
    private final UserId userId;
    private final long dueDate;

    // Private Constructor
    private PendingReservation(
            PendingReservationId id,
            Isbn isbn,
            LoanId loanId,
            UserId userId,
            long dueDate
    ) {
        super(id.value(), "");
        this.isbn = Objects.requireNonNull(isbn);
        this.loanId = Objects.requireNonNull(loanId);
        this.userId = Objects.requireNonNull(userId);
        if (dueDate <= 0) {
            throw new IllegalArgumentException("dueDate must be positive");
        }
        this.dueDate = dueDate;
    }

    // ⭐ Factory Method
    public static PendingReservation create(
            Isbn isbn,
            LoanId loanId,
            UserId userId,
            long dueDate
    ) {
        return new PendingReservation(
                PendingReservationId.newId(),
                isbn,
                loanId,
                userId,
                dueDate
        );
    }

    // ⭐ Reconstitution (für Repository)
    public static PendingReservation reconstitute(
            PendingReservationId id,
            Isbn isbn,
            LoanId loanId,
            UserId userId,
            long dueDate
    ) {
        return new PendingReservation(
                id, isbn, loanId, userId, dueDate
        );
    }

    // ⭐ Business Logic
    public boolean isExpired() {
        return super.getCreatedAt().plus(dueDate, ChronoUnit.DAYS)
                .isBefore(Instant.now());
    }

    // Nur Getters
    public PendingReservationId getPendingReservationId() {
        return PendingReservationId.of(super.getId());
    }

    public Isbn getIsbn() {
        return isbn;
    }

    public LoanId getLoanId() {
        return loanId;
    }

    public UserId getUserId() {
        return userId;
    }

    public long getDueDate() {
        return dueDate;
    }
}