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

        return BookReservedPayload.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(e.getOccurredAt().toString())
                .type(BookReserved.class.getCanonicalName())
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(e.getUserId() != null ? e.getUserId().value().toString() : null)
                .bookTitle(e.getBookTitle().toString())
                .bookId(e.getBookId().value())
                .reservationId(e.getReservationId().value().toString())
                .loanId(e.getLoanId().value().toString()).build();
    }
}
