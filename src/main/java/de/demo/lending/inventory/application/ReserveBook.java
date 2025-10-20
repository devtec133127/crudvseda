package de.demo.lending.inventory.application;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.dto.BookReservedPayload;
import de.demo.lending.inventory.application.dto.ReservationCreatedPayload;
import de.demo.lending.inventory.application.dto.event.BookReservedEventMapper;
import de.demo.lending.inventory.application.dto.event.ReservationEventMapper;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.inventory.domain.Reservation;
import de.demo.lending.inventory.domain.ReservationId;
import de.demo.lending.inventory.domain.event.BookReserved;
import de.demo.lending.inventory.domain.event.ReservationCreated;
import de.demo.lending.loan.application.LoanRepository;
import de.demo.lending.loan.application.dto.LoanRequestedPayload;
import de.demo.lending.loan.application.dto.event.LoanEventMapper;
import de.demo.lending.loan.domain.Loan;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.loan.domain.event.LoanRequested;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.demo.lending.common.events.Topics.INVENTORY_RESERVED_V1;
import static de.demo.lending.common.events.Topics.LOAN_REQUESTED_V1;
import static de.demo.lending.common.events.Topics.RESERVATION_CREATED_V1;

@Slf4j
@Component
public class ReserveBook {
    private final OpenLibraryClient externalClient;
    private final ReservationRepository reservationRepository;
    private final InventoryRepository repo;
    private final EventPublisher publisher; // eigenes Port-Interface, s.u.

    public ReserveBook(OpenLibraryClient externalClient, ReservationRepository reservationRepository, InventoryRepository repo, EventPublisher publisher) {
        this.externalClient = externalClient;
        this.repo = repo;
        this.reservationRepository = reservationRepository;
        this.publisher = publisher;
    }

    /* Application Service koordiniert die folgenden Schritte:
     1. Erzeugung & Speichern des Aggregates
     2. Domain Events -> Payload übersetzen (Domain Event -> Integration Event)
     3. Übergabe an Outbox Publisher
     */
    @Transactional
    public void handle(UserId userId, LoanId loanId, String bookTitle, Duration duration, String correlationId, String causationId) {

        Pair<String, String> bookInfo = this.externalClient.searchBook(bookTitle);

        log.info("Buch {} vorhanden", bookInfo.getSecond());

        String title = bookInfo.getSecond();
        String isbn = bookInfo.getFirst();

        BookId bookId = BookId.of(isbn);

        var reservation = Reservation.create(correlationId, causationId, bookId, title, userId, duration);
        reservationRepository.save(reservation);

        reservation.pullProducedEvents().forEach(event -> {
            if(event instanceof ReservationCreated) {
                ReservationCreatedPayload payload = ReservationEventMapper.toPayload((ReservationCreated) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", RESERVATION_CREATED_V1, payload);
                publisher.enqueue(RESERVATION_CREATED_V1, payload);
            }
        });

        InventoryCopy copy = InventoryCopy.createNew(correlationId, causationId, loanId, BookId.of(isbn), title, userId);
        repo.save(copy);

        copy.pullProducedEvents().forEach(event -> {
            if(event instanceof BookReserved) {
                BookReservedPayload payload = BookReservedEventMapper.toPayload((BookReserved) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", INVENTORY_RESERVED_V1, payload);
                publisher.enqueue(INVENTORY_RESERVED_V1, payload);
            }
        });
    }
}
