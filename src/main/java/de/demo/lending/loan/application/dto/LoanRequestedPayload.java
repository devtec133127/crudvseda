package de.demo.lending.loan.application.dto;


public record LoanRequestedPayload(
        String type,        // "loan.requested"
        int version,        // 1
        String eventId,     // UUID as String
        String occurredAt,  // ISO timestamp
        String correlationId,
        String causationId,
        // fachliche Daten:
        String loanId,
        String userId,
        String bookTitle,
        long duration
) {
}