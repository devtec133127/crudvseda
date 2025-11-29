package de.demo.lending.inventory.adapters.in.messaging;

import java.time.Duration;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventUtil;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.dto.BookRegisteredPayload;
import de.demo.lending.inventory.domain.port.in.ReserveBookUseCase;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.loan.application.dto.LoanRequestedPayload;
import de.demo.lending.loan.domain.LoanId;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReserveBookHandler {

    private final ObjectMapper om = new ObjectMapper();
    private final DemoEventSSEPublisher uiPublisher;
    private final ReserveBookUseCase reserveBookUseCase;

    public ReserveBookHandler(DemoEventSSEPublisher uiPublisher, ReserveBookUseCase reserveBookUseCase) {
        this.uiPublisher = uiPublisher;
        this.reserveBookUseCase = reserveBookUseCase;
    }

    @KafkaListener(
            topics = {Topics.INVENTORY_BOOK_REGISTERED_V1},
            groupId = "inventory")
    @Transactional
    public void onBookRegistered(String json,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                 @Header(value = KafkaHeaders.OFFSET, required = false) long offset) throws Exception {
        BookRegisteredPayload payload = om.readValue(json, BookRegisteredPayload.class);

        String incomingEventId = payload.getEventId().toString();

        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        ProcessedEventUtil.checkEvent(ReserveBookHandler.class, incomingEventId);

        LoanId loanId = LoanId.of(UUID.fromString(payload.getLoanId()));
        UserId userId = UserId.of(UUID.fromString(payload.getUserId()));

        //uiPublisher.publishLoanCreatedToUI(loanId.value(), userId.value(), payload.getBookTitle(), payload.getDuration());

        Duration duration = Duration.ofDays(payload.getDuration());

        reserveBookUseCase.reserveBook(userId, loanId, payload.getBookTitle(), duration, payload.getCorrelationId(), payload.getEventId().toString());

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(ReserveBookHandler.class, incomingEventId);
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

        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        ProcessedEventUtil.checkEvent(ReserveBookHandler.class, incomingEventId);

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
        ProcessedEventUtil.saveEvent(ReserveBookHandler.class, incomingEventId);
    }
}
