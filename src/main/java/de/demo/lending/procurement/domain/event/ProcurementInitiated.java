package de.demo.lending.procurement.domain.event;

import java.time.Instant;
import java.util.UUID;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrderId;

public class ProcurementInitiated extends BaseDomainEvent {

    private ProcurementOrderId procurementOrderId;
    private BookId bookId;
    private Isbn isbn;

    protected ProcurementInitiated(UUID eventId, ProcurementOrderId procurementOrderId,
                                   BookId bookId, Isbn isbn, LoanId loanId, Instant occurredAt, UserId userId) {
        super(eventId, loanId, "", "", occurredAt, userId);
        this.procurementOrderId = procurementOrderId;
        this.bookId = bookId;
        this.isbn = isbn;
    }

    public static ProcurementInitiated of(ProcurementOrderId procurementOrderId,
                                          BookId bookId, Isbn isbn, LoanId loanId, UserId userId) {
        return new ProcurementInitiated(
                UUID.randomUUID(),
                procurementOrderId,
                bookId,
                isbn,
                loanId,
                Instant.now(),
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
