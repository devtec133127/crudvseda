package de.demo.lending.inventory.application.dto;

import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ReservationCreatedPayload extends DtoPayload {
    private static final String TYPE = "ReservationCreatedPayload";
    private final String bookTitle;

    /*public ReservationCreatedPayload(String eventId, String occurredAt, String correlationId, String causationId, String userId, String bookTitle) {
        super(eventId, occurredAt, correlationId, causationId, userId, TYPE);
        this.bookTitle = bookTitle;
    }*/
}