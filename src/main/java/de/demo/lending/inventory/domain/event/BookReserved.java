package de.demo.lending.inventory.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.ReservationId;
import de.demo.lending.loan.domain.LoanId;

public class BookReserved extends BaseDomainEvent {
    private final CopyId copyId;
    private final String bookTitle;
    private final BookId bookId;
    private final ReservationId reservationId;

    public BookReserved(LoanId loanId, String correlationId, String causationId,
                        CopyId copyId, String bookTitle, UserId userId, BookId bookId, ReservationId reservationId) {
        super(UUID.randomUUID().toString(), loanId, correlationId, causationId, Instant.now(), userId);
        this.copyId = copyId;
        this.bookTitle = Objects.requireNonNull(bookTitle);
        this.bookId = bookId;
        this.reservationId = reservationId;
    }

    public ReservationId getReservationId() {
        return reservationId;
    }

    public CopyId getCopyId() {
        return copyId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public BookId getBookId() {
        return bookId;
    }
}
