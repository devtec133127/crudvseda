package de.demo.lending.inventory.adapters.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventUtil;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.domain.Isbn;
import de.demo.lending.inventory.domain.port.in.InventoryResult;
import de.demo.lending.inventory.domain.port.in.register_book.RegisterBookCommand;
import de.demo.lending.inventory.domain.port.in.register_book.RegisterBookUseCase;
import de.demo.lending.inventory.domain.port.in.reserve_book.ReserveBookCommand;
import de.demo.lending.inventory.domain.port.in.reserve_book.ReserveBookUseCase;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.application.dto.BookReceivedPayload;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegisterBookHandler {

    private final ObjectMapper om = new ObjectMapper();
    private final DemoEventSSEPublisher uiPublisher;
    private final RegisterBookUseCase registerBookUseCase;
    private final ReserveBookUseCase reserveBookUseCase;

    public RegisterBookHandler(DemoEventSSEPublisher uiPublisher, RegisterBookUseCase registerBookUseCase, ReserveBookUseCase reserveBookUseCase) {
        this.uiPublisher = uiPublisher;
        this.registerBookUseCase = registerBookUseCase;
        this.reserveBookUseCase = reserveBookUseCase;
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
        ProcessedEventUtil.checkEvent(RegisterBookHandler.class, incomingEventId);

        UUID loanIdUUId = UUID.fromString(payload.getLoanId());

        uiPublisher.publishOrderReceivedToUI(loanIdUUId, payload.getBookId()); //, pay.getExternalOrderId());

        LoanId loanId = LoanId.of(UUID.fromString(payload.getLoanId()));
        BookId bookId = BookId.of(payload.getBookId());
        Isbn isbn = Isbn.of(payload.getIsbn());

        // 1. Buch registrieren
        RegisterBookCommand registerBookCommand = RegisterBookCommand.of(loanId, bookId, isbn);
        InventoryResult result = registerBookUseCase.registerBook(registerBookCommand);

        UserId userId = UserId.of(UUID.fromString(payload.getUserId()));

        ReserveBookCommand reserveBookCommand = ReserveBookCommand.of(loanId, bookId, userId, result.getCopy());
        // 4. Reservation erstellen mit Daten aus Pending
        reserveBookUseCase.reserveBook(reserveBookCommand);

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(RegisterBookHandler.class, incomingEventId);
    }
}
