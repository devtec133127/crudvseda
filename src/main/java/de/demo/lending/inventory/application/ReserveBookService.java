package de.demo.lending.inventory.application;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.dto.BookNotFoundLocallyPayload;
import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.inventory.application.dto.event.BookNotFoundLocallyMapper;
import de.demo.lending.inventory.application.dto.event.BookReservedEventMapper;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.inventory.domain.event.BookNotFoundLocally;
import de.demo.lending.inventory.domain.event.BookReserved;
import de.demo.lending.inventory.domain.port.out.InventoryRepository;
import de.demo.lending.inventory.domain.port.out.ReservationRepository;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.read.application.port.LoanStatusReadPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

import static de.demo.lending.common.events.Topics.INVENTORY_BOOK_NOT_FOUND_V1;
import static de.demo.lending.common.events.Topics.INVENTORY_RESERVED_V1;

@Slf4j
@Service
public class ReserveBookService implements de.demo.lending.inventory.domain.port.in.ReserveBookUseCase {
    private final OpenLibraryClient externalClient;
    private final ReservationRepository reservationRepository;
    private final InventoryRepository repo;
    private final EventPublisher publisher; // eigenes Port-Interface, s.u.
    private final LoanStatusReadPort loanStatusReadPort;

    public ReserveBookService(OpenLibraryClient externalClient, ReservationRepository reservationRepository,
                              InventoryRepository repo, EventPublisher publisher, LoanStatusReadPort loanStatusReadPort) {
        this.externalClient = externalClient;
        this.repo = repo;
        this.reservationRepository = reservationRepository;
        this.publisher = publisher;
        this.loanStatusReadPort = loanStatusReadPort;
    }

    /* Application Service koordiniert die folgenden Schritte:
     1. Prüfe, ob Buch in lokaler DB vorhanden
     2. Wenn vorhanden, Buch reservieren und DB updaten
        2a. Domain Events -> Payload übersetzen (Domain Event -> Integration Event)
        2c. Senden via Outbox Publisher
     3. Wenn nicht vorhanden, event werfen (hier macht dann Procurement weiter)
     */
    @Transactional
    public void reserveBook(UserId userId, LoanId loanId, String bookTitle, Duration duration, String correlationId, String causationId) {

        Optional<InventoryCopy> foundBook = repo.lookupForBookInLocal(bookTitle);
        final InventoryCopy localCopy;
        if (foundBook.isPresent()) {
            localCopy = foundBook.get();
            log.info("Buch {} im local store vorhanden", localCopy.getBookTitle());
            localCopy.reserve(correlationId, causationId);
            repo.save(localCopy);
        } else {

            BookNotFoundLocally notFoundEvent = new BookNotFoundLocally(loanId, correlationId, causationId, bookTitle, userId);
            BookNotFoundLocallyPayload payload = BookNotFoundLocallyMapper.toPayload(notFoundEvent, correlationId, causationId);
            log.info("Publishing BookNotFoundLocally to topic {}: {}", INVENTORY_BOOK_NOT_FOUND_V1, payload);
            publisher.enqueue(INVENTORY_BOOK_NOT_FOUND_V1, payload);

            // TODO: Auslagern in Procurement Context
            Pair<String, String> bookInfo = this.externalClient.searchBook(bookTitle);
            log.info("Buch {} im external store gefunden", bookInfo.getSecond());

            String title = bookInfo.getSecond();
            String isbn = bookInfo.getFirst();

            localCopy = InventoryCopy.createNew(correlationId, causationId, loanId, BookId.of(isbn), title, userId);
            repo.save(localCopy);
        }

        localCopy.pullProducedEvents().forEach(event -> {
            if (event instanceof BookReserved) {
                BookReservedPayload payload = BookReservedEventMapper.toPayload((BookReserved) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", INVENTORY_RESERVED_V1, payload);
                publisher.enqueue(INVENTORY_RESERVED_V1, payload);
            }
            /*if (event instanceof ProcurementRequested) {
                ReservationCreatedPayload payload = ReservationEventMapper.toPayload((ProcurementRequested) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", RESERVATION_CREATED_V1, payload);
                publisher.enqueue(RESERVATION_CREATED_V1, payload);

                // TODO: Read-Model aktualisieren (in Read-DB), kann aber auch in DB durch trigger gelöst werden
                //loanStatusReadPort.updateLoanStatus(rcEvent.getLoanId(), rcEvent.getUserId(), rcEvent.getBookTitle(), rcEvent.getExpiresAt(), rcEvent.getEventId());

            } else if (event instanceof BookReserved) {
                BookReservedPayload payload = BookReservedEventMapper.toPayload((BookReserved) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", INVENTORY_RESERVED_V1, payload);
                publisher.enqueue(INVENTORY_RESERVED_V1, payload);
            }*/
        });
    }
}
