package de.demo.lending.payment.application.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PaymentInitiatedPayload extends DtoPayload {
    private static final String TYPE = "PaymentInitiatedPayload";

    @JsonCreator
    public PaymentInitiatedPayload(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") String occurredAt,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("type") String type,
            @JsonProperty("loanId") String loanId,
            @JsonProperty("userId") String userId
    ) {
        super(eventId, occurredAt, correlationId, causationId, loanId, userId, type);
    }
}
