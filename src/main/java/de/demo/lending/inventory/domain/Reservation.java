package de.demo.lending.inventory.domain;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.event.ProcurementRequested;
import de.demo.lending.loan.domain.LoanId;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class Reservation extends AggregateRoot {
    public enum ReservationStatus {PENDING, CONFIRMED, CANCELLED, FAILED, EXPIRED}

    private final LoanId loanId;
    private final String bookTitle;
    private final BookId bookId;
    private final UserId userId;
    private ReservationStatus status;
    private Instant createdAt;
    private Instant expiresAt;
    private String copyId; // optional


    private Reservation(String id, LoanId loanId, String correlationId, BookId bookId, String bookTitle, UserId userId) {
        super(id, correlationId);
        this.loanId = loanId;
        this.bookTitle = bookTitle;
        this.bookId = bookId;
        this.userId = userId;
    }

    private Reservation(String id, LoanId loanId, String correlationId, String bookTitle, BookId bookId, UserId userId,
                        ReservationStatus status, Instant createdAt, Instant expiresAt, String copyId) {
        super(id, correlationId);
        this.bookTitle = bookTitle;
        this.loanId = loanId;
        this.bookId = bookId;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.copyId = copyId;
    }

    public static Reservation create(LoanId loanId, String correlationId, String causationId, BookId bookId, String bookTitle, UserId userId, Duration ttl) {
        Reservation reservation = new Reservation(ReservationId.newId().value().toString(), loanId, correlationId, bookId, bookTitle, userId);
        reservation.status = ReservationStatus.PENDING;
        reservation.createdAt = Instant.now();
        reservation.expiresAt = reservation.createdAt.plus(ttl);

        reservation.raise(new ProcurementRequested(loanId, correlationId, causationId, true, bookTitle, userId));
        return reservation;
    }

    public static Reservation create(String id, LoanId loanId, String correlationId, String bookTitle, BookId bookId, UserId userId,
                                     ReservationStatus status, Instant createdAt, Instant expiresAt, String copyId) {
        return new Reservation(ReservationId.newId().value().toString(), loanId, correlationId, bookTitle, bookId, userId, status, createdAt, expiresAt, copyId);
    }

    public void confirm(String copyId) {
        if (this.status != ReservationStatus.PENDING) throw new IllegalStateException("Reservation not PENDING");
        this.copyId = copyId;
        this.status = ReservationStatus.CONFIRMED;
        // emit ReservationConfirmed
    }

    //public void cancel(String reason) { ... } // set FAILED/CANCELLED + event
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public ReservationId getReservationId() {
        UUID uuid = UUID.fromString(super.getId());
        return ReservationId.of(uuid);
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public BookId getBookId() {
        return bookId;
    }

    public UserId getUserId() {
        return userId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getCopyId() {
        return copyId;
    }

    public LoanId getLoanId() {
        return loanId;
    }
}
