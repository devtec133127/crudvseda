package de.demo.lending.loan.domain;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.event.LoanActivated;
import de.demo.lending.loan.domain.event.LoanRequested;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Loan extends AggregateRoot {
    private static final int RANGE_IN_DAYS = 14;

    private final UserId userId;
    private final String isbn;

    private CopyId copyId;
    private Status status;
    private LocalDate dueDate;
    private final Instant createdAt;
    private Instant updatedAt;

    public enum Status {REQUESTED, READY_FOR_PICKUP, ACTIVE, EXTENDED, OVERDUE, CLOSED}

    private Loan(LoanId id, UserId userId, String isbn,
                 CopyId copyId, Status status,
                 LocalDate dueDate, Instant createdAt, Instant updatedAt) {
        super(id.value(), "");
        this.userId = userId;
        this.isbn = isbn;
        this.copyId = copyId;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Loan createNew(UserId userId, String isbn, String correlationId, String causationId) {
        var now = Instant.now();
        Loan newLoan = new Loan(LoanId.newId(), userId, isbn, null, Status.REQUESTED, null, now, now);
        newLoan.status = Status.REQUESTED;

        newLoan.raise(new LoanRequested(UUID.randomUUID(), newLoan.getLoanId(), correlationId, causationId, Instant.now(),
                userId, isbn, LoanPolicy.STANDARD_DURATION));
        return newLoan;
    }

    public void activate(CopyId copyId) {
        if (status != Status.REQUESTED && this.status != Status.READY_FOR_PICKUP) {
            throw new IllegalStateException("Not in REQUESTED or READY_FOR_PICKUP");
        }

        if (copyId == null) {
            throw new IllegalArgumentException("Keine Item-ID vorhanden - Buch nicht angekommen?");
        }

        this.copyId = copyId;
        this.status = Status.ACTIVE;
        this.updatedAt = Instant.now();

        raise(new LoanActivated(getLoanId(), this.copyId, this.dueDate, this.userId));
    }

    /*
     * Listen to procurement.book_received.v1 → markAsReadyForPickup
     */
    public void markAsReadyForPickup(CopyId copyId) {
        if (this.status != Status.REQUESTED && this.status != Status.ACTIVE) {
            throw new IllegalStateException(
                    "Kann nur aus REQUESTED oder ACTIVE zu READY_FOR_PICKUP wechseln"
            );
        }

        this.status = Status.READY_FOR_PICKUP;
        this.copyId = copyId;  // Jetzt kennen wir das konkrete Item
        this.updatedAt = Instant.now();

        // Optional: Event für Benachrichtigung an User
        //raise(new LoanReadyForPickup(this.loanId, this.userId));
    }

    /**
     * Policy: Extension nur 1x + nur wenn ACTIVE
     */
    public void extend() {
        // Policy: Nur im ACTIVE Status
        if (this.status != Status.ACTIVE) {
            throw new IllegalStateException(
                    "Verlängerung nur für aktive Ausleihen möglich"
            );
        }

        // Policy: Nur 1x verlängerbar
        if (this.status == Status.EXTENDED) {
            throw new IllegalStateException(
                    "Diese Ausleihe wurde bereits einmal verlängert"
            );
        }

        // Policy OK → Verlängern
        this.dueDate = this.dueDate.plusDays(14);  // Policy 4: +14 Tage
        this.updatedAt = Instant.now();

        //raise(new LoanExtended(this.loanId, this.dueDate));
    }

    public static Loan restore(LoanId id, UserId userId, String bookTitle,
                               Status status, LocalDate dueDate, Instant createdAt, Instant updatedAt) {
        return new Loan(id, userId, bookTitle, null, status, dueDate, createdAt, updatedAt);
    }

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

    public String getIsbn() {
        return isbn;
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
                ", isbn=" + isbn +
                ", copyId=" + copyId +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                "}";
    }
}