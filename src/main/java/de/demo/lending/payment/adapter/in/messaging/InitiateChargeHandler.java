package de.demo.lending.payment.adapter.in.messaging;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventUtil;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.payment.domain.port.in.InitiateChargeUseCase;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class InitiateChargeHandler {

    private final ObjectMapper om = new ObjectMapper();
    private final InitiateChargeUseCase initiateChargeUseCase;

    public InitiateChargeHandler(InitiateChargeUseCase initiateChargeUseCase) {
        this.initiateChargeUseCase = initiateChargeUseCase;
    }

    @KafkaListener(
            topics = {Topics.LOAN_ACTIVATED_V1},
            groupId = "loan")
    @Transactional
    public void onBookReceived(String json,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                               @Header(value = KafkaHeaders.OFFSET, required = false) long offset) throws Exception {
        BookReservedPayload payload = om.readValue(json, BookReservedPayload.class);

        String incomingEventId = payload.getEventId().toString();

        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        ProcessedEventUtil.checkEvent(InitiateChargeHandler.class, incomingEventId);

        UUID loanIdUUId = UUID.fromString(payload.getLoanId());
        UUID userIdUUId = UUID.fromString(payload.getUserId());

        LoanId loanId = LoanId.of(loanIdUUId);
        UserId userId = UserId.of(userIdUUId);
        InitiateChargeUseCase.InitiateChargeCommand command = InitiateChargeUseCase.InitiateChargeCommand.of(loanId, userId);
        initiateChargeUseCase.initiate(command);

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(InitiateChargeHandler.class, incomingEventId);
    }
}
