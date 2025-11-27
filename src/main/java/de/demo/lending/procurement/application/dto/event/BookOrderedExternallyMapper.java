package de.demo.lending.procurement.application.dto.event;

import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.procurement.application.dto.BookOrderedExternallyPayload;
import de.demo.lending.procurement.domain.event.BookOrderedExternally;
import org.springframework.stereotype.Component;

@Component
public class BookOrderedExternallyMapper {

    private BookOrderedExternallyMapper() {
    }

    public static BookOrderedExternallyPayload toPayload(
            BookOrderedExternally e, String correlationId, String causationId) {

        UserId userId = e.getUserId();
        BookOrderedExternallyPayload payload = BookOrderedExternallyPayload.builder()
                .type("loan.requested")
                .eventId(e.getEventId())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .causationId(causationId)
                .loanId(e.getLoanId().value().toString())
                .userId(userId != null ? e.getUserId().value().toString() : null)
                .bookId(e.getBookId().toString())
                .estimatedArrival(Long.toString(e.getEstimatedArrival()))
                .build();
        return payload;
    }
}
