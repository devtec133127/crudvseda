package de.demo.lending.payment.application.dto.event;

import de.demo.lending.payment.application.dto.PaymentCreatedPayload;
import de.demo.lending.payment.application.dto.PaymentFailedPayload;
import de.demo.lending.payment.domain.event.PaymentCreated;
import de.demo.lending.payment.domain.event.PaymentFailed;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public final class PaymentEventMapper {

    public static PaymentCreatedPayload toPayload(
            PaymentCreated e, String correlationId, String causationId) {

        return new PaymentCreatedPayload(
                UUID.randomUUID().toString(),
                e.getLoanId().toString(),
                e.getOccurredAt().toString(),
                correlationId,
                causationId,
                e.getUserId().toString(),
                e.getBookId().toString()
        );
    }

    public static PaymentFailedPayload toPayload(
            PaymentFailed e, String correlationId, String causationId) {

        return new PaymentFailedPayload(
                UUID.randomUUID().toString(),
                e.getLoanId().toString(),
                e.getBookId().toString(),
                e.getOccurredAt().toString(),
                correlationId,
                causationId,
                e.getUserId().toString(),
                e.getReason()
        );
    }
}
