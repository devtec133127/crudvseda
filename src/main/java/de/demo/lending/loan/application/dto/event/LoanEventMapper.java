package de.demo.lending.loan.application.dto.event;

import de.demo.lending.loan.domain.event.LoanRequested;
import de.demo.lending.loan.application.dto.LoanRequestedPayload;

import java.util.UUID;

public final class LoanEventMapper {
    private LoanEventMapper() {}

    public static LoanRequestedPayload toPayload(
            LoanRequested e, String correlationId, String causationId) {

        return new LoanRequestedPayload(
                "loan.requested",
                1,
                UUID.randomUUID().toString(),          // eventId
                e.occurredAt().toString(),
                correlationId,
                causationId,
                e.loanId().toString(),
                e.userId().toString(),
                e.bookTitle()
        );
    }
}
