package de.demo.lending.inventory.domain;

import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.event.ReservationCreated;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.loan.domain.event.LoanRequested;

import java.time.Duration;
import java.time.Instant;

public class Reservation {
    public enum ReservationStatus { PENDING, CONFIRMED, CANCELLED, FAILED, EXPIRED }

    private final ReservationId id;
    private final String correlationId; // Aggregate Id
    private final String isbn;
    private final String bookTitle;
    private final UserId userId;
    private ReservationStatus status;
    private Instant createdAt;
    private Instant expiresAt;
    private String copyId; // optional
    private String bookId;


    private final java.util.List<Object> domainEvents = new java.util.ArrayList<>();

    private Reservation(ReservationId id, String correlationId, String isbn, String bookTitle, UserId userId) {
        this.id = id;
        this.correlationId = correlationId;
        this.isbn = isbn;
        this.bookTitle = bookTitle;
        this.userId = userId;
    }

    public static Reservation create(String correlationId, LoanId loanId, String isbn, String bookTitle, UserId userId, Duration ttl) {
        Reservation reservation = new Reservation(ReservationId.newId(), correlationId, isbn, bookTitle, userId);
        reservation.status = ReservationStatus.PENDING;
        reservation.createdAt = Instant.now();
        reservation.expiresAt = reservation.createdAt.plus(ttl);

        reservation.raise(new ReservationCreated(loanId, userId, bookTitle, Instant.now()));
        return reservation;
    }

    private void raise(Object event) {
        domainEvents.add(event);
    }

    public java.util.List<Object> pullDomainEvents() {
        var copy = java.util.List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public void confirm(String copyId) {
        if (this.status != ReservationStatus.PENDING) throw new IllegalStateException("Reservation not PENDING");
        this.copyId = copyId;
        this.status = ReservationStatus.CONFIRMED;
        // emit ReservationConfirmed
    }

    //public void cancel(String reason) { ... } // set FAILED/CANCELLED + event
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }

    public ReservationId getId() {
        return id;
    }
}
