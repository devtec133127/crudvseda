package de.demo.lending.loan.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.event.LoanActivated;
import de.demo.lending.loan.domain.event.LoanRequested;

public class Loan extends AggregateRoot {
    private static final int RANGE_IN_DAYS = 14;

    private final UserId userId;
    private final String bookTitle;

    private CopyId copyId;
    private Status status;
    private LocalDate dueDate;
    private final Instant createdAt;
    private Instant updatedAt;

    public enum Status {REQUESTED, ACTIVE, EXTENDED, OVERDUE, CLOSED}

    private Loan(LoanId id, UserId userId, String bookTitle,
                 CopyId copyId, Status status,
                 LocalDate dueDate, Instant createdAt, Instant updatedAt) {
        super(id.value(), "");
        this.userId = userId;
        this.bookTitle = bookTitle;
        this.copyId = copyId;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Loan createNew(UserId userId, String bookTitle, String correlationId, String causationId) {
        var now = Instant.now();
        Loan newLoan = new Loan(LoanId.newId(), userId, bookTitle, null, Status.REQUESTED, null, now, now);
        newLoan.status = Status.REQUESTED;

        newLoan.raise(new LoanRequested(UUID.randomUUID(), newLoan.getLoanId(), correlationId, causationId, Instant.now(),
                userId, bookTitle, LoanPolicy.STANDARD_DURATION));
        return newLoan;
    }

    public static Loan restore(LoanId id, UserId userId, String bookTitle,
                               Status status, LocalDate dueDate, Instant createdAt, Instant updatedAt) {
        return new Loan(id, userId, bookTitle, null, status, dueDate, createdAt, updatedAt);
    }

    public void activate(CopyId copyId) {
        if (status != Status.REQUESTED) throw new IllegalStateException("Not in REQUESTED");
        this.copyId = copyId;
        this.status = Status.ACTIVE;
        this.updatedAt = Instant.now();

        raise(new LoanActivated(getLoanId(), this.copyId, this.dueDate));
    }

    /*public void fail() {
        this.status = Status.FAILED;
        this.updatedAt = Instant.now();
    }*/

    private Instant calculateDueDate() {
        return Instant.now().plus(RANGE_IN_DAYS, ChronoUnit.DAYS);
    }

    // Getter
    public LoanId getLoanId() {
        return LoanId.of(super.getId());
    }

    public UserId getUserId() {
        return userId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public CopyId getCopyId() {
        return copyId;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + getLoanId() +
                ", userId=" + userId +
                ", bookTitle=" + bookTitle +
                ", copyId=" + copyId +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                "}";
    }
}