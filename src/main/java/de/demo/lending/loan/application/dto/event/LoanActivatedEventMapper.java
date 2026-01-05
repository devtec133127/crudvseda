package de.demo.lending.loan.application.dto.event;

import java.util.UUID;

import de.demo.lending.common.events.BookReserved;
import de.demo.lending.common.events.integration.LoanActivatedPayload;
import de.demo.lending.loan.domain.event.LoanActivated;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoanActivatedEventMapper {
    public static LoanActivatedPayload toPayload(
            LoanActivated e, String correlationId, String causationId) {

        return LoanActivatedPayload.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(e.getOccurredAt().toString())
                .type(BookReserved.class.getCanonicalName())
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(e.getUserId() != null ? e.getUserId().value().toString() : null)
                .copyId(e.getCopyId().value().toString())
                .dueDate(e.getDueDate() != null ? e.getDueDate().toString() : null)
                .loanId(e.getLoanId().value().toString()).build();
    }
}
