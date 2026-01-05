package de.demo.lending.inventory.application.dto.event;

import java.util.UUID;

import de.demo.lending.common.events.BookReserved;
import de.demo.lending.common.events.integration.BookReservedPayload;
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
                .bookId(e.getBookId().toString())
                .copyId(e.getCopyId().value().toString())
                .reservationId(e.getReservationId().value().toString())
                .loanId(e.getLoanId().value().toString()).build();
    }
}
