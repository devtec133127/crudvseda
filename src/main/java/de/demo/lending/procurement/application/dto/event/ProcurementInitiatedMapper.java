package de.demo.lending.procurement.application.dto.event;

import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.procurement.application.dto.ProcurementInitiatedPayload;
import de.demo.lending.procurement.domain.event.ProcurementInitiated;
import org.springframework.stereotype.Component;

@Component
public class ProcurementInitiatedMapper {

    private ProcurementInitiatedMapper() {
    }

    public static ProcurementInitiatedPayload toPayload(
            ProcurementInitiated e, String correlationId, String causationId) {

        UserId userId = e.getUserId();
        ProcurementInitiatedPayload payload = ProcurementInitiatedPayload.builder()
                .type("loan.requested")
                .eventId(e.getEventId())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .causationId(causationId)
                .loanId(e.getLoanId().value().toString())
                .userId(userId != null ? e.getUserId().value().toString() : null)
                .bookTitle(e.getBookId().toString())
                .build();
        return payload;
    }
}
