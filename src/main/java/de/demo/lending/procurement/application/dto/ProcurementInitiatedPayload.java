package de.demo.lending.procurement.application.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ProcurementInitiatedPayload extends DtoPayload {
    private static final String TYPE = "ProcurementInitiatedPayload";
    private final String bookTitle;

    @JsonCreator
    public ProcurementInitiatedPayload(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") String occurredAt,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("type") String type,
            @JsonProperty("loanId") String loanId,
            @JsonProperty("userId") String userId,
            @JsonProperty("bookTitle") String bookTitle
    ) {
        super(eventId, occurredAt, correlationId, causationId, loanId, userId, type);
        this.bookTitle = bookTitle;
    }
}
