package de.demo.lending.loan.application.dto;


import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class LoanRequestedPayload extends DtoPayload {
    // fachliche Daten:
    private String bookTitle;
    private long duration;

    @JsonCreator
    public LoanRequestedPayload(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") String occurredAt,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("type") String type,
            @JsonProperty("loanId") String loanId,
            @JsonProperty("userId") String userId,
            @JsonProperty("bookTitle") String bookTitle,
            @JsonProperty("duration") long duration
    ) {
        super(eventId, occurredAt, correlationId, causationId, loanId, userId, type);
        this.bookTitle = bookTitle;
        this.duration = duration;
    }
}