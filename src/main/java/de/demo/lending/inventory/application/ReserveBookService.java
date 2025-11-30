package de.demo.lending.inventory.application;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.inventory.application.dto.event.BookReservedEventMapper;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.inventory.domain.PendingReservation;
import de.demo.lending.inventory.domain.Reservation;
import de.demo.lending.inventory.domain.event.BookReserved;
import de.demo.lending.inventory.domain.port.out.InventoryRepository;
import de.demo.lending.inventory.domain.port.out.PendingReservationRepository;
import de.demo.lending.inventory.domain.port.out.ReservationRepository;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.procurement.adapters.out.external.OpenLibraryClientAdapter;
import de.demo.lending.read.application.port.LoanStatusReadPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

import static de.demo.lending.common.events.Topics.INVENTORY_RESERVED_V1;

@Slf4j
@Service
public class ReserveBookService implements de.demo.lending.inventory.domain.port.in.ReserveBookUseCase {
    private final OpenLibraryClientAdapter externalClient;
    private final ReservationRepository reservationRepository;
    private final InventoryRepository repo;
    private final EventPublisher publisher; // eigenes Port-Interface, s.u.
    private final LoanStatusReadPort loanStatusReadPort;
    private final PendingReservationRepository pendingReservationRepository;

    public ReserveBookService(OpenLibraryClientAdapter externalClient, ReservationRepository reservationRepository,
                              InventoryRepository repo, EventPublisher publisher, LoanStatusReadPort loanStatusReadPort, PendingReservationRepository pendingReservationRepository) {
        this.externalClient = externalClient;
        this.repo = repo;
        this.reservationRepository = reservationRepository;
        this.publisher = publisher;
        this.loanStatusReadPort = loanStatusReadPort;
        this.pendingReservationRepository = pendingReservationRepository;
    }

    /* Application Service koordiniert die folgenden Schritte:
     1. Prüfe, ob Buch in lokaler DB vorhanden
     2. Wenn vorhanden, Buch reservieren und DB updaten
        2a. Domain Events -> Payload übersetzen (Domain Event -> Integration Event)
        2c. Senden via Outbox Publisher
     3. Wenn nicht vorhanden, event werfen (hier macht dann Procurement weiter)
     */
    @Transactional
    public void reserveBook(LoanId loanId, BookId bookId, InventoryCopy copy) {
        log.info("Buch mit ID {} im local store vorhanden", copy.getBookId());

        PendingReservation pending = pendingReservationRepository.findByBookId(copy.getBookId())
                .orElseThrow(() -> new RuntimeException("pending reservation for bookId {} " + copy.getBookId().value() + " not found"));

        copy.reserve("", "", pending.getDueDate(), pending.getUserId());
        repo.save(copy);

        Duration duration = Duration.ofDays(pending.getDueDate());
        Reservation reservation = Reservation.create(loanId, copy.getCopyId(), pending.getUserId(), duration);

        reservationRepository.save(reservation);

        copy.pullProducedEvents().forEach(event -> {
            if (event instanceof BookReserved) {
                BookReservedPayload payload = BookReservedEventMapper.toPayload((BookReserved) event, "", "");
                log.info("Publishing event to topic {}: {}", INVENTORY_RESERVED_V1, payload);
                publisher.enqueue(INVENTORY_RESERVED_V1, payload);
            }
        });
    }

    @Transactional
    public void reserveBook(UserId userId, LoanId loanId, BookId bookId, long durationInDays) {

        Optional<InventoryCopy> foundBook = repo.lookupForBookInLocal(bookId.toString());

        final InventoryCopy localCopy;
        if (foundBook.isPresent()) {
            localCopy = foundBook.get();
            log.info("Buch {} im local store vorhanden", localCopy.getBookId());

            localCopy.reserve("", "", durationInDays, userId);
            repo.save(localCopy);

            localCopy.pullProducedEvents().forEach(event -> {
                if (event instanceof BookReserved) {
                    BookReservedPayload payload = BookReservedEventMapper.toPayload((BookReserved) event, "", "");
                    log.info("Publishing event to topic {}: {}", INVENTORY_RESERVED_V1, payload);
                    publisher.enqueue(INVENTORY_RESERVED_V1, payload);
                }
            });
        }
    }
}
