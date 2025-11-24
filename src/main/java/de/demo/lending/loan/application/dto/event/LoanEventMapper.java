package de.demo.lending.loan.application.dto.event;

import de.demo.lending.loan.application.dto.LoanRequestedPayload;
import de.demo.lending.loan.domain.event.LoanRequested;

public final class LoanEventMapper {
    private LoanEventMapper() {
    }

    public static LoanRequestedPayload toPayload(
            LoanRequested e, String correlationId, String causationId) {

        LoanRequestedPayload payload = LoanRequestedPayload.builder()
                .type("loan.requested")
                .eventId(e.getEventId())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .causationId(causationId)
                .loanId(e.getLoanId().value().toString())
                .userId(e.getUserId().value().toString())
                .bookTitle(e.getBookTitle())
                .duration(e.getDuration().toDays()).build();
        return payload;
    }
}
