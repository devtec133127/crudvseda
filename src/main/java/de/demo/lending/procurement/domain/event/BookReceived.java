package de.demo.lending.procurement.domain.event;

import java.time.Instant;
import java.util.UUID;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrderId;

public class BookReceived extends BaseDomainEvent {

    private ProcurementOrderId procurementOrderId;
    private BookId bookId;
    private Isbn isbn;

    protected BookReceived(UUID eventId, ProcurementOrderId procurementOrderId,
                           LoanId loanId, Instant occurredAt, BookId bookId, Isbn isbn, UserId userId) {
        super(eventId, loanId, "", "", occurredAt, userId);
        this.procurementOrderId = procurementOrderId;
        this.bookId = bookId;
        this.isbn = isbn;
    }

    public static BookReceived of(ProcurementOrderId procurementOrderId, LoanId loanId,
                                  BookId bookId, Isbn isbn, UserId userId) {
        return new BookReceived(
                UUID.randomUUID(),
                procurementOrderId,
                loanId,
                Instant.now(),
                bookId,
                isbn,
                userId
        );
    }

    public ProcurementOrderId getProcurementOrderId() {
        return procurementOrderId;
    }

    public BookId getBookId() {
        return bookId;
    }

    public Isbn getIsbn() {
        return isbn;
    }
}
