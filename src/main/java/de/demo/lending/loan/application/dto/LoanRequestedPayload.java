package de.demo.lending.loan.application.dto;


import de.demo.lending.common.application.dto.DtoPayload;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class LoanRequestedPayload extends DtoPayload {

    // fachliche Daten:
    private String loanId;
    private String userId;
    private String bookTitle;
    private long duration;
}