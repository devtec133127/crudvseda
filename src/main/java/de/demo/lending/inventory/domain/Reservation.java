package de.demo.lending.inventory.domain;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.event.ReservationCreated;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.loan.domain.event.LoanRequested;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class Reservation extends AggregateRoot {
    public enum ReservationStatus { PENDING, CONFIRMED, CANCELLED, FAILED, EXPIRED }

    private final String bookTitle;
    private final BookId bookId;
    private final UserId userId;
    private ReservationStatus status;
    private Instant createdAt;
    private Instant expiresAt;
    private String copyId; // optional


    private Reservation(String id, String correlationId, BookId bookId, String bookTitle, UserId userId) {
        super(id, correlationId);
        this.bookTitle = bookTitle;
        this.bookId = bookId;
        this.userId = userId;
    }

    public static Reservation create(String correlationId, String causationId, BookId  bookId, String bookTitle, UserId userId, Duration ttl) {
        Reservation reservation = new Reservation(ReservationId.newId().value().toString(), correlationId, bookId, bookTitle, userId);
        reservation.status = ReservationStatus.PENDING;
        reservation.createdAt = Instant.now();
        reservation.expiresAt = reservation.createdAt.plus(ttl);

        reservation.raise(new ReservationCreated(correlationId, causationId, reservation.getReservationId(), bookTitle, userId, Instant.now()));
        return reservation;
    }

    public void confirm(String copyId) {
        if (this.status != ReservationStatus.PENDING) throw new IllegalStateException("Reservation not PENDING");
        this.copyId = copyId;
        this.status = ReservationStatus.CONFIRMED;
        // emit ReservationConfirmed
    }

    //public void cancel(String reason) { ... } // set FAILED/CANCELLED + event
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }

    public ReservationId getReservationId() {
        UUID uuid = UUID.fromString(super.getId());
        return ReservationId.of(uuid);
    }
}
