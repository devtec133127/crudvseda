package de.demo.lending.loan.adapters.in.messaging;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventUtil;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.CopyId;
import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.loan.domain.port.in.ActivateLoanUseCase;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class LoanActivationHandler {

    private final ObjectMapper om = new ObjectMapper();
    private final DemoEventSSEPublisher uiPublisher;
    private final ActivateLoanUseCase activateLoanUseCase;

    public LoanActivationHandler(DemoEventSSEPublisher uiPublisher, ActivateLoanUseCase activateLoanUseCase) {
        this.uiPublisher = uiPublisher;
        this.activateLoanUseCase = activateLoanUseCase;
    }

    @KafkaListener(
            topics = {Topics.INVENTORY_RESERVED_V1},
            groupId = "inventory")
    @Transactional
    public void onBookReceived(String json,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                               @Header(value = KafkaHeaders.OFFSET, required = false) long offset) throws Exception {
        BookReservedPayload payload = om.readValue(json, BookReservedPayload.class);

        String incomingEventId = payload.getEventId().toString();
        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        ProcessedEventUtil.checkEvent(LoanActivationHandler.class, incomingEventId);

        UUID loanIdUUId = UUID.fromString(payload.getLoanId());
        UUID copyIdUUId = UUID.fromString(payload.getCopyId());

        //uiPublisher.publishOrderReceivedToUI(loanIdUUId, payload.getBookId()); //, pay.getExternalOrderId());

        LoanId loanId = LoanId.of(loanIdUUId);
        CopyId copyId = CopyId.of(copyIdUUId);
        ActivateLoanUseCase.ActivateLoanCommand command = ActivateLoanUseCase.ActivateLoanCommand.of(loanId, copyId);
        activateLoanUseCase.activate(command);

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(LoanActivationHandler.class, incomingEventId);
    }
}
