package de.demo.lending.loan.adapters.in.demo.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.demo.lending.loan.domain.Loan;
import lombok.Builder;
import lombok.Value;

/**
 * Response DTO für Demo-UI.
 * Enthält die initiale Rückmeldung nach Loan-Erstellung.
 */
@Value
@Builder
public class DemoLoanResponse {

    String id;
    String status;
    String bookTitle;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime timestamp;

    Double amount;
    String customerId;

    /**
     * Factory Method: Erstellt Response aus Domain Model
     */
    public static DemoLoanResponse from(Loan loan) {
        return DemoLoanResponse.builder()
                .id(loan.getLoanId().value().toString())
                .status(loan.getStatus().name())
                .timestamp(LocalDateTime.now())
                .bookTitle(loan.getBookTitle())
                .customerId(loan.getUserId().value().toString())
                .build();
    }
}