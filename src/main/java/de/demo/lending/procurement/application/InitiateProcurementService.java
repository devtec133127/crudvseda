package de.demo.lending.procurement.application;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.inventory.application.dto.event.BookReservedEventMapper;
import de.demo.lending.inventory.domain.event.BookReserved;
import de.demo.lending.procurement.domain.ProcurementOrder;
import de.demo.lending.procurement.domain.event.ProcurementInitiated;
import de.demo.lending.procurement.domain.port.in.InitiateProcurementUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static de.demo.lending.common.events.Topics.INVENTORY_RESERVED_V1;

@Slf4j
@Service
public class InitiateProcurementService implements InitiateProcurementUseCase {

    private final EventPublisher publisher;

    public InitiateProcurementService(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void execute(InitiateProcurementCommand command) {
        ProcurementOrder order = ProcurementOrder.initiate(command.getLoanId(),
                command.getUserId(),
                command.getBookTitle());
        //order.save(order);

        order.pullProducedEvents().forEach(event -> {
            if (event instanceof ProcurementInitiated) {
                BookReservedPayload payload = BookReservedEventMapper.toPayload((BookReserved) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", INVENTORY_RESERVED_V1, payload);
                publisher.enqueue(INVENTORY_RESERVED_V1, payload);
            }
        });

        // Events clearen (wichtig!)
        order.clearDomainEvents();
    }
}
