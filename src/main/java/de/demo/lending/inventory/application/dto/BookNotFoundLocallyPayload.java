package de.demo.lending.inventory.application.dto;

import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BookNotFoundLocallyPayload extends DtoPayload {
    private static final String TYPE = "BookNotFoundLocallyPayload";
    private final String bookTitle;
    private final String loanId;
}
