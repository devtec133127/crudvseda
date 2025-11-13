package de.demo.lending.loan.adapters.in.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

/**
 * Event DTO für Server-Sent Events Stream.
 * Wird an die Demo-UI gestreamt.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DemoEvent {

    String type;              // z.B. "payment-completed", "inventory-reserved"
    String loanId;
    String message;           // User-friendly Nachricht
    Long timestamp;           // Unix timestamp in ms

    // Event-spezifische Felder (optional)
    String transactionId;     // Für Payment Events
    Double amount;            // Für Payment Events

    String reservationId;     // Für Inventory Events
    String article;           // Für Inventory Events

    // Metadaten
    Long elapsedMs;           // Zeit seit Loan-Erstellung

    String occuredAt;
    String userId;
    String bookId;
}