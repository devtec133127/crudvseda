package de.demo.lending.inventory.domain;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class PendingReservation extends AggregateRoot {
    private final BookId bookId;
    private final LoanId loanId;
    private final UserId userId;
    private final long dueDate;

    // Private Constructor
    private PendingReservation(
            PendingReservationId id,
            BookId bookId,
            LoanId loanId,
            UserId userId,
            long dueDate
    ) {
        super(id.value(), "");
        this.bookId = Objects.requireNonNull(bookId);
        this.loanId = Objects.requireNonNull(loanId);
        this.userId = Objects.requireNonNull(userId);
        if (dueDate <= 0) {
            throw new IllegalArgumentException("dueDate must be positive");
        }
        this.dueDate = dueDate;
    }

    // ⭐ Factory Method
    public static PendingReservation create(
            BookId bookId,
            LoanId loanId,
            UserId userId,
            long dueDate
    ) {
        return new PendingReservation(
                PendingReservationId.newId(),
                bookId,
                loanId,
                userId,
                dueDate
        );
    }

    // ⭐ Reconstitution (für Repository)
    public static PendingReservation reconstitute(
            PendingReservationId id,
            BookId bookId,
            LoanId loanId,
            UserId userId,
            long dueDate
    ) {
        return new PendingReservation(
                id, bookId, loanId, userId, dueDate
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

    public BookId getBookId() {
        return bookId;
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