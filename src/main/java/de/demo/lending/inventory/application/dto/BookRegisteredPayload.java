package de.demo.lending.inventory.application.dto;

import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BookRegisteredPayload extends DtoPayload {
    private static final String TYPE = "BookRegisteredPayload";
    private final String bookId;
    private final String reservationId;
}
