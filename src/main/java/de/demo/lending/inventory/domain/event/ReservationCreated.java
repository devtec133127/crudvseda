package de.demo.lending.inventory.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.ReservationId;
import de.demo.lending.loan.domain.LoanId;

public final class ReservationCreated extends BaseDomainEvent {

    private final ReservationId reservationId;
    private final String bookTitle;
    private final UserId userId;
    private final Instant expiresAt;

    public ReservationCreated(LoanId loanId, String correlationId, String causationId,
                              ReservationId reservationId, String bookTitle, UserId userId, Instant expiresAt) {
        super(UUID.randomUUID().toString(), loanId, correlationId, causationId, Instant.now(), userId);
        this.reservationId = reservationId;
        this.bookTitle = Objects.requireNonNull(bookTitle);
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    // getters
    public ReservationId getReservationId() {
        return reservationId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public UserId getUserId() {
        return userId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
