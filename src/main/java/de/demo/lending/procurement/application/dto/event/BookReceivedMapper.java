package de.demo.lending.procurement.application.dto.event;

import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.procurement.application.dto.BookReceivedPayload;
import de.demo.lending.procurement.domain.event.BookReceived;
import org.springframework.stereotype.Component;

@Component
public class BookReceivedMapper {

    private BookReceivedMapper() {
    }

    public static BookReceivedPayload toPayload(
            BookReceived e, String correlationId, String causationId) {

        UserId userId = e.getUserId();
        return BookReceivedPayload.builder()
                .eventId(e.getEventId())
                .type("procurement.book.received.v1")
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .causationId(causationId)
                .loanId(e.getLoanId().value().toString())
                .userId(userId != null ? e.getUserId().value().toString() : null)
                .bookId(e.getBookId().value())
                .isbn(e.getIsbn().value())
                .build();
    }
}
