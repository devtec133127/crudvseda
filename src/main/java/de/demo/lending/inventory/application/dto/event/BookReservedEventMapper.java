package de.demo.lending.inventory.application.dto.event;

import java.util.UUID;

import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.inventory.domain.event.BookReserved;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
