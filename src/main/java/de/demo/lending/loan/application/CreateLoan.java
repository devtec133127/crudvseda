package de.demo.lending.loan.application;

import static de.demo.lending.common.events.Topics.LOAN_REQUESTED_V1;

import java.util.UUID;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.application.command.ReserveBookCommand;
import de.demo.lending.loan.application.dto.LoanRequestedPayload;
import de.demo.lending.loan.application.dto.event.LoanEventMapper;
import de.demo.lending.loan.domain.Loan;
import de.demo.lending.loan.domain.event.LoanRequested;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ConditionalOnProperty(value = "service.role", havingValue = "loan")
public class CreateLoan {
    private final LoanRepository repo;
    private final EventPublisher publisher; // eigenes Port-Interface, s.u.

    public CreateLoan(LoanRepository repo, EventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    /* Application Service koordiniert die folgenden Schritte:
     1. Erzeugung & Speichern des Aggregates
     2. Domain Events -> Payload übersetzen (Domain Event -> Integration Event)
     3. Übergabe an Outbox Publisher
     */
    @Transactional
    public UUID handle(ReserveBookCommand reserveBookCommand, String correlationId, String causationId) {
        UUID userIdFromRequest = UUID.fromString(reserveBookCommand.userId());
        var loan = Loan.createNew(UserId.of(userIdFromRequest), reserveBookCommand.bookTitle());
        repo.save(loan);

        loan.pullDomainEvents().forEach(event -> {
            // Fachliches Event -> Payload fürs Outbox System
            if (event instanceof LoanRequested) {
                LoanRequestedPayload payload = LoanEventMapper.toPayload((LoanRequested) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", LOAN_REQUESTED_V1, payload);
                publisher.enqueue(LOAN_REQUESTED_V1, payload);
            }
        });

        return loan.getId().value();
    }
}
