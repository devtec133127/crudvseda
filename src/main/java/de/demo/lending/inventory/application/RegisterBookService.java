package de.demo.lending.inventory.application;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.inventory.application.dto.BookRegisteredPayload;
import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.inventory.application.dto.event.BookRegisteredEventMapper;
import de.demo.lending.inventory.application.dto.event.BookReservedEventMapper;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.inventory.domain.event.BookRegistered;
import de.demo.lending.inventory.domain.event.BookReserved;
import de.demo.lending.inventory.domain.port.in.RegisterBookUseCase;
import de.demo.lending.inventory.domain.port.out.InventoryRepository;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static de.demo.lending.common.events.Topics.INVENTORY_BOOK_REGISTERED_V1;
import static de.demo.lending.common.events.Topics.INVENTORY_RESERVED_V1;

@Slf4j
@Service
public class RegisterBookService implements RegisterBookUseCase {

    private final InventoryRepository repository;
    private final EventPublisher publisher;
    private final DemoEventSSEPublisher uiPublisher;

    public RegisterBookService(InventoryRepository repository, EventPublisher publisher, DemoEventSSEPublisher uiPublisher) {
        this.repository = repository;
        this.publisher = publisher;
        this.uiPublisher = uiPublisher;
    }


    @Override
    public void registerBook(RegisterBookCommand command) {
        InventoryCopy copy = InventoryCopy.createNew("", "", command.getLoanId(),
                command.getBookId(), command.getBookTitle());
        copy.registerBook();
        copy.markAsAvailable();
        repository.save(copy);

        copy.pullProducedEvents().forEach(event -> {
            if (event instanceof BookRegistered) {
                BookRegistered registeredBookEvent = (BookRegistered) event;
                BookRegisteredPayload payload = BookRegisteredEventMapper.toPayload(registeredBookEvent, "", "");
                log.info("Publishing event to topic {}: {}", INVENTORY_BOOK_REGISTERED_V1, payload);
                publisher.enqueue(INVENTORY_BOOK_REGISTERED_V1, payload);
                uiPublisher.publishBookRegisteredToUI(event.getLoanId().value(), null, registeredBookEvent.getBookId().value(),
                        registeredBookEvent.getReservationId().value());
            } else if (event instanceof BookReserved) {
                BookReserved reservedBookEvent = ((BookReserved) event);
                BookReservedPayload payload = BookReservedEventMapper.toPayload(reservedBookEvent, "", "");
                log.info("Publishing event to topic {}: {}", INVENTORY_RESERVED_V1, payload);
                publisher.enqueue(INVENTORY_RESERVED_V1, payload);
                // UUID loanUuid, UUID userUuid, String bookId, UUID reservationId
                uiPublisher.publishBookReservedToUI(event.getLoanId().value(), null, reservedBookEvent.getBookId().value(),
                        reservedBookEvent.getReservationId().value());
            }
        });
    }
}
