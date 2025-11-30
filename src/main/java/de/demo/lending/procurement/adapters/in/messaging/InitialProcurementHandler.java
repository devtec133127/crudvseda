package de.demo.lending.procurement.adapters.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventUtil;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.dto.BookNotFoundLocallyPayload;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.domain.port.in.InitiateProcurementUseCase;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka-basierter Event Listener für Inventory.
 * <p>
 * Aktiviert durch Profile "kafka" (Standard für Produktion).
 * Wird durch AsyncInventoryEventListener ersetzt bei Profile "async".
 */
@Component
public class InitialProcurementHandler {

    private static final Logger log = LoggerFactory.getLogger(InitialProcurementHandler.class);

    private final ObjectMapper om = new ObjectMapper();
    private final InitiateProcurementUseCase initiateProcurementUseCase;

    public InitialProcurementHandler(InitiateProcurementUseCase initiateProcurementUseCase) {
        this.initiateProcurementUseCase = initiateProcurementUseCase;
    }

    @KafkaListener(
            topics = {Topics.INVENTORY_BOOK_NOT_FOUND_V1},
            groupId = "inventory")
    @Transactional
    public void initialProcurement(String json,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                   @Header(value = KafkaHeaders.OFFSET, required = false) long offset) throws Exception {

        BookNotFoundLocallyPayload payload = om.readValue(json, BookNotFoundLocallyPayload.class);
        log.info("Received BookNotFoundLocally event for loan: {}, book: {}", payload.getLoanId(), payload.getBookId());

        String incomingEventId = payload.getEventId().toString();

        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        ProcessedEventUtil.checkEvent(InitialProcurementHandler.class, incomingEventId);

        LoanId loanId = LoanId.of(UUID.fromString(payload.getLoanId()));
        UserId userId = UserId.of(UUID.fromString(payload.getUserId()));
        BookId bookId = BookId.of(payload.getBookId());

        if (shouldProcure(loanId, bookId)) {
            log.info("Initiating procurement for book: {}", bookId);

            // Starte Procurement
            InitiateProcurementUseCase.InitiateProcurementCommand command = InitiateProcurementUseCase.InitiateProcurementCommand.of(
                    loanId,
                    userId,
                    bookId
            );

            InitiateProcurementUseCase.ProcurementResult result = initiateProcurementUseCase.execute(command);

            //uiPublisher.publishProcurementInitiatedToUI(loanId.value(), payload.getBookTitle());

            if (result.isSuccess()) {
                log.info("Procurement initiated successfully. OrderId: {}",
                        result.getProcurementOrderId()
                );
                // Use Case hat bereits procurement.initiated.v1 published
            } else {
                log.warn("Procurement failed: {}", result.getFailureReason());
                // Optional: Publish procurement.failed.v1
            }
        } else {
            log.info("Decided NOT to procure book: {}", bookId);
            // Optional: Publish procurement.declined.v1
        }

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(InitialProcurementHandler.class, incomingEventId);
    }

    /**
     * Business-Logik: Soll dieses Buch beschafft werden?
     */
    private boolean shouldProcure(LoanId loanId, BookId bookId) {
        // Beispiel-Logik (kann komplex sein):

        // 1. Immer procuren (für Demo)
        return true;

        // 2. Oder: Prüfe Business Rules
        // - Ist der User berechtigt?
        // - Ist das Budget vorhanden?
        // - Ist die Kategorie erlaubt?
        // - etc.
    }
}
