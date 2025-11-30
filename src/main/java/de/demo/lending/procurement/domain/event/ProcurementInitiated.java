package de.demo.lending.procurement.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrderId;

import java.time.Instant;
import java.util.UUID;

public class ProcurementInitiated extends BaseDomainEvent {

    private ProcurementOrderId procurementOrderId;
    private BookId bookId;

    protected ProcurementInitiated(UUID eventId, ProcurementOrderId procurementOrderId,
                                   BookId bookId, LoanId loanId, Instant occurredAt, UserId userId) {
        super(eventId, loanId, "", "", occurredAt, userId);
        this.procurementOrderId = procurementOrderId;
        this.bookId = bookId;
    }

    public static ProcurementInitiated of(ProcurementOrderId procurementOrderId,
                                          BookId bookId, LoanId loanId, UserId userId) {
        return new ProcurementInitiated(
                UUID.randomUUID(),
                procurementOrderId,
                bookId,
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
}
