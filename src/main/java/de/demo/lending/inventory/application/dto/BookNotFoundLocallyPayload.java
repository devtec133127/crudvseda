package de.demo.lending.inventory.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
public class BookNotFoundLocallyPayload extends DtoPayload {
    private static final String TYPE = "BookNotFoundLocallyPayload";
    private final String isbn;

    @JsonCreator
    public BookNotFoundLocallyPayload(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") String occurredAt,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("type") String type,
            @JsonProperty("loanId") String loanId,
            @JsonProperty("userId") String userId,
            @JsonProperty("isbn") String isbn
    ) {
        super(eventId, occurredAt, correlationId, causationId, loanId, userId, type);
        this.isbn = isbn;
    }
}
