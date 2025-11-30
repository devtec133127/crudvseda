package de.demo.lending.inventory.application.dto.event;

import de.demo.lending.inventory.application.dto.BookNotFoundLocallyPayload;
import de.demo.lending.inventory.domain.event.BookNotFoundLocally;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookNotFoundLocallyMapper {
    public static BookNotFoundLocallyPayload toPayload(
            BookNotFoundLocally e, String correlationId, String causationId) {

        return BookNotFoundLocallyPayload.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(e.getOccurredAt().toString())
                .type(BookNotFoundLocally.class.getCanonicalName())
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(e.getUserId().value().toString())
                .bookId(e.getBookId().value())
                .loanId(e.getLoanId().value().toString()).build();
    }
}
