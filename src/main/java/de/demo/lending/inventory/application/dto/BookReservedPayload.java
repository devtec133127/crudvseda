package de.demo.lending.inventory.application.dto;

import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;

@Getter
public class BookReservedPayload extends DtoPayload {
    private final String bookTitle;
    private final String bookId;

    public BookReservedPayload(String eventId, String occurredAt, String correlationId, String causationId, String userId,  String bookTitle,  String bookId) {
        super(eventId, occurredAt, correlationId, causationId, userId);
        this.bookId = bookId;
        this.bookTitle = bookTitle;
    }
}
