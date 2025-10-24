package de.demo.lending.inventory.application.dto.event;

import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.inventory.domain.event.BookReserved;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
                e.getBookTitle(),
                e.getBookId(),
                e.getLoanId()

        );
    }
}
