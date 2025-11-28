package de.demo.lending.inventory.application.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BookReservedPayload extends DtoPayload {
    private static final String TYPE = "BookReservedPayload";
    private final String bookTitle;
    private final String copyId;
    private final String reservationId;

    @JsonCreator
    public BookReservedPayload(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") String occurredAt,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("type") String type,
            @JsonProperty("loanId") String loanId,
            @JsonProperty("userId") String userId,
            @JsonProperty("bookTitle") String bookTitle,
            @JsonProperty("copyId") String copyId,
            @JsonProperty("reservationId") String reservationId
    ) {
        super(eventId, occurredAt, correlationId, causationId, loanId, userId, type);
        this.bookTitle = bookTitle;
        this.copyId = copyId;
        this.reservationId = reservationId;
    }
}
