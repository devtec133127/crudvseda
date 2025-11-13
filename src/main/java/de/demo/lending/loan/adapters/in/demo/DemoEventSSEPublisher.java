package de.demo.lending.loan.adapters.in.demo;

import de.demo.lending.loan.adapters.in.demo.dto.DemoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demo Event Listener für die Präsentations-UI.
 * <p>
 * Übersetzt Domain Events in UI-freundliche DemoEvents und
 * leitet sie an den DemoEventStore für SSE-Streaming weiter.
 * <p>
 * Dieser Listener ist NICHT Teil der Production-Logik und dient
 * ausschließlich der Demonstration von Event-Driven Architecture.
 *
 */
@Slf4j
@Component
public class DemoEventSSEPublisher {

    private final DemoEventStore eventStore;

    // Speichert Start-Timestamp pro Loan für elapsed-time Berechnung
    private final Map<String, Long> loanStartTimes = new ConcurrentHashMap<>();

    public DemoEventSSEPublisher(DemoEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void publishLoanCreatedToUI(UUID loanUuid, UUID userUuid, String bookTitle, long durationDays) {
        // Speichere Start-Timestamp für elapsed-time Berechnung
        loanStartTimes.put(loanUuid.toString(), System.currentTimeMillis());

        // Optional: Initial Event an UI senden
        DemoEvent demoEvent = DemoEvent.builder()
                .type("loan-created")
                .loanId(loanUuid.toString())
                .userId(userUuid.toString())
                .message("Loan Request erstellt - Events werden verarbeitet...")
                .timestamp(System.currentTimeMillis())
                .elapsedMs(calculateElapsedTime(loanUuid.toString()))
                .build();

        eventStore.publishEvent(loanUuid.toString(), demoEvent);
    }

    /**
     * Reagiert auf PaymentCompletedEvent.
     * Übersetzt Domain Event in UI-freundliches DemoEvent.
     */
    /*@Async  // Wichtig: Asynchron, blockiert nicht den Domain Event Handler
    @EventListener
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Demo UI: PaymentCompletedEvent empfangen für Loan-ID: {}", event.getLoanId());

        try {
            // Domain Event → Demo Event übersetzen
            DemoEvent demoEvent = DemoEvent.builder()
                    .type("payment-completed")
                    .loanId(event.getLoanId().toString())
                    .message("Zahlung erfolgreich verarbeitet")
                    .timestamp(System.currentTimeMillis())

                    // Payment-spezifische Felder
                    .transactionId(event.getTransactionId().toString())
                    .amount(extractAmount(event))

                    // Elapsed time seit Loan-Erstellung
                    .elapsedMs(calculateElapsedTime(event.getLoanId().toString()))
                    .build();

            // An DemoEventStore für SSE-Streaming weiterleiten
            eventStore.publishEvent(event.getLoanId().toString(), demoEvent);

            log.debug("Demo UI: PaymentCompletedEvent erfolgreich an EventStore weitergeleitet");

        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten von PaymentCompletedEvent für Demo UI", e);
            // Fehler nicht weiterwerfen - Demo soll Production nicht beeinflussen
        }
    }*/

    /**
     * Reagiert auf InventoryReservedEvent.
     * Übersetzt Domain Event in UI-freundliches DemoEvent.
     */
    /*@Async
    @EventListener
    public void onInventoryReserved(InventoryReservedEvent event) {
        log.info("Demo UI: InventoryReservedEvent empfangen für Loan-ID: {}", event.getLoanId());

        try {
            DemoEvent demoEvent = DemoEvent.builder()
                    .type("inventory-reserved")
                    .loanId(event.getLoanId().toString())
                    .message("Reservierung erfolgreich erstellt")
                    .timestamp(System.currentTimeMillis())

                    // Inventory-spezifische Felder
                    .reservationId(event.getReservationId().toString())
                    .article(event.getArticle())

                    // Elapsed time seit Loan-Erstellung
                    .elapsedMs(calculateElapsedTime(event.getLoanId().toString()))
                    .build();

            eventStore.publishEvent(event.getLoanId().toString(), demoEvent);

            log.debug("Demo UI: InventoryReservedEvent erfolgreich an EventStore weitergeleitet");

        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten von InventoryReservedEvent für Demo UI", e);
        }
    }*/

    /**
     * Optional: Reagiert auf LoanFinalizedEvent (wenn beide Services fertig sind).
     */
    /*@Async
    @EventListener
    public void onLoanFinalized(LoanFinalizedEvent event) {
        log.info("Demo UI: LoanFinalizedEvent empfangen für Loan-ID: {}", event.getLoanId());

        try {
            DemoEvent demoEvent = DemoEvent.builder()
                    .type("loan-finalized")
                    .loanId(event.getLoanId().toString())
                    .message("✅ Loan Request vollständig verarbeitet")
                    .timestamp(System.currentTimeMillis())
                    .elapsedMs(calculateElapsedTime(event.getLoanId().toString()))
                    .build();

            eventStore.publishEvent(event.getLoanId().toString(), demoEvent);

            // Cleanup: Timestamp entfernen (Memory Leak vermeiden)
            loanStartTimes.remove(event.getLoanId().toString());

        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten von LoanFinalizedEvent für Demo UI", e);
        }
    }*/

    /**
     * Berechnet die verstrichene Zeit seit Loan-Erstellung.
     */
    private Long calculateElapsedTime(String loanId) {
        Long startTime = loanStartTimes.get(loanId);
        if (startTime == null) {
            log.warn("Kein Start-Timestamp gefunden für Loan-ID: {} - verwende 0ms", loanId);
            return 0L;
        }
        return System.currentTimeMillis() - startTime;
    }
}
