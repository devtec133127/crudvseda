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
                .eventId(UUID.randomUUID().toString())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(e.getUserId().value().toString())
                .bookTitle(e.getBookTitle())
                .bookId(e.getBookId())
                .loanId(e.getLoanId()).build();
    }
}
