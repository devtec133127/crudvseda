package de.demo.lending.payment.adapter.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.async.AsyncEventBus;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventUtil;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.payment.application.ExecutePayment;
import de.demo.lending.payment.domain.PaymentMethod;
import de.demo.lending.payment.domain.PaymentPolicy;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Async-basierter Event Listener für Payment (ohne Kafka).
 * Registriert sich beim AsyncEventBus statt @KafkaListener.
 * <p>
 * Aktiviert durch Profile "async".
 */
@Component
@Profile("async")
public class AsyncPaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(AsyncPaymentEventListener.class);

    private final ObjectMapper om = new ObjectMapper();
    private final ExecutePayment executePaymentUseCase;
    private final AsyncEventBus eventBus;
    private final DemoEventSSEPublisher uiPublisher;

    public AsyncPaymentEventListener(
            ExecutePayment executePaymentUseCase,
            AsyncEventBus eventBus,
            DemoEventSSEPublisher uiPublisher) {
        this.executePaymentUseCase = executePaymentUseCase;
        this.eventBus = eventBus;
        this.uiPublisher = uiPublisher;
    }

    @PostConstruct
    public void subscribeToEvents() {
        // Registriere Handler beim Event Bus
        eventBus.subscribe(Topics.INVENTORY_RESERVED_V1, this::onExecute);
        log.info("Subscribed to {} via AsyncEventBus", Topics.INVENTORY_RESERVED_V1);
    }

    @Transactional
    public void onExecute(String json) throws Exception {
        log.info("Received async event on {}: {}", Topics.INVENTORY_RESERVED_V1, json);
        JsonNode node = om.readTree(json);
        String incomingEventId = node.has("eventId") ? node.get("eventId").asText(null) : null;

        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        ProcessedEventUtil.checkEvent(AsyncPaymentEventListener.class, incomingEventId);

        // mandatory fields expected: loanId, bookId
        if (!node.has("loanId") || !node.has("bookId")) {
            log.warn("Received loan.requested without loanId/bookId: {}", json);
            return;
        }

        UUID loanUuid = UUID.fromString(node.get("loanId").asText());
        UUID userUuid = UUID.fromString(node.get("userId").asText());
        String bookId = node.get("bookId").asText();
        String corralationId = node.get("correlationId").asText();
        String causationId = incomingEventId;

        uiPublisher.publishBookReservedToUI(loanUuid, userUuid, bookId);

        executePaymentUseCase.handle(UserId.of(userUuid), LoanId.of(loanUuid), BookId.of(bookId),
                PaymentPolicy.STANDARD_FEE, PaymentMethod.PAYPAL, corralationId, causationId);

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(AsyncPaymentEventListener.class, incomingEventId);
    }
}
