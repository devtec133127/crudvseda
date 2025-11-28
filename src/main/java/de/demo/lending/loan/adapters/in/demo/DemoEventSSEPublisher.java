package de.demo.lending.loan.adapters.in.demo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.async.AsyncEventBus;
import de.demo.lending.common.events.Topics;
import de.demo.lending.loan.adapters.in.demo.dto.DemoEvent;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    private final AsyncEventBus eventBus;
    private final DemoEventStore eventStore;
    private final ObjectMapper objectMapper;

    // Speichert Start-Timestamp pro Loan für elapsed-time Berechnung
    private final Map<String, Long> loanStartTimes = new ConcurrentHashMap<>();

    public DemoEventSSEPublisher(AsyncEventBus eventBus, DemoEventStore eventStore) {
        this.eventBus = eventBus;
        this.eventStore = eventStore;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void subscribe() {
        eventBus.subscribe(Topics.LOAN_REQUESTED_V1, this::fireAnalyticsEvent);
        eventBus.subscribe(Topics.LOAN_REQUESTED_V1, this::fireNotificationEvent);
        eventBus.subscribe(Topics.LOAN_REQUESTED_V1, this::fireFraudDetection);
        eventBus.subscribe(Topics.LOAN_REQUESTED_V1, this::firePaymentFinished);
    }

    private long randomBeetween(int min, int max) {
        return min + (long) (Math.random() * (max - min));
    }

    private DemoEvent createDemoEvent(String eventJson, String title, String message) {
        JsonNode node = null;
        try {
            node = objectMapper.readTree(eventJson);

            String incomingEventId = node.has("eventId") ? node.get("eventId").asText(null) : null;
            // mandatory fields expected: loanId, bookId
            if (!node.has("loanId") || !node.has("bookId")) {
                log.warn("Received event without loanId/bookId: {}", eventJson);
            }

            UUID loanUuid = UUID.fromString(node.get("loanId").asText());
            UUID userUuid = UUID.fromString(node.get("userId").asText());
            String reservationId = "";
            if (node.has("reservationId")) {
                reservationId = UUID.fromString(node.get("reservationId").asText()).toString();
            }

            String bookId = "";
            if (node.has("bookId")) {
                bookId = node.get("bookId").asText();
            }

            return DemoEvent.builder()
                    .type(title)
                    .loanId(loanUuid.toString())
                    .userId(userUuid.toString())
                    .message(message)
                    .reservationId(reservationId)
                    .bookId(bookId)
                    .timestamp(System.currentTimeMillis())
                    .elapsedMs(calculateElapsedTime(loanUuid.toString()))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void fireAnalyticsEvent(String eventJson) {
        try {
            Thread.sleep(randomBeetween(100, 200));

            DemoEvent demoEvent = createDemoEvent(eventJson, "analytics", "Analytics");

            eventStore.publishEvent(demoEvent.getLoanId(), demoEvent);

        } catch (InterruptedException e) {
            log.error("Analytics interrupted!", e);
        }
    }

    private void fireNotificationEvent(String eventJson) {
        try {
            Thread.sleep(randomBeetween(150, 250));

            DemoEvent demoEvent = createDemoEvent(eventJson, "notification", "Notification");

            eventStore.publishEvent(demoEvent.getLoanId(), demoEvent);

        } catch (InterruptedException e) {
            log.error("Benachrichtigung versenden interrupted!", e);
        }
    }

    private void firePaymentFinished(String eventJson) {
        try {
            Thread.sleep(randomBeetween(400, 600));

            DemoEvent demoEvent = createDemoEvent(eventJson, "payment-finished", "Payment Finished");

            eventStore.publishEvent(demoEvent.getLoanId(), demoEvent);

        } catch (InterruptedException e) {
            log.error("Zahlung Abgeschlossen interrupted!", e);
        }
    }

    private void fireFraudDetection(String eventJson) {
        try {
            Thread.sleep(randomBeetween(500, 700));

            DemoEvent demoEvent = createDemoEvent(eventJson, "fraud", "Fraud Check");

            eventStore.publishEvent(demoEvent.getLoanId(), demoEvent);

        } catch (InterruptedException e) {
            log.error("Zahlung Abgeschlossen interrupted!", e);
        }
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

    public void publishLoanActivatedToUI(UUID loanUuid, UUID copyIdUuid, String dueDate) {
        // Speichere Start-Timestamp für elapsed-time Berechnung
        loanStartTimes.put(loanUuid.toString(), System.currentTimeMillis());

        // Optional: Initial Event an UI senden
        DemoEvent demoEvent = DemoEvent.builder()
                .type("loan-activated")
                .loanId(loanUuid.toString())
                //.copyId(copyIdUuid.toString())
                //.dueDate(dueDate)
                .message("Die Ausleihe wurde aktiviert. Der User wird informiert.")
                .timestamp(System.currentTimeMillis())
                .elapsedMs(calculateElapsedTime(loanUuid.toString()))
                .build();

        eventStore.publishEvent(loanUuid.toString(), demoEvent);
    }

    public void publishBookRegisteredToUI(UUID loanUuid, UUID userUuid, String bookId, UUID reservationId) {
        // Speichere Start-Timestamp für elapsed-time Berechnung
        loanStartTimes.put(loanUuid.toString(), System.currentTimeMillis());

        // Optional: Initial Event an UI senden
        DemoEvent demoEvent = DemoEvent.builder()
                .type("book-registered")
                .loanId(loanUuid.toString())
                .userId(userUuid != null ? userUuid.toString() : "")
                .bookId(bookId)
                .reservationId(reservationId.toString())
                .message("Buch wurde erfasst")
                .timestamp(System.currentTimeMillis())
                .elapsedMs(calculateElapsedTime(loanUuid.toString()))
                .build();

        eventStore.publishEvent(loanUuid.toString(), demoEvent);
    }

    public void publishBookReservedToUI(UUID loanUuid, UUID userUuid, String bookId, UUID reservationId) {
        // Speichere Start-Timestamp für elapsed-time Berechnung
        loanStartTimes.put(loanUuid.toString(), System.currentTimeMillis());

        // Optional: Initial Event an UI senden
        DemoEvent demoEvent = DemoEvent.builder()
                .type("book-reserved")
                .loanId(loanUuid.toString())
                .userId(userUuid != null ? userUuid.toString() : "")
                .bookId(bookId)
                .reservationId(reservationId.toString())
                .message("Buch wurde reserviert")
                .timestamp(System.currentTimeMillis())
                .elapsedMs(calculateElapsedTime(loanUuid.toString()))
                .build();

        eventStore.publishEvent(loanUuid.toString(), demoEvent);
    }

    public void publishPaymentCapturedToUI(UUID loanUuid, UUID userUuid, String bookId, String occuredAt) {
        // Speichere Start-Timestamp für elapsed-time Berechnung
        loanStartTimes.put(loanUuid.toString(), System.currentTimeMillis());

        // Optional: Initial Event an UI senden
        DemoEvent demoEvent = DemoEvent.builder()
                .type("payment-captured")
                .loanId(loanUuid.toString())
                .userId(userUuid.toString())
                .bookId(bookId)
                .message("Zahlung erfasst")
                .timestamp(System.currentTimeMillis())
                //.amount((double) fee.getCent())
                //.transactionId(transactionId.toString())
                .elapsedMs(calculateElapsedTime(loanUuid.toString()))
                .occuredAt(occuredAt)
                .build();

        eventStore.publishEvent(loanUuid.toString(), demoEvent);
    }

    public void publishProcurementInitiatedToUI(UUID loanUuid, String bookId) {
        // Speichere Start-Timestamp für elapsed-time Berechnung
        loanStartTimes.put(loanUuid.toString(), System.currentTimeMillis());

        // Optional: Initial Event an UI senden
        DemoEvent demoEvent = DemoEvent.builder()
                .type("procurement-initiated")
                .loanId(loanUuid.toString())
                .userId("")
                .bookId(bookId)
                .reservationId("")
                .message("Procurement wurde beauftragt!")
                .timestamp(System.currentTimeMillis())
                .elapsedMs(calculateElapsedTime(loanUuid.toString()))
                .build();

        eventStore.publishEvent(loanUuid.toString(), demoEvent);
    }

    public void publishBookOrderedExternallyToUI(UUID loanUuid, String bookId, String estimatedArrival) {
        // Speichere Start-Timestamp für elapsed-time Berechnung
        loanStartTimes.put(loanUuid.toString(), System.currentTimeMillis());

        // Optional: Initial Event an UI senden
        DemoEvent demoEvent = DemoEvent.builder()
                .type("book-externally-ordered")
                .loanId(loanUuid.toString())
                .userId("")
                .reservationId("")
                .bookId(bookId)
                //.estimatedArrival(estimatedArrival)
                .message("Procurement wurde beauftragt!")
                .timestamp(System.currentTimeMillis())
                .elapsedMs(calculateElapsedTime(loanUuid.toString()))
                .build();

        eventStore.publishEvent(loanUuid.toString(), demoEvent);
    }

    public void publishOrderReceivedToUI(UUID loanUuid, String bookId) {//, String externalOrderId) {
        // Speichere Start-Timestamp für elapsed-time Berechnung
        loanStartTimes.put(loanUuid.toString(), System.currentTimeMillis());

        // Optional: Initial Event an UI senden
        DemoEvent demoEvent = DemoEvent.builder()
                .type("order-received")
                .loanId(loanUuid.toString())
                .userId("")
                .bookId(bookId)
                .reservationId("")
                .message("Das Buch ist angekommen!")
                .timestamp(System.currentTimeMillis())
                //.externalOrderId(externalOrderId);
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
