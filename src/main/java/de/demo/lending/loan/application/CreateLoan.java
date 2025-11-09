package de.demo.lending.loan.application;

import static de.demo.lending.common.events.Topics.LOAN_REQUESTED_V1;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.demo.lending.common.adapters.out.outbox.messaging.OutboxEntity;
import de.demo.lending.common.adapters.out.outbox.messaging.OutboxRepository;
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
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    public CreateLoan(LoanRepository repo, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
    }

    /* Application Service koordiniert die folgenden Schritte:
     1. Erzeugung & Speichern des Aggregates
     2. Domain Events -> Payload übersetzen (Domain Event -> Integration Event)
     3. a.) Übergabe an Outbox Publisher, b.) Speichern der Outbox Entity
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

                // ############ OUTBOX Pattern ###############
                OutboxEntity savedEntity = createOutboxEntity((LoanRequested) event, correlationId, causationId);
                outboxRepository.save(savedEntity);
                log.info("Event to topic {}: {} saved", LOAN_REQUESTED_V1, payload);


                // Zuvor: Bei Verwendung des Outbox Patterns wird an dieser Stelle nicht mehr versendet.
                //log.info("Publishing event to topic {}: {}", LOAN_REQUESTED_V1, payload);
                //publisher.enqueue(LOAN_REQUESTED_V1, payload);
            }
        });

        return loan.getId().value();
    }

    private OutboxEntity createOutboxEntity(LoanRequested event, String correlationId, String causationId) {
        OutboxEntity entity = new OutboxEntity();
        entity.setEventId(UUID.randomUUID());
        entity.setAggregate_type(Loan.class.getSimpleName());
        entity.setType(LOAN_REQUESTED_V1);
        entity.setCreatedAt(Instant.now());

        try {
            LoanRequestedPayload payload = LoanEventMapper.toPayload(event, correlationId, causationId);
            String jsonPayload = null;
            jsonPayload = objectMapper.writeValueAsString(payload);
            entity.setPayload(jsonPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not create payload for event!!!", e);
        }
        return entity;
    }
}
