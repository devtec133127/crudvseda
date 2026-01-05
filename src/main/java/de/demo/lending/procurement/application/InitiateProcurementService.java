package de.demo.lending.procurement.application;

import static de.demo.lending.common.events.Topics.BOOK_ORDERED_EXTERNALLY_V1;
import static de.demo.lending.common.events.Topics.PROCUREMENT_INITIATED_V1;

import de.demo.lending.common.application.ports.out.DemoEventSSEPublisher;
import de.demo.lending.common.application.ports.out.EventPublisher;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.procurement.application.dto.BookOrderedExternallyPayload;
import de.demo.lending.procurement.application.dto.ProcurementInitiatedPayload;
import de.demo.lending.procurement.application.dto.event.BookOrderedExternallyMapper;
import de.demo.lending.procurement.application.dto.event.ProcurementInitiatedMapper;
import de.demo.lending.procurement.application.ports.in.InitiateProcurementUseCase;
import de.demo.lending.procurement.application.ports.out.ProcurementClient;
import de.demo.lending.procurement.application.ports.out.ProcurementOrderRepository;
import de.demo.lending.procurement.domain.ProcurementOrder;
import de.demo.lending.procurement.domain.SupplierInfo;
import de.demo.lending.procurement.domain.event.BookOrderedExternally;
import de.demo.lending.procurement.domain.event.ProcurementInitiated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InitiateProcurementService implements InitiateProcurementUseCase {

    private final EventPublisher publisher;
    private final ProcurementOrderRepository repository;
    private final ProcurementClient externalClient;
    private final DemoEventSSEPublisher uiPublisher;

    public InitiateProcurementService(EventPublisher publisher, ProcurementOrderRepository repository,
                                      ProcurementClient externalClient, DemoEventSSEPublisher uiPublisher) {
        this.publisher = publisher;
        this.repository = repository;
        this.externalClient = externalClient;
        this.uiPublisher = uiPublisher;
    }

    @Override
    public ProcurementResult execute(InitiateProcurementCommand command) {

        SupplierInfo externalBookInfo = externalClient.searchBook(command.getIsbn().value());
        if (externalBookInfo == null) {
            log.warn("Book not available in external libraries: {}", command.getIsbn());
            return ProcurementResult.notAvailable();
        }

        BookId bookId = BookId.of(externalBookInfo.externalBookId());

        ProcurementOrder order = ProcurementOrder.initiate(command.getLoanId(),
                bookId, command.getUserId(), command.getIsbn());

        long estimatedArrival = 0L;
        Isbn isbn = Isbn.of(externalBookInfo.isbn());
        // Update Aggregat mit externer Order-ID
        order.confirmExternalOrder(bookId, isbn, estimatedArrival);
        // Aggregat hat book.ordered_externally.v1 registriert

        // Speichern
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
