package de.demo.lending.inventory.adapters.in.messaging;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventUtil;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.BookTitle;
import de.demo.lending.inventory.domain.port.in.RegisterBookUseCase;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.application.dto.BookReceivedPayload;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class BookReceivedListener {

    private final ObjectMapper om = new ObjectMapper();
    private final DemoEventSSEPublisher uiPublisher;
    private final RegisterBookUseCase registerBookUseCase;

    public BookReceivedListener(DemoEventSSEPublisher uiPublisher, RegisterBookUseCase registerBookUseCase) {
        this.uiPublisher = uiPublisher;
        this.registerBookUseCase = registerBookUseCase;
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
        ProcessedEventUtil.checkEvent(BookReceivedListener.class, incomingEventId);

        UUID loanIdUUId = UUID.fromString(payload.getLoanId());

        uiPublisher.publishOrderReceivedToUI(loanIdUUId, payload.getBookId()); //, pay.getExternalOrderId());

        LoanId loanId = LoanId.of(UUID.fromString(payload.getLoanId()));
        BookId bookId = BookId.of(payload.getBookId());
        BookTitle bookTitle = BookTitle.of("test");

        RegisterBookUseCase.RegisterBookCommand registerBookCommand = RegisterBookUseCase.RegisterBookCommand.of(loanId, bookTitle, bookId);
        registerBookUseCase.registerBook(registerBookCommand);

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(BookReceivedListener.class, incomingEventId);
    }
}
