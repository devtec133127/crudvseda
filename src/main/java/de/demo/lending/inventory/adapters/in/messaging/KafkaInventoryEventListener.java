package de.demo.lending.inventory.adapters.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventUtil;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.port.in.RegisterBookUseCase;
import de.demo.lending.inventory.domain.port.in.ReserveBookUseCase;
import de.demo.lending.inventory.domain.port.out.InventoryRepository;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.loan.application.dto.LoanRequestedPayload;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.application.dto.BookReceivedPayload;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Kafka-basierter Event Listener für Inventory.
 * <p>
 * Aktiviert durch Profile "kafka" (Standard für Produktion).
 * Wird durch AsyncInventoryEventListener ersetzt bei Profile "async".
 */
@Component
public class KafkaInventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaInventoryEventListener.class);

    private final ObjectMapper om = new ObjectMapper();
    private final InventoryRepository repo;
    private final EventPublisher events;
    private final DemoEventSSEPublisher uiPublisher;
    private final ReserveBookUseCase reserveBookUseCase;
    private final RegisterBookUseCase registerBookUseCase;

    public KafkaInventoryEventListener(ReserveBookUseCase reserveBookUseCase,
                                       InventoryRepository repo,
                                       EventPublisher events,
                                       DemoEventSSEPublisher uiPublisher,
                                       RegisterBookUseCase registerBookUseCase) {
        this.reserveBookUseCase = reserveBookUseCase;
        this.repo = repo;
        this.events = events;
        this.uiPublisher = uiPublisher;
        this.registerBookUseCase = registerBookUseCase;
    }

    @KafkaListener(
            topics = {Topics.LOAN_REQUESTED_V1},
            groupId = "inventory")
    @Transactional
    public void onLoanRequested(String json,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                @Header(value = KafkaHeaders.OFFSET, required = false) long offset) throws Exception {

        LoanRequestedPayload payload = om.readValue(json, LoanRequestedPayload.class);

        String incomingEventId = payload.getEventId().toString();
        incomingEventId = payload.getEventId().toString();

        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        ProcessedEventUtil.checkEvent(KafkaInventoryEventListener.class, incomingEventId);

        // mandatory fields expected: loanId, bookId
        if (payload.getLoanId() == null || payload.getBookTitle() == null) {
            log.warn("Received loan.requested without loanId/bookId: {}", json);
            return;
        }

        LoanId loanId = LoanId.of(UUID.fromString(payload.getLoanId()));
        UserId userId = UserId.of(UUID.fromString(payload.getUserId()));

        uiPublisher.publishLoanCreatedToUI(loanId.value(), userId.value(), payload.getBookTitle(), payload.getDuration());

        Duration duration = Duration.ofDays(payload.getDuration());

        reserveBookUseCase.reserveBook(userId, loanId, payload.getBookTitle(), duration, payload.getCorrelationId(), payload.getEventId().toString());

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(KafkaInventoryEventListener.class, incomingEventId);
    }

    @KafkaListener(
            topics = {Topics.PROCUREMENT_RECEIVED_V1},
            groupId = "procurement")
    @Transactional
    public void onBookReceived(String json,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                               @Header(value = KafkaHeaders.OFFSET, required = false) long offset) throws Exception {
        BookReceivedPayload payload = om.readValue(json, BookReceivedPayload.class);

        String incomingEventId = payload.getEventId().toString();
        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        ProcessedEventUtil.checkEvent(KafkaInventoryEventListener.class, incomingEventId);

        UUID loanIdUUId = UUID.fromString(payload.getLoanId());

        uiPublisher.publishOrderReceivedToUI(loanIdUUId, payload.getBookId()); //, pay.getExternalOrderId());

        LoanId loanId = LoanId.of(UUID.fromString(payload.getLoanId()));
        BookId bookId = BookId.of(payload.getBookId());
        BookTitle bookTitle = BookTitle.of("test");

        RegisterBookUseCase.RegisterBookCommand registerBookCommand = RegisterBookUseCase.RegisterBookCommand.of(loanId, bookTitle, bookId);
        registerBookUseCase.registerBook(registerBookCommand);

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(KafkaInventoryEventListener.class, incomingEventId);
    }
}
