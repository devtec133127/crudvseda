package de.demo.lending.payment.application.dto.event;

import java.util.UUID;

import de.demo.lending.payment.application.dto.PaymentCapturedPayload;
import de.demo.lending.payment.application.dto.PaymentCreatedPayload;
import de.demo.lending.payment.application.dto.PaymentFailedPayload;
import de.demo.lending.payment.domain.event.PaymentCreated;
import de.demo.lending.payment.domain.event.PaymentFailed;
import de.demo.lending.payment.domain.event.PaymentInitiated;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class PaymentEventMapper {

    public static PaymentCreatedPayload toPayload(
            PaymentCreated e, String correlationId, String causationId) {

        return PaymentCreatedPayload.builder()
                .eventId(UUID.randomUUID())
                .loanId(e.getLoanId().toString())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(e.getUserId().toString())
                .bookId(e.getBookId().value()).build();
    }

    public static PaymentCapturedPayload toPayload(
            PaymentInitiated e, String correlationId, String causationId) {

        return PaymentCapturedPayload.builder()
                .eventId(UUID.randomUUID())
                .loanId(e.getLoanId().toString())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(e.getUserId().toString())
                .type(PaymentInitiated.class.getSimpleName())
                .build();
    }

    public static PaymentFailedPayload toPayload(
            PaymentFailed e, String correlationId, String causationId) {

        return PaymentFailedPayload.builder()
                .eventId(UUID.randomUUID())
                .loanId(e.getLoanId().toString())
                .occurredAt(e.getOccurredAt().toString())
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(e.getUserId().toString())
                .bookId(e.getBookId().value()).build();
    }
}
