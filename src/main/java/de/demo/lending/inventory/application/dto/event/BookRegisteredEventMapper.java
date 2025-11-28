package de.demo.lending.inventory.application.dto.event;

import java.util.UUID;

import de.demo.lending.inventory.application.dto.BookRegisteredPayload;
import de.demo.lending.inventory.domain.event.BookRegistered;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookRegisteredEventMapper {
    public static BookRegisteredPayload toPayload(
            BookRegistered e, String correlationId, String causationId) {

        return BookRegisteredPayload.builder()
                .eventId(UUID.randomUUID())
                .type(BookRegistered.class.getCanonicalName())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(e.getUserId() != null ? e.getUserId().value().toString() : null)
                .bookId(e.getBookId().value())
                .reservationId(e.getReservationId().value().toString())
                .loanId(e.getLoanId().value().toString()).build();
    }
}
