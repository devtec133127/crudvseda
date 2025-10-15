package de.demo.lending.loan.application;

import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.loan.application.dto.LoanRequestedPayload;
import de.demo.lending.loan.application.dto.event.LoanEventMapper;
import de.demo.lending.loan.domain.Loan;
import de.demo.lending.loan.domain.event.LoanRequested;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "service.role", havingValue = "loan")
public class CreateLoan {
    private final LoanRepository repo;
    private final EventPublisher outbox; // eigenes Port-Interface, s.u.

    public CreateLoan(LoanRepository repo, EventPublisher outbox) {
        this.repo = repo; this.outbox = outbox;
    }

    /* Application Service koordiniert die folgenden Schritte:
     1. Erzeugung & Speichern des Aggregates
     2. Domain Events -> Payload übersetzen (Domain Event -> Integration Event)
     3. Übergabe an Outbox Publisher
     */
    @Transactional
    public UUID handle(UserId userId, BookId bookId, String correlationId, String causationId) {
        var loan = Loan.createNew(userId, bookId);
        repo.save(loan);

        loan.pullDomainEvents().forEach(event -> {
            // Fachliches Event -> Payload fürs Outbox System
            if(event instanceof LoanRequested) {
                LoanRequestedPayload payload = LoanEventMapper.toPayload((LoanRequested) event, correlationId, causationId);
                outbox.enqueue("loan.requested.v1", payload);
            }
        });

        return loan.getId().value();
    }
}
