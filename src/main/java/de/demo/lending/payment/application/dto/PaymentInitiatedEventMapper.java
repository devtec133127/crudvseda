package de.demo.lending.payment.application.dto;

import java.util.UUID;

import de.demo.lending.common.events.BookReserved;
import de.demo.lending.payment.domain.event.PaymentInitiated;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentInitiatedEventMapper {
    public static PaymentInitiatedPayload toPayload(
            PaymentInitiated e, String correlationId, String causationId) {

        return PaymentInitiatedPayload.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(e.getOccurredAt().toString())
                .type(BookReserved.class.getCanonicalName())
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(e.getUserId() != null ? e.getUserId().value().toString() : null)
                .loanId(e.getLoanId().value().toString()).build();
    }
}
