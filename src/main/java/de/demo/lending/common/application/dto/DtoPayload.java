package de.demo.lending.common.application.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public abstract class DtoPayload {

    private final UUID eventId;     // UUID as String
    private final String occurredAt;  // ISO timestamp
    private final String correlationId;
    private final String causationId;
    private final String loanId;
    private final String userId;

    private final String type;
}
