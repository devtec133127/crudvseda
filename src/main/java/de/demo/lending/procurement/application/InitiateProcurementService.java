package de.demo.lending.procurement.application;

import static de.demo.lending.common.events.Topics.BOOK_ORDERED_EXTERNALLY_V1;
import static de.demo.lending.common.events.Topics.PROCUREMENT_INITIATED_V1;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.inventory.application.OpenLibraryClient;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.procurement.application.dto.BookOrderedExternallyPayload;
import de.demo.lending.procurement.application.dto.ProcurementInitiatedPayload;
import de.demo.lending.procurement.application.dto.event.BookOrderedExternallyMapper;
import de.demo.lending.procurement.application.dto.event.ProcurementInitiatedMapper;
import de.demo.lending.procurement.domain.ProcurementOrder;
import de.demo.lending.procurement.domain.event.BookOrderedExternally;
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
    private final OpenLibraryClient externalClient;
    private final DemoEventSSEPublisher uiPublisher;

    public InitiateProcurementService(EventPublisher publisher, ProcurementOrderRepository repository, OpenLibraryClient externalClient, DemoEventSSEPublisher uiPublisher) {
        this.publisher = publisher;
        this.repository = repository;
        this.externalClient = externalClient;
        this.uiPublisher = uiPublisher;
    }

    @Override
    public ProcurementResult execute(InitiateProcurementCommand command) {

        OpenLibraryClient.ExternalBookInfo externalBookInfo = externalClient.searchBook(command.getBookTitle().toString());
        if (externalBookInfo == null) {
            log.warn("Book not available in external libraries: {}", command.getBookTitle());
            return ProcurementResult.notAvailable();
        }

        ProcurementOrder order = ProcurementOrder.initiate(command.getLoanId(),
                command.getBookTitle());

        // String externalOrderId = externalClient.orderBook(info.getExternalBookId());
        // log.info("External order created: {}", externalOrderId);

        String externalOrderId = "";
        long estimatedArrival = 0L;
        BookId bookId = BookId.of(externalBookInfo.getIsbn());
        // Update Aggregat mit externer Order-ID
        order.confirmExternalOrder(externalOrderId, bookId, estimatedArrival);
        // Aggregat hat book.ordered_externally.v1 registriert

        // 5️⃣ Speichern
        repository.save(order);


        String correlationId = command.getLoanId().value().toString();
        String causationId = InitiateProcurementCommand.class.getCanonicalName();

        order.pullProducedEvents().forEach(event -> {
            if (event instanceof ProcurementInitiated) {
                ProcurementInitiatedPayload payload = ProcurementInitiatedMapper.toPayload((ProcurementInitiated) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", PROCUREMENT_INITIATED_V1, payload);
                publisher.enqueue(PROCUREMENT_INITIATED_V1, payload);
                uiPublisher.publishProcurementInitiatedToUI(event.getLoanId().value(), payload.getBookTitle());
            } else if (event instanceof BookOrderedExternally) {
                BookOrderedExternallyPayload payload = BookOrderedExternallyMapper.toPayload((BookOrderedExternally) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", BOOK_ORDERED_EXTERNALLY_V1, payload);
                publisher.enqueue(BOOK_ORDERED_EXTERNALLY_V1, payload);
                uiPublisher.publishBookOrderedExternallyToUI(event.getLoanId().value(), payload.getBookId(), payload.getEstimatedArrival());
            }
        });

        return ProcurementResult.success(order.getProcurementOrderId());
    }
}
