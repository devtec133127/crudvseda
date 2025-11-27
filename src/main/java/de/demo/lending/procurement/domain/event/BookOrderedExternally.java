package de.demo.lending.procurement.domain.event;

import de.demo.lending.common.domain.events.BaseDomainEvent;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.ProcurementOrderId;

import java.time.Instant;
import java.util.UUID;

public class BookOrderedExternally extends BaseDomainEvent {

    private ProcurementOrderId procurementOrderId;
    private String externalOrderId;
    private long estimatedArrival;

    protected BookOrderedExternally(UUID eventId, ProcurementOrderId procurementOrderId,
                                    String externalOrderId, LoanId loanId,
                                    Instant occurredAt, UserId userId, long estimatedArrival) {
        super(eventId, loanId, "", "", occurredAt, userId);
        this.procurementOrderId = procurementOrderId;
        this.externalOrderId = externalOrderId;
        this.estimatedArrival = estimatedArrival;
    }

    public static BookOrderedExternally of(ProcurementOrderId procurementOrderId,
                                           String externalOrderId, LoanId loanId,
                                           UserId userId, long estimatedArrival) {
        return new BookOrderedExternally(
                UUID.randomUUID(),
                procurementOrderId,
                externalOrderId,
                loanId,
                Instant.now(),
                userId,
                estimatedArrival
        );
    }

    public ProcurementOrderId getProcurementOrderId() {
        return procurementOrderId;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }
}
