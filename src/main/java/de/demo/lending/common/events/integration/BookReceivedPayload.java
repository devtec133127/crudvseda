package de.demo.lending.common.events.integration;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BookReceivedPayload extends DtoPayload {
    private static final String TYPE = "BookReceivedPayload";
    private final String bookId;
    private final String isbn;

    @JsonCreator
    public BookReceivedPayload(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") String occurredAt,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("type") String type,
            @JsonProperty("loanId") String loanId,
            @JsonProperty("userId") String userId,
            @JsonProperty("bookId") String bookId,
            @JsonProperty("isbn") String isbn
    ) {
        super(eventId, occurredAt, correlationId, causationId, loanId, userId, type);
        this.bookId = bookId;
        this.isbn = isbn;
    }
}
