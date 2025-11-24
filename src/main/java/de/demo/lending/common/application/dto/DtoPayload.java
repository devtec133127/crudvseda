package de.demo.lending.common.application.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
public abstract class DtoPayload {

    private final UUID eventId;     // UUID as String
    private final String occurredAt;  // ISO timestamp
    private final String correlationId;
    private final String causationId;
    private final String userId;

    private final String type;
}
