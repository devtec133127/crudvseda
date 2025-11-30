package de.demo.lending.inventory.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
public class BookReservedPayload extends DtoPayload {
    private static final String TYPE = "BookReservedPayload";
    private final String bookId;
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
            @JsonProperty("bookId") String bookId,
            @JsonProperty("copyId") String copyId,
            @JsonProperty("reservationId") String reservationId,
            @JsonProperty("dueDate") String dueDate
    ) {
        super(eventId, occurredAt, correlationId, causationId, loanId, userId, type);
        this.bookId = bookId;
        this.copyId = copyId;
        this.reservationId = reservationId;
    }
}
