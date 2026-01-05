package de.demo.lending.common.events.integration;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class LoanActivatedPayload extends DtoPayload {
    private static final String TYPE = "LoanActivatedPayload";
    private final String copyId;
    private final String dueDate;

    @JsonCreator
    public LoanActivatedPayload(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") String occurredAt,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("type") String type,
            @JsonProperty("loanId") String loanId,
            @JsonProperty("userId") String userId,
            @JsonProperty("copyId") String copyId,
            @JsonProperty("dueDate") String dueDate
    ) {
        super(eventId, occurredAt, correlationId, causationId, loanId, userId, type);
        this.copyId = copyId;
        this.dueDate = dueDate;
    }
}
