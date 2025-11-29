package de.demo.lending.inventory.domain;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class PendingReservation extends AggregateRoot {
    private final BookTitle bookTitle;
    private final LoanId loanId;
    private final UserId userId;
    private final long dueDate; // in days

    // Private Constructor
    private PendingReservation(
            PendingReservationId id,
            BookTitle bookTitle,
            LoanId loanId,
            UserId userId,
            long dueDate
    ) {
        super(id.value(), "");
        this.bookTitle = Objects.requireNonNull(bookTitle);
        this.loanId = Objects.requireNonNull(loanId);
        this.userId = Objects.requireNonNull(userId);
        if (dueDate <= 0) {
            throw new IllegalArgumentException("dueDate must be positive");
        }
        this.dueDate = dueDate;
    }

    // Factory Method
    public static PendingReservation create(
            BookTitle bookTitle,
            LoanId loanId,
            UserId userId,
            long dueDate
    ) {
        return new PendingReservation(
                PendingReservationId.newId(),
                bookTitle,
                loanId,
                userId,
                dueDate
        );
    }

    // Reconstitution (für Repository)
    public static PendingReservation reconstitute(
            PendingReservationId id,
            BookTitle bookTitle,
            LoanId loanId,
            UserId userId,
            long dueDate
    ) {
        return new PendingReservation(id, bookTitle, loanId, userId, dueDate);
    }

    // Business Logic
    public boolean isExpired() {
        // dueDate interpreted as number of days after creation
        return super.getCreatedAt().plus(dueDate, ChronoUnit.DAYS)
                .isBefore(Instant.now());
    }

    // Getters
    public PendingReservationId getPendingReservationId() {
        return PendingReservationId.of(super.getId());
    }

    public BookTitle getBookTitle() {
        return bookTitle;
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

    public Instant getCreatedAt() {
        return super.getCreatedAt();
    }
}
