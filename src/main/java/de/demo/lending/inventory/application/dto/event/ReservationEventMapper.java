package de.demo.lending.inventory.application.dto.event;

import java.util.UUID;

import de.demo.lending.inventory.application.dto.ReservationCreatedPayload;
import de.demo.lending.inventory.domain.event.ReservationCreated;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReservationEventMapper {

    public static ReservationCreatedPayload toPayload(
            ReservationCreated e, String correlationId, String causationId) {

        return new ReservationCreatedPayload(
                UUID.randomUUID().toString(),          // eventId
                e.getOccurredAt().toString(),
                correlationId,
                causationId,
                e.getUserId().toString(),
                e.getBookTitle()
        );
    }
}
