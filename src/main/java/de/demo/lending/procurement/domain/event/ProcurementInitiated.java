package de.demo.lending.procurement.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrderId;

import java.time.Instant;
import java.util.UUID;

public class ProcurementInitiated extends BaseDomainEvent {

    private ProcurementOrderId procurementOrderId;
    private BookTitle bookTitle;

    protected ProcurementInitiated(UUID eventId, ProcurementOrderId procurementOrderId,
                                   BookTitle bookTitle, LoanId loanId, Instant occurredAt, UserId userId) {
        super(eventId, loanId, "", "", occurredAt, userId);
        this.procurementOrderId = procurementOrderId;
        this.bookTitle = bookTitle;
    }

    public static ProcurementInitiated of(ProcurementOrderId procurementOrderId,
                                          BookTitle bookTitle, LoanId loanId, UserId userId) {
        return new ProcurementInitiated(
                UUID.randomUUID(),
                procurementOrderId,
                bookTitle,
                loanId,
                Instant.now(),
                userId
        );
    }

    public ProcurementOrderId getProcurementOrderId() {
        return procurementOrderId;
    }

    public BookTitle getBookTitle() {
        return bookTitle;
    }
}
