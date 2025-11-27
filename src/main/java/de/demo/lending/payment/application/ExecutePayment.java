package de.demo.lending.payment.application;

import static de.demo.lending.common.events.Topics.PAYMENT_V1;

import java.util.UUID;

import de.demo.lending.common.adapters.out.outbox.messaging.EventPublisher;
import de.demo.lending.common.valueobjects.BookId;
import de.demo.lending.common.valueobjects.UserId;
import de.demo.lending.loan.domain.LoanId;
import de.demo.lending.payment.application.dto.PaymentCapturedPayload;
import de.demo.lending.payment.application.dto.PaymentCreatedPayload;
import de.demo.lending.payment.application.dto.PaymentFailedPayload;
import de.demo.lending.payment.application.dto.event.PaymentEventMapper;
import de.demo.lending.payment.domain.Money;
import de.demo.lending.payment.domain.Payment;
import de.demo.lending.payment.domain.PaymentMethod;
import de.demo.lending.payment.domain.event.PaymentCaptured;
import de.demo.lending.payment.domain.event.PaymentCreated;
import de.demo.lending.payment.domain.event.PaymentFailed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ExecutePayment {
    private final JsonPlaceHolderClient externalClient;
    private final PaymentRepository paymentRepository;
    private final EventPublisher publisher; // eigenes Port-Interface, s.u.

    public ExecutePayment(JsonPlaceHolderClient externalClient, PaymentRepository paymentRepository, EventPublisher publisher) {
        this.externalClient = externalClient;
        this.paymentRepository = paymentRepository;
        this.publisher = publisher;
    }

    @Transactional
    public Payment createPayment(UserId userId, LoanId loanId, BookId bookId, Money amount, PaymentMethod method,
                                 String correlationId, String causationId) {
        // 1. Aggregate erzeugen
        Payment payment = Payment.createNew(UUID.randomUUID(), loanId, bookId, userId, amount, method, correlationId);
        paymentRepository.save(payment);
        payment.pullProducedEvents().forEach(event -> {
            if (event instanceof PaymentCreated) {
                PaymentCreatedPayload payload = PaymentEventMapper.toPayload((PaymentCreated) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", PAYMENT_V1, payload);
                publisher.enqueue(PAYMENT_V1, payload);
            }
        });
        return payment;
    }

    // außerhalb TX: external call
    public boolean capture(Payment payment, String correlationId, String causationId) {
        boolean success = externalClient.call(payment.getUserId().toString(), payment.getAmount());
        if (!success) {
            markPaymentFailed(payment, correlationId, causationId);
        }

        return success;
    }

    @Transactional
    public void markPaymentFailed(Payment payment, String correlationId, String causationId) {
        payment.fail("Not enough money to pay!!!"); // Aggregat erzeugt PaymentFailed Event
        paymentRepository.save(payment);

        // Event publizieren
        payment.pullProducedEvents().forEach(event -> {
            if (event instanceof PaymentFailed) {
                PaymentFailedPayload payload = PaymentEventMapper.toPayload((PaymentFailed) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", PAYMENT_V1, payload);
                publisher.enqueue(PAYMENT_V1, payload);
            }
        });
    }

    @Transactional
    public void updatePaymentAfterCapture(Payment payment, String correlationId, String causationId) {
        payment.capture(payment.getAmount());
        paymentRepository.save(payment);
        payment.pullProducedEvents().forEach(event -> {
            if (event instanceof PaymentCaptured) {
                PaymentCapturedPayload payload = PaymentEventMapper.toPayload((PaymentCaptured) event, correlationId, causationId);
                log.info("Publishing event to topic {}: {}", PAYMENT_V1, payload);
                publisher.enqueue(PAYMENT_V1, payload);
            }
        });
    }

    public void handle(UserId userId, LoanId loanId, BookId bookId, Money amount, PaymentMethod method,
                       String correlationId, String causationId) {

        Payment payment = createPayment(userId, loanId, bookId, amount, method, correlationId, causationId);

        boolean success = capture(payment, correlationId, causationId);

        if (success) {
            updatePaymentAfterCapture(payment, correlationId, causationId);
        }
    }
}