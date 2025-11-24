package de.demo.lending.inventory.application.dto.event;

import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.inventory.domain.event.BookReserved;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookReservedEventMapper {
    public static BookReservedPayload toPayload(
            BookReserved e, String correlationId, String causationId) {

        return BookReservedPayload.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(e.getUserId().value().toString())
                .bookTitle(e.getBookTitle())
                .bookId(e.getBookId().value())
                .reservationId(e.getReservationId().value().toString())
                .loanId(e.getLoanId().value().toString()).build();
    }
}
