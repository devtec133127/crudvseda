package de.demo.lending.procurement.application.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BookOrderedExternallyPayload extends DtoPayload {
    private static final String TYPE = "ProcurementInitiatedPayload";
    private final String bookId;
    private final String estimatedArrival;

    @JsonCreator
    public BookOrderedExternallyPayload(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") String occurredAt,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("type") String type,
            @JsonProperty("loanId") String loanId,
            @JsonProperty("userId") String userId,
            @JsonProperty("bookId") String bookId,
            @JsonProperty("estimatedArrival") String estimatedArrival
    ) {
        super(eventId, occurredAt, correlationId, causationId, loanId, userId, type);
        this.bookId = bookId;
        this.estimatedArrival = estimatedArrival;
    }
}
