package de.demo.lending.loan.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.event.LoanActivated;
import de.demo.lending.loan.domain.event.LoanRequested;

public class Loan extends AggregateRoot {
    private final UserId userId;
    private final Isbn isbn;

    private CopyId copyId;
    private Status status;
    private LocalDate dueDate;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Status-Flow:
     * Use Case 1 (Buch vorhanden): REQUESTED → ACTIVE → CLOSED
     * Use Case 2 (Procurement):    REQUESTED → READY_FOR_PICKUP → ACTIVE → CLOSED
     */
    public enum Status {REQUESTED, READY_FOR_PICKUP, ACTIVE, CLOSED}

    private Loan(LoanId id, UserId userId, Isbn isbn,
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

    /**
     * Factory Method: User fragt Buch an
     */
    public static Loan request(UserId userId, Isbn isbn, String correlationId, String causationId) {
        var now = Instant.now();
        Loan newLoan = new Loan(LoanId.newId(),
                userId,
                isbn,
                null, // CopyId noch unbekannt
                Status.REQUESTED,
                null, // DueDate wird bei Checkout gesetzt
                now,
                now);
        newLoan.status = Status.REQUESTED;

        newLoan.raise(new LoanRequested(UUID.randomUUID(), newLoan.getLoanId(), correlationId, causationId, Instant.now(),
                userId, isbn.value(), Duration.ofDays(LoanPolicy.STANDARD_DURATION_DAYS)));
        return newLoan;
    }

    /**
     * Use Case 1: Buch ist lokal vorhanden und kann direkt ausgeliehen werden
     * Triggered by: inventory.reserved.v1 event
     */
    public void activate(CopyId copyId) {
        if (copyId == null) {
            throw new IllegalArgumentException("CopyId darf nicht null sein");
        }

        if (status != Status.REQUESTED && this.status != Status.READY_FOR_PICKUP) {
            throw new IllegalStateException("Not in REQUESTED or READY_FOR_PICKUP");
        }

        this.copyId = copyId;
        this.status = Status.ACTIVE;
        this.dueDate = LocalDate.now().plusDays(LoanPolicy.STANDARD_DURATION_DAYS);
        this.updatedAt = Instant.now();

        raise(new LoanActivated(getLoanId(), this.copyId, this.dueDate, this.userId));
    }

    /**
     * Business Method: Buch ist angekommen → bereit zur Abholung
     * Triggered by: procurement.book_received.v1 event
     */
    public void markAsReadyForPickup(CopyId copyId) {
        if (copyId == null) {
            throw new IllegalArgumentException("CopyId darf nicht null sein");
        }

        if (this.status != Status.REQUESTED) {
            throw new IllegalStateException(
                    "Kann nur aus REQUESTED zu READY_FOR_PICKUP wechseln"
            );
        }

        this.status = Status.READY_FOR_PICKUP;
        this.copyId = copyId;  // Jetzt kennen wir das konkrete Item
        this.updatedAt = Instant.now();

        // Optional: Event für Benachrichtigung an User
        //raise(new LoanReadyForPickup(this.loanId, this.userId));
    }

    /**
     * Business Method: User holt Buch ab → Ausleihe wird aktiv
     */
    public void checkOut() {
        if (this.status != Status.READY_FOR_PICKUP) {
            throw new IllegalStateException(
                    "Checkout nur möglich wenn Buch zur Abholung bereit ist. Aktueller Status: " + this.status
            );
        }

        if (this.copyId == null) {
            throw new IllegalStateException("CopyId muss gesetzt sein vor Checkout");
        }

        this.status = Status.ACTIVE;
        this.dueDate = LocalDate.now().plusDays(LoanPolicy.STANDARD_DURATION_DAYS);
        this.updatedAt = Instant.now();

        raise(new LoanActivated(
                getLoanId(),
                this.copyId,
                this.dueDate,
                this.userId
        ));
    }

    public static Loan restore(LoanId id, UserId userId, Isbn isbn,
                               Status status, LocalDate dueDate, Instant createdAt, Instant updatedAt) {
        return new Loan(id, userId, isbn, null, status, dueDate, createdAt, updatedAt);
    }

    // Getter
    public LoanId getLoanId() {
        return LoanId.of(super.getId());
    }

    public UserId getUserId() {
        return userId;
    }

    public Isbn getIsbn() {
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