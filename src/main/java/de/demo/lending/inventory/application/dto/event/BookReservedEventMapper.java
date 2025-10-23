package de.demo.lending.inventory.application.dto.event;

import java.util.UUID;

import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.inventory.application.dto.ReservationCreatedPayload;
import de.demo.lending.inventory.domain.event.BookReserved;
import de.demo.lending.inventory.domain.event.ReservationCreated;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BookReservedEventMapper {
    public static BookReservedPayload toPayload(
            BookReserved e, String correlationId, String causationId) {

        return new BookReservedPayload(
                UUID.randomUUID().toString(),          // eventId
                e.getOccurredAt().toString(),
                correlationId,
                causationId,
                e.getUserId().value().toString(),
                e.getBookId(),
                e.getBookTitle()
        );
    }
}
