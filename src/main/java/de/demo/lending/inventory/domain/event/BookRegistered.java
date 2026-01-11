package de.demo.lending.inventory.domain.event;

import java.time.Instant;
import java.util.UUID;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.inventory.domain.ReservationId;

public final class BookRegistered extends BaseDomainEvent {
    private final CopyId copyId;
    private final BookId bookId;
    private final ReservationId reservationId;

    public BookRegistered(LoanId loanId, String correlationId, String causationId,
                          CopyId copyId, BookId bookId, ReservationId reservationId) {
        super(UUID.randomUUID(), loanId, correlationId, causationId, Instant.now(), null);
        this.copyId = copyId;
        this.bookId = bookId;
        this.reservationId = reservationId;
    }

    public ReservationId getReservationId() {
        return reservationId;
    }

    public CopyId getCopyId() {
        return copyId;
    }

    public BookId getBookId() {
        return bookId;
    }
}
