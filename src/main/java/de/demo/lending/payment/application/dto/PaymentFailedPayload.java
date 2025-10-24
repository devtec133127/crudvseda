package de.demo.lending.payment.application.dto;

import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;

@Getter
public class PaymentFailedPayload extends DtoPayload {
    private static final String TYPE = "PaymentFailedPayload";
    private final String loanId;
    private final String bookId;
    private final String reason;

    public PaymentFailedPayload(String eventId, String loanId, String occurredAt, String correlationId,
                                String causationId, String userId, String bookId, String reason) {
        super(eventId, occurredAt, correlationId, causationId, userId, TYPE);
        this.bookId = bookId;
        this.loanId = loanId;
        this.reason = reason;
    }
}
