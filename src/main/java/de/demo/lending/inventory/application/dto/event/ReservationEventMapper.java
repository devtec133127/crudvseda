package de.demo.lending.inventory.application.dto.event;

import de.demo.lending.inventory.application.dto.ReservationCreatedPayload;
import de.demo.lending.inventory.domain.event.ProcurementRequested;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReservationEventMapper {

    public static ReservationCreatedPayload toPayload(
            ProcurementRequested e, String correlationId, String causationId) {

        return ReservationCreatedPayload.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .isAvailable(e.isAvailable())
                .causationId(causationId)
                .userId(e.getUserId().toString())
                .bookTitle(e.getBookTitle()).build();
    }
}
