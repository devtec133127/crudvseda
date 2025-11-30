package de.demo.lending.procurement.adapters.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.application.dto.BookOrderedExternallyPayload;
import de.demo.lending.procurement.application.dto.BookReceivedPayload;
import de.demo.lending.procurement.application.dto.event.BookReceivedMapper;
import de.demo.lending.procurement.domain.ProcurementOrder;
import de.demo.lending.procurement.domain.event.BookReceived;
import de.demo.lending.procurement.domain.port.out.ProcurementOrderRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static de.demo.lending.common.events.Topics.PROCUREMENT_RECEIVED_V1;

/**
 * Kafka-basierter Event Listener für Inventory.
 * <p>
 * Aktiviert durch Profile "kafka" (Standard für Produktion).
 * Wird durch AsyncInventoryEventListener ersetzt bei Profile "async".
 */
@Component
public class ReceiveBookHandler {

    private static final Logger log = LoggerFactory.getLogger(ReceiveBookHandler.class);

    private final ObjectMapper om = new ObjectMapper();
    private final ProcurementOrderRepository repo;
    private final EventPublisher publisher;

    public ReceiveBookHandler(ProcurementOrderRepository repo,
                              EventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    @KafkaListener(
            topics = {Topics.BOOK_ORDERED_EXTERNALLY_V1},
            groupId = "procurement")
    @Transactional
    public void onExternallyOrdered(String json,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                    @Header(value = KafkaHeaders.OFFSET, required = false) long offset) throws Exception {

        BookOrderedExternallyPayload payload = om.readValue(json, BookOrderedExternallyPayload.class);
        log.info("Received BookOrderedExternally event for loan: {}, book: {}", payload.getLoanId(), payload.getBookId());

        LoanId loanId = LoanId.of(UUID.fromString(payload.getLoanId()));
        UserId userId = UserId.of(UUID.fromString(payload.getUserId()));
        ProcurementOrder byLoanId = repo.findByLoanId(loanId);
        if (byLoanId != null) {
            byLoanId.markAsReceived();

            byLoanId.pullProducedEvents().forEach(event -> {
                if (event instanceof BookReceived) {
                    BookReceived receivedEvent = BookReceived.of(byLoanId.getProcurementOrderId(), byLoanId.getExternalOrderId(),
                            byLoanId.getLoanId(), byLoanId.getIsbn(), userId);
                    BookReceivedPayload eventPayload = BookReceivedMapper.toPayload(receivedEvent, "", "");
                    log.info("Publishing BookReceived to topic {}: {}", PROCUREMENT_RECEIVED_V1, eventPayload);
                    publisher.enqueue(PROCUREMENT_RECEIVED_V1, eventPayload);
                }
            });
        }
    }
}
