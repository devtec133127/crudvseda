package de.demo.lending.procurement.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrderId;

import java.time.Instant;
import java.util.UUID;

public class BookReceived extends BaseDomainEvent {

    private ProcurementOrderId procurementOrderId;
    private String externalOrderId;
    private BookId bookId;

    protected BookReceived(UUID eventId, ProcurementOrderId procurementOrderId,
                           String externalOrderId, LoanId loanId,
                           Instant occurredAt, BookId bookId) {
        super(eventId, loanId, "", "", occurredAt);
        this.procurementOrderId = procurementOrderId;
        this.externalOrderId = externalOrderId;
        this.bookId = bookId;
    }

    public static BookReceived of(ProcurementOrderId procurementOrderId,
                                  String externalOrderId, LoanId loanId, BookId bookId) {
        return new BookReceived(
                UUID.randomUUID(),
                procurementOrderId,
                externalOrderId,
                loanId,
                Instant.now(),
                bookId
        );
    }

    public ProcurementOrderId getProcurementOrderId() {
        return procurementOrderId;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public BookId getBookId() {
        return bookId;
    }
}
