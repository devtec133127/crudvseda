package de.demo.lending.loan.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.event.LoanRequested;

public class Loan {
    private final LoanId id;
    private final UserId userId;
    private final String bookTitle;

    private CopyId copyId;
    private Status status;
    private LocalDate dueDate;
    private final Instant createdAt;
    private Instant updatedAt;

    public enum Status {REQUESTED, RESERVED, CHECKED_OUT, RETURNED, FAILED}

    // Domain-Events nur intern sammeln (keine Framework-Abh.)
    private final java.util.List<Object> domainEvents = new java.util.ArrayList<>();

    private Loan(LoanId id, UserId userId, String bookTitle,
                 CopyId copyId, Status status,
                 LocalDate dueDate, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.bookTitle = bookTitle;
        this.copyId = copyId;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Loan createNew(UserId userId, String bookTitle) {
        var now = Instant.now();
        Loan newLoan = new Loan(LoanId.newId(), userId, bookTitle, null, Status.REQUESTED, null, now, now);
        newLoan.status = Status.REQUESTED;
        newLoan.raise(new LoanRequested(newLoan.getId(), userId, bookTitle, Instant.now(), LoanPolicy.STANDARD_DURATION));
        return newLoan;
    }

    public static Loan restore(LoanId id, UserId userId, String bookTitle, CopyId copyId,
                               Status status, LocalDate dueDate, Instant createdAt, Instant updatedAt) {
        return new Loan(id, userId, bookTitle, copyId, status, dueDate, createdAt, updatedAt);
    }

    private void raise(Object event) {
        domainEvents.add(event);
    }

    public java.util.List<Object> pullDomainEvents() {
        var copy = java.util.List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public void markReserved(CopyId copyId) {
        if (status != Status.REQUESTED) throw new IllegalStateException("Not in REQUESTED");
        this.copyId = copyId;
        this.status = Status.RESERVED;
        this.updatedAt = Instant.now();
    }

    public void checkOut(LocalDate dueDate) {
        if (status != Status.RESERVED) throw new IllegalStateException("Not in RESERVED");
        this.status = Status.CHECKED_OUT;
        this.dueDate = dueDate;
        this.updatedAt = Instant.now();
    }

    public void markReturned() {
        if (status != Status.CHECKED_OUT) throw new IllegalStateException("Not in CHECKED_OUT");
        this.status = Status.RETURNED;
        this.updatedAt = Instant.now();
    }

    public void fail() {
        this.status = Status.FAILED;
        this.updatedAt = Instant.now();
    }

    // Getter
    public LoanId getId() {
        return id;
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
                "id=" + id +
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