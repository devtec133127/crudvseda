package de.demo.lending.inventory.application.dto.event;

import de.demo.lending.inventory.application.dto.ReservationCreatedPayload;
import de.demo.lending.inventory.domain.event.ReservationCreated;
import de.demo.lending.loan.application.dto.LoanRequestedPayload;
import de.demo.lending.loan.domain.event.LoanRequested;

import java.util.UUID;

public final class ReservationEventMapper {
    private ReservationEventMapper() {}

    public static ReservationCreatedPayload toPayload(
            ReservationCreated e, String correlationId, String causationId) {

        return new ReservationCreatedPayload(
                "reservation.created",
                1,
                UUID.randomUUID().toString(),          // eventId
                e.occurredAt().toString(),
                correlationId,
                causationId,
                e.loanId().toString(),
                e.userId().toString(),
                e.bookTitle()
        );
    }
}
