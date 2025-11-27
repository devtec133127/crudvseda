package de.demo.lending.procurement.application;

import static de.demo.lending.common.events.Topics.PROCUREMENT_INITIATED_V1;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.procurement.application.dto.ProcurementInitiatedPayload;
import de.demo.lending.procurement.application.dto.event.ProcurementInitiatedMapper;
import de.demo.lending.procurement.domain.ProcurementOrder;
import de.demo.lending.procurement.domain.event.ProcurementInitiated;
import de.demo.lending.procurement.domain.port.in.InitiateProcurementUseCase;
import de.demo.lending.procurement.domain.port.out.ProcurementOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InitiateProcurementService implements InitiateProcurementUseCase {

    private final EventPublisher publisher;
    private final ProcurementOrderRepository repository;

    public InitiateProcurementService(EventPublisher publisher, ProcurementOrderRepository repository) {
        this.publisher = publisher;
        this.repository = repository;
    }

    @Override
    public ProcurementResult execute(InitiateProcurementCommand command) {
        ProcurementOrder order = ProcurementOrder.initiate(command.getLoanId(),
                command.getBookTitle());
        repository.save(order);


        String correlationId = command.getLoanId().value().toString();
        String causationId = InitiateProcurementCommand.class.getCanonicalName();

        order.pullProducedEvents().forEach(event -> {
            if (event instanceof ProcurementInitiated) {
                ProcurementInitiatedPayload payload = ProcurementInitiatedMapper.toPayload((ProcurementInitiated) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", PROCUREMENT_INITIATED_V1, payload);
                publisher.enqueue(PROCUREMENT_INITIATED_V1, payload);
            }
        });

        return ProcurementResult.success(order.getProcurementOrderId());
    }
}
