package de.demo.lending.inventory.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import de.demo.lending.common.domain.AggregateRoot;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;

public class Reservation extends AggregateRoot {
    public enum ReservationStatus {PENDING, CONFIRMED, CANCELLED, FAILED, EXPIRED}

    private final LoanId loanId;
    private final UserId userId;
    private final CopyId copyId;
    private ReservationStatus status;
    private Instant expiresAt;


    private Reservation(UUID id, LoanId loanId, UserId userId, CopyId copyId) {
        super(id, "");
        this.loanId = loanId;
        this.copyId = copyId;
        this.userId = userId;
    }
    
    public static Reservation create(LoanId loanId, CopyId copyId, UserId userId, Duration ttl) {
        Reservation reservation = new Reservation(ReservationId.newId().value(), loanId, userId, copyId);
        reservation.status = ReservationStatus.PENDING;
        reservation.expiresAt = reservation.getCreatedAt().plus(ttl);

        //reservation.raise(new ProcurementRequested(loanId, correlationId, causationId, true, bookTitle, userId));
        return reservation;
    }

    public void confirm() {
        if (this.status != ReservationStatus.PENDING) throw new IllegalStateException("Reservation not PENDING");
        this.status = ReservationStatus.CONFIRMED;
        // emit ReservationConfirmed
    }

    //public void cancel(String reason) { ... } // set FAILED/CANCELLED + event
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public ReservationId getReservationId() {
        return ReservationId.of(getId());
    }

    public UserId getUserId() {
        return userId;
    }

    public ReservationStatus getStatus() {
        return status;
    }


    public Instant getExpiresAt() {
        return expiresAt;
    }

    public CopyId getCopyId() {
        return copyId;
    }

    public LoanId getLoanId() {
        return loanId;
    }
}
