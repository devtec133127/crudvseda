package de.demo.lending.procurement.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.Isbn;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrderId;

import java.time.Instant;
import java.util.UUID;

public class BookOrderedExternally extends BaseDomainEvent {

    private ProcurementOrderId procurementOrderId;
    private long estimatedArrival;
    private BookId bookId;
    private Isbn isbn;

    protected BookOrderedExternally(UUID eventId, ProcurementOrderId procurementOrderId,
                                    LoanId loanId,
                                    Instant occurredAt, UserId userId, long estimatedArrival,
                                    BookId bookId, Isbn isbn) {
        super(eventId, loanId, "", "", occurredAt, userId);
        this.procurementOrderId = procurementOrderId;
        this.estimatedArrival = estimatedArrival;
        this.bookId = bookId;
        this.isbn = isbn;

    }

    public static BookOrderedExternally of(ProcurementOrderId procurementOrderId, LoanId loanId,
                                           UserId userId, long estimatedArrival, BookId bookId, Isbn isbn) {
        return new BookOrderedExternally(
                UUID.randomUUID(),
                procurementOrderId,
                loanId,
                Instant.now(),
                userId,
                estimatedArrival,
                bookId,
                isbn
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

    public long getEstimatedArrival() {
        return estimatedArrival;
    }
}
