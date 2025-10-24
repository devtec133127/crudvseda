package de.demo.lending.loan.adapters.in.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.adapters.out.outbox.messaging.async.AsyncEventBus;
import de.demo.lending.common.events.Topics;
import de.demo.lending.inventory.application.InventoryRepository;
import de.demo.lending.inventory.application.ReserveBook;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Async-basierter Event Listener für Inventory (ohne Kafka).
 * Registriert sich beim AsyncEventBus statt @KafkaListener.
 * <p>
 * Aktiviert durch Profile "async".
 */
@Component
@Profile("async")
//@ConditionalOnProperty(value = "service.role", havingValue = "inventory")
public class AsyncLoanEventListener {

    private static final Logger log = LoggerFactory.getLogger(AsyncLoanEventListener.class);

    private final ObjectMapper om = new ObjectMapper();
    private final InventoryRepository repo;
    private final EventPublisher events;
    private final ReserveBook reserveBookUseCase;
    //private final SpringProcessedEventRepository processedRepo;
    private final AsyncEventBus eventBus;

    public AsyncLoanEventListener(
            InventoryRepository repo,
            EventPublisher events,
            ReserveBook reserveBookUseCase,
            //SpringProcessedEventRepository processedRepo,
            AsyncEventBus eventBus) {
        this.repo = repo;
        this.events = events;
        this.reserveBookUseCase = reserveBookUseCase;
        //this.processedRepo = processedRepo;
        this.eventBus = eventBus;
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

        switch (type) {
            case "PaymentCapturedPayload":
                log.info("############ inform user about the successful lending process ############");
                break;
            case "PaymentFailedPayload":
                log.warn("############ inform user about the failed lending process ############");
                break;
            default:
                log.warn("Unbekanntes Event: {}", type);
        }
    }
}
