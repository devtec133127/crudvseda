package de.demo.lending.inventory.adapters.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.adapters.out.persistence.ProcessedEventUtil;
import de.demo.lending.common.events.Topics;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.inventory.application.dto.BookNotFoundLocallyPayload;
import de.demo.lending.inventory.application.dto.event.BookNotFoundLocallyMapper;
import de.demo.lending.inventory.domain.InventoryCopy;
import de.demo.lending.inventory.domain.event.BookNotFoundLocally;
import de.demo.lending.inventory.domain.port.in.CreatePendingReservationUseCase;
import de.demo.lending.inventory.domain.port.in.ReserveBookUseCase;
import de.demo.lending.inventory.domain.port.out.InventoryRepository;
import de.demo.lending.loan.adapters.in.demo.DemoEventSSEPublisher;
import de.demo.lending.loan.application.dto.LoanRequestedPayload;
import de.demo.lending.loan.domain.LoanId;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static de.demo.lending.common.events.Topics.INVENTORY_BOOK_NOT_FOUND_V1;

@Slf4j
@Component
public class ReserveBookHandler {

    private final ObjectMapper om = new ObjectMapper();
    private final DemoEventSSEPublisher uiPublisher;
    private final ReserveBookUseCase reserveBookUseCase;
    private final CreatePendingReservationUseCase createPendingReservationUseCase;
    private final InventoryRepository repo;
    private final EventPublisher publisher;

    public ReserveBookHandler(DemoEventSSEPublisher uiPublisher, ReserveBookUseCase reserveBookUseCase, CreatePendingReservationUseCase createPendingReservationUseCase, InventoryRepository repo, EventPublisher publisher) {
        this.uiPublisher = uiPublisher;
        this.reserveBookUseCase = reserveBookUseCase;
        this.createPendingReservationUseCase = createPendingReservationUseCase;
        this.repo = repo;
        this.publisher = publisher;
    }

    @KafkaListener(
            topics = {Topics.LOAN_REQUESTED_V1},
            groupId = "inventory")
    @Transactional
    public void onLoanRequested(String json,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                @Header(value = KafkaHeaders.OFFSET, required = false) long offset) throws Exception {

        LoanRequestedPayload payload = om.readValue(json, LoanRequestedPayload.class);

        String incomingEventId = payload.getEventId().toString();

        // ########## Indempotenz - Event schon verarbeitet? - Inbox Tabelle abfragen ##########
        ProcessedEventUtil.checkEvent(ReserveBookHandler.class, incomingEventId);

        // mandatory fields expected: loanId, bookId
        if (payload.getLoanId() == null || payload.getIsbn() == null) {
            log.warn("Received loan.requested without loanId/isbn: {}", json);
            return;
        }

        LoanId loanId = LoanId.of(UUID.fromString(payload.getLoanId()));
        UserId userId = UserId.of(UUID.fromString(payload.getUserId()));

        uiPublisher.publishLoanCreatedToUI(loanId.value(), userId.value(), payload.getIsbn(), payload.getDuration());

        Duration duration = Duration.ofDays(payload.getDuration());
        BookId isbn = BookId.of(payload.getIsbn());
        //BookId bookId = BookId.of("123");

        // 1. Prüfung ob in local store vorhanden, sonst Procurement anstoßen
        // Fall B: Buch nicht da → PendingReservation erstellen ⭐
        Optional<InventoryCopy> foundBook = repo.lookupForBookInLocal(isbn.value());

        final InventoryCopy localCopy;
        if (foundBook.isPresent()) {
            localCopy = foundBook.get();
            log.info("Buch mit ID {} im local store vorhanden", localCopy.getBookId());
            // reserv book flow ...
            reserveBookUseCase.reserveBook(userId, loanId, isbn, duration.toDays());
        } else {
            createPendingReservationUseCase.create(
                    isbn,
                    loanId,
                    userId,
                    duration.toDays()
            );

            BookNotFoundLocally notFoundEvent = new BookNotFoundLocally(loanId, "", "", isbn, userId);
            BookNotFoundLocallyPayload eventPayload = BookNotFoundLocallyMapper.toPayload(notFoundEvent, "", "");
            log.info("Publishing BookNotFoundLocally to topic {}: {}", INVENTORY_BOOK_NOT_FOUND_V1, eventPayload);
            publisher.enqueue(INVENTORY_BOOK_NOT_FOUND_V1, eventPayload);
        }

        // ########## Indempotenz - Event verarbeitet -> speichern  ##########
        ProcessedEventUtil.saveEvent(ReserveBookHandler.class, incomingEventId);

    }
}
