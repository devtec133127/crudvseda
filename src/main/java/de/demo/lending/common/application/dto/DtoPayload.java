package de.demo.lending.common.application.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class DtoPayload {

    private final String eventId;     // UUID as String
    private final String occurredAt;  // ISO timestamp
    private final String correlationId;
    private final String causationId;
    private final String userId;

    private final String type;
}
