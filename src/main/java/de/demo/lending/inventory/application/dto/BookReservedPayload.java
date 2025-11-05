package de.demo.lending.inventory.application.dto;

import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BookReservedPayload extends DtoPayload {
    private static final String TYPE = "BookReservedPayload";
    private final String bookTitle;
    private final String bookId;
    private final String loanId;

    /*public BookReservedPayload(String eventId, String occurredAt, String correlationId,
                               String causationId, String userId, String bookTitle,
                               String bookId, String loanId) {
        super(eventId, occurredAt, correlationId, causationId, userId, TYPE);
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.loanId = loanId;
    }*/
}
