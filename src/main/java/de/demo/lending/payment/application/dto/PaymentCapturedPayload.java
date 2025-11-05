package de.demo.lending.payment.application.dto;

import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PaymentCapturedPayload extends DtoPayload {

    private static final String TYPE = "PaymentCapturedPayload";
    private final String loanId;
    private final String bookId;

    /*public PaymentCapturedPayload(String eventId, String loanId, String occurredAt, String correlationId,
                                  String causationId, String userId, String bookId) {
        super(eventId, occurredAt, correlationId, causationId, userId, TYPE);
        this.bookId = bookId;
        this.loanId = loanId;
    }*/
}
