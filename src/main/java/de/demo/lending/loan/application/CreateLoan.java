package de.demo.lending.loan.application;

import static de.demo.lending.common.events.Topics.LOAN_REQUESTED_V1;

import java.util.UUID;

import de.demo.lending.common.application.ports.out.EventPublisher;
import de.demo.lending.common.events.integration.LoanRequestedPayload;
import de.demo.lending.common.valueobjects.Isbn;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.application.command.ReserveBookCommand;
import de.demo.lending.loan.application.dto.event.LoanEventMapper;
import de.demo.lending.loan.application.ports.out.LoanRepository;
import de.demo.lending.loan.domain.Loan;
import de.demo.lending.loan.domain.event.LoanRequested;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class CreateLoan {
    private final LoanRepository repo;
    private final EventPublisher eventPublisher;

    public CreateLoan(LoanRepository repo, EventPublisher eventPublisher) {
        this.repo = repo;
        this.eventPublisher = eventPublisher;
    }

    /* Application Service koordiniert die folgenden Schritte:
     1. Erzeugung & Speichern des Aggregates
     2. Domain Events -> Payload übersetzen (Domain Event -> Integration Event)
     3. a.) Übergabe an Outbox Publisher, b.) Speichern der Outbox Entity
     */
    @Transactional
    public Loan handle(ReserveBookCommand reserveBookCommand, String correlationId, String causationId) {
        UUID userIdFromRequest = UUID.fromString(reserveBookCommand.userId());
        Loan loan = Loan.request(UserId.of(userIdFromRequest), Isbn.of(reserveBookCommand.isbn()),
                correlationId, causationId);
        repo.save(loan);

        loan.pullProducedEvents().forEach(event -> {
            // Fachliches Event -> Payload fürs Outbox System
            if (event instanceof LoanRequested) {
                LoanRequestedPayload payload = LoanEventMapper.toPayload((LoanRequested) event, correlationId, causationId);

                // Zuvor: Bei Verwendung des Outbox Patterns wird an dieser Stelle nicht mehr versendet.
                log.info("Publishing event to topic {}: {}", LOAN_REQUESTED_V1, payload);
                eventPublisher.enqueue(LOAN_REQUESTED_V1, payload);
            }
        });

        return loan;
    }
}
