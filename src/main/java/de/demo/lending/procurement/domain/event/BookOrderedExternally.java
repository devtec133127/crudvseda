package de.demo.lending.procurement.domain.event;

import java.time.Instant;
import java.util.UUID;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrderId;

public class BookOrderedExternally extends BaseDomainEvent {

    private ProcurementOrderId procurementOrderId;
    private String externalOrderId;
    private long estimatedArrival;
    private BookId bookId;

    protected BookOrderedExternally(UUID eventId, ProcurementOrderId procurementOrderId,
                                    String externalOrderId, LoanId loanId,
                                    Instant occurredAt, UserId userId, long estimatedArrival, BookId bookId) {
        super(eventId, loanId, "", "", occurredAt, userId);
        this.procurementOrderId = procurementOrderId;
        this.externalOrderId = externalOrderId;
        this.estimatedArrival = estimatedArrival;
        this.bookId = bookId;

    }

    public static BookOrderedExternally of(ProcurementOrderId procurementOrderId,
                                           String externalOrderId, LoanId loanId,
                                           UserId userId, long estimatedArrival, BookId bookId) {
        return new BookOrderedExternally(
                UUID.randomUUID(),
                procurementOrderId,
                externalOrderId,
                loanId,
                Instant.now(),
                userId,
                estimatedArrival,
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

    public long getEstimatedArrival() {
        return estimatedArrival;
    }
}
