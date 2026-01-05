package de.demo.lending.inventory.application;

import static de.demo.lending.common.events.Topics.INVENTORY_RESERVED_V1;

import java.time.Duration;
import java.util.Optional;

import de.demo.lending.common.application.ports.out.EventPublisher;
import de.demo.lending.common.events.BookReserved;
import de.demo.lending.common.events.integration.BookReservedPayload;
import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.LoanId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.dto.event.BookReservedEventMapper;
import de.demo.lending.inventory.application.ports.in.InventoryResult;
import de.demo.lending.inventory.application.ports.in.reserve.ReserveBookCommand;
import de.demo.lending.inventory.application.ports.in.reserve.ReserveBookUseCase;
import de.demo.lending.inventory.application.ports.in.reserve.ReserveLocalBookCommand;
import de.demo.lending.inventory.application.ports.out.InventoryRepository;
import de.demo.lending.inventory.application.ports.out.PendingReservationRepository;
import de.demo.lending.inventory.application.ports.out.ReservationRepository;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.inventory.domain.PendingReservation;
import de.demo.lending.inventory.domain.Reservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReserveBookService implements ReserveBookUseCase {
    private final ReservationRepository reservationRepository;
    private final InventoryRepository repo;
    private final EventPublisher publisher; // eigenes Port-Interface, s.u.
    private final PendingReservationRepository pendingReservationRepository;

    public ReserveBookService(ReservationRepository reservationRepository,
                              InventoryRepository repo, EventPublisher publisher, PendingReservationRepository pendingReservationRepository) {
        this.repo = repo;
        this.reservationRepository = reservationRepository;
        this.publisher = publisher;
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
    public InventoryResult reserveBook(ReserveBookCommand reserveBookCommand) {
        log.info("Reserviere Buch mit ID {}", reserveBookCommand.getBookId().value());

        InventoryCopy copy = reserveBookCommand.getCopy();
        UserId userId = reserveBookCommand.getUserId();
        LoanId loanId = reserveBookCommand.getLoanId();

        PendingReservation pending = pendingReservationRepository.findBookForLoan(copy.getIsbn(), userId, loanId)
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

        return InventoryResult.success(reservation.getReservationId());
    }

    @Transactional
    public InventoryResult reserveLocalBook(ReserveLocalBookCommand command) {

        UserId userId = command.getUserId();
        Isbn isbn = command.getIsbn();
        long durationInDays = command.getDurationInDays();

        Optional<InventoryCopy> foundBook = repo.lookupForBookInLocal(isbn.value());

        final InventoryCopy localCopy;
        if (foundBook.isPresent()) {
            localCopy = foundBook.get();
            log.info("Buch {} im local store vorhanden", localCopy.getBookId().value());

            localCopy.reserve("", "", durationInDays, userId);
            repo.save(localCopy);

            localCopy.pullProducedEvents().forEach(event -> {
                if (event instanceof BookReserved) {
                    BookReservedPayload payload = BookReservedEventMapper.toPayload((BookReserved) event, "", "");
                    log.info("Publishing event to topic {}: {}", INVENTORY_RESERVED_V1, payload);
                    publisher.enqueue(INVENTORY_RESERVED_V1, payload);
                }
            });

            return InventoryResult.success(localCopy);
        }

        return InventoryResult.failed("Keine lokale Kopie des Buches gefunden!");
    }
}
