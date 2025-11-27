package de.demo.lending.loan.adapters.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.adapters.out.outbox.messaging.async.AsyncEventBus;
import de.demo.lending.common.events.Topics;
import de.demo.lending.inventory.application.ReserveBookService;
import de.demo.lending.inventory.domain.port.out.InventoryRepository;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Async-basierter Event Listener für Loan (ohne Kafka).
 * Registriert sich beim AsyncEventBus statt @KafkaListener.
 * <p>
 * Aktiviert durch Profile "async".
 */
@Component
@Profile("async")
public class AsyncLoanEventListener {

    private static final Logger log = LoggerFactory.getLogger(AsyncLoanEventListener.class);

    private final ObjectMapper om = new ObjectMapper();
    private final InventoryRepository repo;
    private final EventPublisher events;
    private final ReserveBookService reserveBookUseCase;
    private final AsyncEventBus eventBus;
    private final DemoEventSSEPublisher uiPublisher;

    public AsyncLoanEventListener(
            InventoryRepository repo,
            EventPublisher events,
            ReserveBookService reserveBookUseCase,
            AsyncEventBus eventBus,
            DemoEventSSEPublisher uiPublisher) {
        this.repo = repo;
        this.events = events;
        this.reserveBookUseCase = reserveBookUseCase;
        this.eventBus = eventBus;
        this.uiPublisher = uiPublisher;
    }

    @PostConstruct
    public void subscribeToEvents() {
        // Registriere Handler beim Event Bus
        eventBus.subscribe(Topics.PAYMENT_V1, this::onPaymentRequested);
        log.info("Subscribed to {} via AsyncEventBus", Topics.PAYMENT_V1);
    }

    @Transactional
    public void onPaymentRequested(String json) throws Exception {
        log.info("Received async event on {}: {}", Topics.PAYMENT_V1, json);
        JsonNode node = om.readTree(json);

        String type = node.get("type").asText();
        UUID loanUuid = UUID.fromString(node.get("loanId").asText());
        UUID userUuid = UUID.fromString(node.get("userId").asText());
        //String bookTitle = node.get("bookTitle").asText();
        String bookId = node.get("bookId").asText();
        String occurredAt = node.get("occurredAt").asText();

        switch (type) {
            case "PaymentCaptured":
                log.info("############ inform user about the successful lending process ############");
                uiPublisher.publishPaymentCapturedToUI(loanUuid, userUuid, bookId, occurredAt);
                break;
            case "PaymentFailedPayload":
                log.warn("############ inform user about the failed lending process ############");
                break;
            default:
                log.warn("Unbekanntes Event: {}", type);
        }


    }
}
