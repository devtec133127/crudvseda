package de.demo.lending.inventory.application.dto.event;

import de.demo.lending.inventory.application.dto.ReservationCreatedPayload;
import de.demo.lending.inventory.domain.event.BookAvailabilityCheckedEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReservationEventMapper {

    public static ReservationCreatedPayload toPayload(
            BookAvailabilityCheckedEvent e, String correlationId, String causationId) {

        return ReservationCreatedPayload.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .reservationId(e.getReservationId().value().toString())
                .causationId(causationId)
                .userId(e.getUserId().toString())
                .bookTitle(e.getBookTitle()).build();
    }
}
